/*
 * Copyright 2025 Alessandro Talmi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.crolangP2P

import internal.broker.BrokerSocketCreator.createSocketIO
import internal.broker.OnConnectionToBrokerSettings
import internal.events.data.AreNodesConnectedToBrokerMsg
import internal.events.data.AreNodesConnectedToBrokerMsgResponse
import internal.events.data.SocketDirectMsg
import internal.events.data.abstractions.SocketMsgType.Companion.ARE_NODES_CONNECTED_TO_BROKER
import internal.events.data.abstractions.SocketMsgType.Companion.SOCKET_MSG_EXCHANGE
import internal.events.data.abstractions.SocketResponses
import internal.node.NodeState
import internal.utils.AwaitAsyncEventGuard
import internal.utils.CrolangLogger
import internal.utils.RuntimeDependencyResolver
import internal.utils.SharedStore
import internal.utils.SharedStore.brokerLifecycleCallbacks
import internal.utils.SharedStore.disconnectAllInitiatorNodesNotConnected
import internal.utils.SharedStore.disconnectAllResponderNodesNotConnected
import internal.utils.SharedStore.incomingCrolangNodesCallbacks
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.onConnectionToBrokerSettings
import internal.utils.SharedStore.onNewSocketMsgCallbacks
import internal.utils.SharedStore.parser
import internal.utils.SharedStore.settings
import internal.utils.SharedStore.socketIO
import io.socket.client.Ack
import org.crolangP2P.exceptions.AllowIncomingConnectionsException
import org.crolangP2P.exceptions.ConnectToBrokerException
import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException
import org.crolangP2P.exceptions.RemoteNodesConnectionStatusCheckException
import org.crolangP2P.exceptions.SendSocketMsgException
import java.util.*

/**
 * CrolangP2P is a singleton object that manages the connection to the Crolang Broker and allows for connecting to
 * remote CrolangNodes.
 *
 * Contains both a Kotlin and Java interface, allowing for easy integration with both languages.
 *
 * @see Kotlin
 * @see Java
 */
object CrolangP2P {

    /**
     * The Kotlin interface for CrolangP2P.
     */
    object Kotlin {

        /**
         * Checks if the local node is connected to the Crolang Broker.
         * This method returns true if the socket is present and connected, false otherwise.
         *
         * @return true if the local node is connected to the Broker, false otherwise.
         */
        fun isLocalNodeConnectedToBroker(): Boolean {
            return socketIO.isPresent && socketIO.get().connected()
        }

        /**
         * Checks if the provided remote node is connected to the Crolang Broker.
         * This method returns true if the node is connected, false otherwise. If the local Node is not connected to the Broker,
         * an exception is thrown.
         *
         * @param id The ID of the remote node to check.
         * @return A Result containing true if the node is connected, false otherwise.
         */
        fun isRemoteNodeConnectedToBroker(id: String): Result<Boolean> {
            return areRemoteNodesConnectedToBroker(setOf(id)).fold(
                onSuccess = {
                    val result = it[id]
                    if (result != null) {
                        Result.success(result)
                    } else {
                        Result.failure(RemoteNodesConnectionStatusCheckException.UnknownError)
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }

        /**
         * Checks if the provided set of remote nodes are connected to the Crolang Broker.
         * This method returns a map where the key is the node ID and the value is true if the Node is connected,
         * false otherwise. If the local Node is not connected to the Broker, an exception is thrown.
         *
         * @param ids The set of remote node IDs to check.
         * @return A Result containing a map of node IDs and their connection status.
         */
        fun areRemoteNodesConnectedToBroker(ids: Set<String>): Result<Map<String, Boolean>> {
            if(!isLocalNodeConnectedToBroker()){
                return Result.failure(RemoteNodesConnectionStatusCheckException.NotConnectedToBroker)
            } else if(ids.isEmpty()){
                return Result.success(emptyMap())
            }
            var result: Map<String, Boolean>? = null
            val guard = AwaitAsyncEventGuard(UUID.randomUUID().toString())
            guard.startNewCountdown()
            socketIO.get().emit(
                ARE_NODES_CONNECTED_TO_BROKER,
                parser.toJson(AreNodesConnectedToBrokerMsg(ids)),
                Ack { args ->
                    if(args.size != 1 || args[0] == null){
                        guard.stepDown()
                        return@Ack
                    }
                    parser.fromJson(args[0].toString(), AreNodesConnectedToBrokerMsgResponse::class.java).ifPresentOrElse(
                        { res ->
                            if(res.results != null && res.results!!.all { it.id != null && it.connected != null }){
                                result = res.results!!.associate { it.id!! to it.connected!! }
                            }
                            guard.stepDown()
                        },
                        { guard.stepDown() }
                    )
                }
            )
            guard.await()
            return if(result == null){
                Result.failure(RemoteNodesConnectionStatusCheckException.UnknownError)
            } else {
                Result.success(result!!)
            }
        }

        /**
         * Sends a message to a remote node via the Broker using WebSocket relay.
         *
         * @param id The ID of the remote node to send the message to.
         * @param channel The channel on which to send the message.
         * @param msg The message to send (optional). If not provided, an empty string will be sent.
         * @return A Result indicating success or failure. On failure, see [SendSocketMsgException].
         *
         * @see SendSocketMsgException
         */
        fun sendSocketMsg(id: String, channel: String, msg: String?): Result<Unit>{
            if(!isLocalNodeConnectedToBroker()){
                return Result.failure(SendSocketMsgException.NotConnectedToBroker)
            } else if (channel.isEmpty()){
                return Result.failure(SendSocketMsgException.EmptyChannel)
            } else if(id.isEmpty()){
                return Result.failure(SendSocketMsgException.EmptyId)
            } else if(id === localNodeId){
                return Result.failure(SendSocketMsgException.TriedToSendMsgToSelf)
            }

            var err: String? = null
            val guard = AwaitAsyncEventGuard(UUID.randomUUID().toString())
            guard.startNewCountdown()
            socketIO.get().emit(
                SOCKET_MSG_EXCHANGE,
                parser.toJson(SocketDirectMsg(localNodeId, id, channel, msg ?: "")),
                Ack { args ->
                    if(args.size != 1 || args[0] == null || args[0] !is String){
                        err = SocketResponses.ERROR
                        guard.stepDown()
                        return@Ack
                    }
                    val response = args[0] as String
                    if(SocketResponses.ALL.contains(response)){
                        if(!SocketResponses.isOk(response)){
                            err = response
                        }
                    } else {
                        err = SocketResponses.ERROR
                    }
                    guard.stepDown()
                }
            )
            guard.await()
            return if(err == null){
                Result.success(Unit)
            } else {
                Result.failure(SendSocketMsgException.fromMessage(err!!))
            }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID.
         * This method initiates a connection attempt to the Broker and handles the connection process.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @return A Result indicating success or failure of the connection attempt.
         *
         * @see ConnectToBrokerException
         */
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String
        ): Result<Unit> {
            return connectToBroker(brokerAddr, nodeId, "", emptyMap(), BrokerConnectionAdditionalParameters())
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID.
         * This method initiates a connection attempt to the Broker and handles the connection process.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         * @param onNewSocketMsg Optional Map of callbacks for handling direct messages received via the Broker's WebSocket relay.
         * @param additionalParameters Optional additional parameters for the connection, including logging options, settings and lifecycle callbacks.
         * @return A Result indicating success or failure of the connection attempt.
         * 
         * @see ConnectToBrokerException
         */
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onConnectionAttemptData: String = "",
            onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit> = emptyMap(),
            additionalParameters: BrokerConnectionAdditionalParameters = BrokerConnectionAdditionalParameters()
        ): Result<Unit> {
            if(socketIO.isPresent){
                logger.regularErr("trying to connect to Broker while already connected")
                return Result.failure(ConnectToBrokerException.LocalClientAlreadyConnectedToBroker)
            }

            val runtimeDependencyResolved = RuntimeDependencyResolver.loadDependency(CrolangLogger(additionalParameters.logging))
            if(!runtimeDependencyResolved){
                return Result.failure(ConnectToBrokerException.UnsupportedArchitecture)
            }
            onNewSocketMsgCallbacks = onNewSocketMsg
            brokerLifecycleCallbacks = additionalParameters.lifecycleCallbacks
            logger = CrolangLogger(additionalParameters.logging)
            settings = additionalParameters.settings
            logger.regularInfo("initiating Broker connection attempt")
            localNodeId = nodeId
            onConnectionToBrokerSettings = Optional.of(OnConnectionToBrokerSettings(brokerAddr, onConnectionAttemptData))
            val socket = createSocketIO()
            socketIO = Optional.of(socket)
            SharedStore.brokerConnectionHelper.connectionToBrokerGuard.startNewCountdown()
            socket.connect()
            SharedStore.brokerConnectionHelper.connectionToBrokerGuard.await()
            val connectionToBrokerError = SharedStore.brokerConnectionHelper.connectionToBrokerErrorReason
            if(connectionToBrokerError != null){
                logger.regularErr("failed to connect to Broker")
                socket.disconnect()
                onConnectionToBrokerSettings = Optional.empty()
                SharedStore.flush()
                return Result.failure(connectionToBrokerError.toConnectToBrokerException())
            } else {
                return Result.success(Unit)
            }
        }

        /**
         * Disconnects from the Crolang Broker.
         *
         * Connected Nodes will NOT be disconnected; on the other hand, the connection process of Nodes that are still
         * attempting a connection will be forcefully stopped.
         */
        fun disconnectFromBroker() {
            logger.regularInfo("initiating disconnection from Broker")
            if(!socketIO.isPresent){
                logger.regularInfo("already disconnected from Broker")
                return
            }
            SharedStore.brokerConnectionHelper.disconnectionFromBrokerGuard.startNewCountdown()
            socketIO.get().disconnect()
            disconnectAllInitiatorNodesNotConnected()
            disconnectAllResponderNodesNotConnected()
            SharedStore.brokerConnectionHelper.disconnectionFromBrokerGuard.await()
            logger.regularInfo("disconnected from Broker")
            SharedStore.flush()
        }

        /**
         * Checks if incoming connections from other Nodes are allowed.
         * This method returns true if incoming connections are allowed and the socket is connected.
         *
         * @return true if incoming connections are allowed, false otherwise.
         */
        fun areIncomingConnectionsAllowed(): Boolean {
            return incomingCrolangNodesCallbacks.isPresent && socketIO.isPresent
        }

        /**
         * Allows incoming connections from other nodes.
         *
         * @param callbacks The callbacks to be used for incoming connections.
         * @return A Result indicating success or failure.
         * @see IncomingCrolangNodesCallbacks
         * @see AllowIncomingConnectionsException
         */
        fun allowIncomingConnections(
            callbacks: IncomingCrolangNodesCallbacks = IncomingCrolangNodesCallbacks()
        ): Result<Unit> {
            if (socketIO.isEmpty || !socketIO.get().connected()) {
                logger.regularErr("cannot allow incoming connections: not connected to the Crolang Broker")
                return Result.failure(AllowIncomingConnectionsException.NotConnectedToBroker)
            } else if (incomingCrolangNodesCallbacks.isPresent) {
                logger.regularErr("cannot allow incoming connections: incoming connections already allowed")
                return Result.failure(AllowIncomingConnectionsException.IncomingConnectionsAlreadyAllowed)
            }
            incomingCrolangNodesCallbacks = Optional.of(callbacks)
            logger.regularInfo("incoming connections are now allowed")
            return Result.success(Unit)
        }

        /**
         * Stops accepting incoming connections from other nodes.
         *
         * Stopping incoming connections will not disconnect any currently connected Nodes; on the other hand,
         * any connection attempt from other Nodes will be refused.
         */
        fun stopIncomingConnections(){
            if(socketIO.isEmpty){
                logger.regularErr("cannot stop incoming connections: not connected to the Crolang Broker")
            } else if(incomingCrolangNodesCallbacks.isEmpty){
                logger.regularErr("cannot stop incoming connections: incoming connections already stopped")
            }
            incomingCrolangNodesCallbacks = Optional.empty()
            disconnectAllResponderNodesNotConnected()
            logger.regularInfo("incoming connections are now stopped")
        }

        /**
         * Returns a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
         * @return A map of all connected nodes.
         */
        fun getAllConnectedNodes(): Map<String, CrolangNode> {
            val initiators = SharedStore.brokerPeersContainer.responderNodes.values
                .filter { it.state == NodeState.CONNECTED }
                .map { it.crolangNode }
            val responders = SharedStore.brokerPeersContainer.initiatorNodes.values
                .filter { it.state == NodeState.CONNECTED }
                .map { it.crolangNode }
            return (initiators + responders).associateBy { it.id }
        }

        /**
         * Returns the connected node with the given id, if it exists.
         * @param id The id of the node to retrieve.
         * @return An Optional containing the connected node if found, or an empty Optional if not found.
         */
        fun getConnectedNode(id: String): Optional<CrolangNode> {
            val initiator = SharedStore.brokerPeersContainer.responderNodes[id]
            if(initiator != null && initiator.state == NodeState.CONNECTED){
                return Optional.of(initiator.crolangNode)
            }
            val responder = SharedStore.brokerPeersContainer.initiatorNodes[id]
            if(responder != null && responder.state == NodeState.CONNECTED){
                return Optional.of(responder.crolangNode)
            }
            return Optional.empty()
        }

        /**
         * Connects to multiple nodes asynchronously.
         *
         * This method initiates a connection attempt to the specified nodes and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * If you want to perform the connection synchronously, use [connectToMultipleNodesSync] instead.
         *
         * @param targets A map of node IDs and their corresponding [AsyncCrolangNodeCallbacks].
         * @param onConnectionAttemptConcluded A callback function that is called when the connection attempt is concluded;
         * the callback takes a map of node IDs and their connection results (the connected [CrolangNode] or
         * [ConnectionToNodeFailedReasonException] if the connection attempt failed).
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see AsyncCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        fun connectToMultipleNodesAsync(
            targets: Map<String, AsyncCrolangNodeCallbacks>,
            onConnectionAttemptConcluded: (result: Map<String, Result<CrolangNode>>) -> Unit = {}
        ): ConnectionAttempt {
            logger.regularInfo("attempting to connect to nodes ${targets.keys}")
            return ConnectionAttempt(targets, onConnectionAttemptConcluded)
        }

        /**
         * Connects to a single node asynchronously.
         *
         * This method initiates a connection attempt to the specified node and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * If you want to perform the connection synchronously, use [connectToSingleNodeSync] instead.
         *
         * @param id The ID of the node to connect to.
         * @param callbacks The [AsyncCrolangNodeCallbacks] to be used for the connection attempt, defaulting to empty callbacks.
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see AsyncCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        fun connectToSingleNodeAsync(
            id: String,
            callbacks: AsyncCrolangNodeCallbacks = AsyncCrolangNodeCallbacks()
        ): ConnectionAttempt {
            return connectToMultipleNodesAsync(
                mapOf(id to callbacks)
            ) { /* do nothing, already handled by connection success and failed handlers */ }
        }

        /**
         * Connects to a single node synchronously.
         *
         * This method blocks until the connection attempt is concluded.
         *
         * If you want to perform the connection asynchronously, use [connectToSingleNodeAsync] instead.
         *
         * @param id The ID of the node to connect to.
         * @param callbacks The [SyncCrolangNodeCallbacks] to be used for the connection attempt, defaulting to empty callbacks.
         * @return A Result containing the connected [CrolangNode], or a [ConnectionToNodeFailedReasonException]
         * if the connection attempt failed.
         *
         * @see SyncCrolangNodeCallbacks
         * @see CrolangNode
         * @see ConnectionToNodeFailedReasonException
         */
        fun connectToSingleNodeSync(
            id: String,
            callbacks: SyncCrolangNodeCallbacks = SyncCrolangNodeCallbacks()
        ): Result<CrolangNode> {
            return connectToMultipleNodesSync(mapOf(id to callbacks))[id]!!
        }

        /**
         * Connects to multiple nodes synchronously.
         *
         * This method blocks until the connection attempt is concluded.
         *
         * If you want to perform the connection asynchronously, use [connectToMultipleNodesAsync] instead.
         *
         * @param targets A map of node IDs and their corresponding [SyncCrolangNodeCallbacks].
         * @return A map of node IDs and their connection results (the connected [CrolangNode] or
         * a [ConnectionToNodeFailedReasonException] if the connection attempt failed).
         *
         * @see SyncCrolangNodeCallbacks
         * @see CrolangNode
         * @see ConnectionToNodeFailedReasonException
         */
        fun connectToMultipleNodesSync(
            targets: Map<String, SyncCrolangNodeCallbacks>
        ): Map<String, Result<CrolangNode>> {
            val nodesConnectionAsyncAwaitGuard = AwaitAsyncEventGuard(
                "nodes connection async: ${targets.keys.joinToString(",")}"
            )
            nodesConnectionAsyncAwaitGuard.startNewCountdown()

            lateinit var result: Map<String, Result<CrolangNode>>

            connectToMultipleNodesAsync(
                targets.mapValues { AsyncCrolangNodeCallbacks(
                    onConnectionSuccess = { /* do nothing, info already contained in result */ },
                    onConnectionFailed = { _, _ -> /* do nothing, info already contained in result  */ },
                    onDisconnection = it.value.onDisconnection,
                    onNewMsg = it.value.onNewMsg
                ) }
            ) {
                result = it
                nodesConnectionAsyncAwaitGuard.stepDown()
            }

            nodesConnectionAsyncAwaitGuard.await()
            return result
        }
    }

    /**
     * The Java interface for CrolangP2P.
     */
    object Java {

        /**
         * Checks if the local node is connected to the Crolang Broker.
         * This method returns true if the socket is present and connected, false otherwise.
         *
         * @return true if the local node is connected to the Broker, false otherwise.
         */
        @JvmStatic
        fun isLocalNodeConnectedToBroker(): Boolean {
            return Kotlin.isLocalNodeConnectedToBroker()
        }

        /**
         * Checks if the provided remote node is connected to the Crolang Broker.
         * This method returns true if the node is connected, false otherwise. If the local Node is not connected to the Broker,
         * an exception is thrown.
         *
         * @param id The ID of the remote node to check.
         * @return true if the node is connected, false otherwise.
         * @throws RemoteNodesConnectionStatusCheckException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(RemoteNodesConnectionStatusCheckException::class)
        fun isRemoteNodeConnectedToBroker(id: String): Boolean {
            return nonVoidKotlinResultCall { Kotlin.isRemoteNodeConnectedToBroker(id) }
        }

        /**
         * Checks if the provided set of remote nodes are connected to the Crolang Broker.
         * This method returns a map where the key is the node ID and the value is true if the Node is connected,
         * false otherwise. If the local Node is not connected to the Broker, an exception is thrown.
         *
         * @param ids The set of remote node IDs to check.
         * @return A map of node IDs and their connection status.
         * @throws RemoteNodesConnectionStatusCheckException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(RemoteNodesConnectionStatusCheckException::class)
        fun areRemoteNodesConnectedToBroker(ids: Set<String>): Map<String, Boolean> {
            return nonVoidKotlinResultCall { Kotlin.areRemoteNodesConnectedToBroker(ids) }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID.
         * This method initiates a connection attempt to the Broker and handles the connection process.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @throws ConnectToBrokerException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(ConnectToBrokerException::class)
        fun connectToBroker(brokerAddr: String, nodeId: String) {
            voidKotlinResultCall { Kotlin.connectToBroker(brokerAddr, nodeId) }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID.
         * This method initiates a connection attempt to the Broker and handles the connection process.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param additionalParameters Additional parameters for the connection, including logging options, settings and lifecycle callbacks.
         * @throws ConnectToBrokerException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(ConnectToBrokerException::class)
        fun connectToBroker(
            brokerAddr: String, nodeId: String, additionalParameters: BrokerConnectionAdditionalParameters
        ) {
            voidKotlinResultCall { Kotlin.connectToBroker(
                brokerAddr,
                nodeId,
                additionalParameters = additionalParameters
            ) }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID.
         * This method initiates a connection attempt to the Broker and handles the connection process.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onNewSocketMsg Map of callbacks for handling direct messages received via the Broker's WebSocket relay.
         * @throws ConnectToBrokerException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(ConnectToBrokerException::class)
        fun connectToBroker(
            brokerAddr: String, nodeId: String, onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>
        ){
            voidKotlinResultCall { Kotlin.connectToBroker(
                brokerAddr,
                nodeId,
                onNewSocketMsg = onNewSocketMsg
            ) }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID, and optional authentication data.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         * @throws ConnectToBrokerException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(ConnectToBrokerException::class)
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onConnectionAttemptData: String
        ) {
            voidKotlinResultCall { Kotlin.connectToBroker(brokerAddr, nodeId, onConnectionAttemptData) }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID, with optional authentication data and message callbacks.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         * @param onNewSocketMsg Map of callbacks for handling direct messages received via the Broker's WebSocket relay (optional).
         * @throws ConnectToBrokerException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(ConnectToBrokerException::class)
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onConnectionAttemptData: String,
            onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>
        ) {
            voidKotlinResultCall { Kotlin.connectToBroker(brokerAddr, nodeId, onConnectionAttemptData, onNewSocketMsg) }
        }

        /**
         * Connects to the Crolang Broker using the provided broker address and Node ID, with optional authentication data, message callbacks, and additional parameters.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         * @param onNewSocketMsg Map of callbacks for handling direct messages received via the Broker's WebSocket relay (optional).
         * @param additionalParameters Additional parameters for the connection, including logging options, settings and lifecycle callbacks.
         * @throws ConnectToBrokerException if the connection attempt fails.
         */
        @JvmStatic
        @Throws(ConnectToBrokerException::class)
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onConnectionAttemptData: String,
            onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>,
            additionalParameters: BrokerConnectionAdditionalParameters
        ) {
            voidKotlinResultCall {
                Kotlin.connectToBroker(
                    brokerAddr,
                    nodeId,
                    onConnectionAttemptData,
                    onNewSocketMsg,
                    additionalParameters
                )
            }
        }

        /**
         * Disconnects from the Crolang Broker.
         *
         * Connected Nodes will NOT be disconnected; on the other hand, the connection process of Nodes that are still
         * attempting a connection will be forcefully stopped.
         */
        @JvmStatic
        fun disconnectFromBroker() {
            Kotlin.disconnectFromBroker()
        }

        /**
         * Checks if incoming connections from other Nodes are allowed.
         * This method returns true if incoming connections are allowed and the socket is connected.
         *
         * @return true if incoming connections are allowed, false otherwise.
         */
        @JvmStatic
        fun areIncomingConnectionsAllowed(): Boolean {
            return Kotlin.areIncomingConnectionsAllowed()
        }

        /**
         * Allows incoming connections from other nodes.
         *
         * @param callbacks The callbacks to be used for incoming connections.
         * @see IncomingCrolangNodesCallbacks
         * @throws AllowIncomingConnectionsException
         */
        @JvmStatic
        fun allowIncomingConnections(callbacks: IncomingCrolangNodesCallbacks) {
            voidKotlinResultCall { Kotlin.allowIncomingConnections(callbacks) }
        }

        /**
         * Allows incoming connections from other nodes.
         *
         * @throws AllowIncomingConnectionsException
         */
        @JvmStatic
        fun allowIncomingConnections() {
            voidKotlinResultCall { Kotlin.allowIncomingConnections() }
        }

        /**
         * Stops accepting incoming connections from other nodes.
         *
         * Stopping incoming connections will not disconnect any currently connected Nodes; on the other hand,
         * any connection attempt from other Nodes will be refused.
         */
        @JvmStatic
        fun stopIncomingConnections() {
            Kotlin.stopIncomingConnections()
        }

        /**
         * Returns a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
         * @return A map of all connected nodes.
         */
        @JvmStatic
        fun getAllConnectedNodes(): Map<String, CrolangNode> {
            return Kotlin.getAllConnectedNodes()
        }

        /**
         * Returns the connected node with the given id, if it exists.
         * @param id The id of the node to retrieve.
         * @return An Optional containing the connected node if found, or an empty Optional if not found.
         */
        @JvmStatic
        fun getConnectedNode(id: String): Optional<CrolangNode> {
            return Kotlin.getConnectedNode(id)
        }

        /**
         * Connects to multiple nodes asynchronously.
         *
         * This method initiates a connection attempt to the specified nodes and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * If you want to perform the connection synchronously, use [connectToMultipleNodesSync] instead.
         *
         * @param targets A map of node IDs and their corresponding [AsyncCrolangNodeCallbacks].
         * @param onConnectionAttemptConcluded A callback function that is called when the connection attempt is concluded;
         * the callback takes a map of node IDs and their corresponding [CrolangNodeConnectionResult]
         * (the connected [CrolangNode] or [ConnectionToNodeFailedReasonException] if the connection attempt failed).
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see AsyncCrolangNodeCallbacks
         * @see ConnectionAttempt
         * @see CrolangNodeConnectionResult
         */
        @JvmStatic
        fun connectToMultipleNodesAsync(
            targets: Map<String, AsyncCrolangNodeCallbacks>,
            onConnectionAttemptConcluded: (result: Map<String, CrolangNodeConnectionResult>) -> Unit
        ): ConnectionAttempt {
            return Kotlin.connectToMultipleNodesAsync(targets){
                onConnectionAttemptConcluded(it.mapValues { (_, result) ->
                    result.fold(
                        onSuccess = { r -> CrolangNodeConnectionResult(Optional.of(r), Optional.empty()) },
                        onFailure = { e ->
                            CrolangNodeConnectionResult(
                                Optional.empty(), Optional.of((e as ConnectionToNodeFailedReasonException).reason)
                            )
                        }
                    )
                })
            }
        }

        /**
         * Connects to multiple nodes asynchronously.
         *
         * This method initiates a connection attempt to the specified nodes and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * If you want to perform the connection synchronously, use [connectToMultipleNodesSync] instead.
         *
         * @param targets A map of node IDs and their corresponding [AsyncCrolangNodeCallbacks].
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see AsyncCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        @JvmStatic
        fun connectToMultipleNodesAsync(targets: Map<String, AsyncCrolangNodeCallbacks>): ConnectionAttempt {
            return Kotlin.connectToMultipleNodesAsync(targets)
        }

        /**
         * Connects to a single node asynchronously.
         *
         * This method initiates a connection attempt to the specified node and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * If you want to perform the connection synchronously, use [connectToSingleNodeSync] instead.
         *
         * @param id The ID of the node to connect to.
         * @param callbacks The [AsyncCrolangNodeCallbacks] to be used for the connection attempt.
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see AsyncCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        @JvmStatic
        fun connectToSingleNodeAsync(id: String, callbacks: AsyncCrolangNodeCallbacks): ConnectionAttempt {
            return Kotlin.connectToSingleNodeAsync(id, callbacks)
        }

        /**
         * Connects to a single node asynchronously.
         *
         * This method initiates a connection attempt to the specified node and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * If you want to perform the connection synchronously, use [connectToSingleNodeSync] instead.
         *
         * @param id The ID of the node to connect to.
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see AsyncCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        @JvmStatic
        fun connectToSingleNodeAsync(id: String): ConnectionAttempt {
            return Kotlin.connectToSingleNodeAsync(id)
        }

        /**
         * Connects to a single node synchronously.
         *
         * This method blocks until the connection attempt is concluded.
         *
         * If you want to perform the connection asynchronously, use [connectToSingleNodeAsync] instead.
         *
         * @param id The ID of the node to connect to.
         * @param callbacks The [SyncCrolangNodeCallbacks] to be used for the connection attempt, defaulting to empty callbacks.
         * @return The connected [CrolangNode]
         * @throws ConnectionToNodeFailedReasonException if the connection attempt failed.
         *
         * @see SyncCrolangNodeCallbacks
         * @see CrolangNode
         * @see ConnectionToNodeFailedReasonException
         */
        @JvmStatic
        @Throws(ConnectionToNodeFailedReasonException::class)
        fun connectToSingleNodeSync(id: String, callbacks: SyncCrolangNodeCallbacks): CrolangNode {
            return nonVoidKotlinResultCall { Kotlin.connectToSingleNodeSync(id, callbacks) }
        }

        /**
         * Connects to a single node synchronously.
         *
         * This method blocks until the connection attempt is concluded.
         *
         * If you want to perform the connection asynchronously, use [connectToSingleNodeAsync] instead.
         *
         * @param id The ID of the node to connect to.
         * @return The connected [CrolangNode]
         * @throws ConnectionToNodeFailedReasonException if the connection attempt failed.
         *
         * @see SyncCrolangNodeCallbacks
         * @see CrolangNode
         * @see ConnectionToNodeFailedReasonException
         */
        @JvmStatic
        @Throws(ConnectionToNodeFailedReasonException::class)
        fun connectToSingleNodeSync(id: String): CrolangNode {
            return nonVoidKotlinResultCall { Kotlin.connectToSingleNodeSync(id) }
        }

        /**
         * Connects to multiple nodes synchronously.
         *
         * This method blocks until the connection attempt is concluded.
         *
         * If you want to perform the connection asynchronously, use [connectToMultipleNodesAsync] instead.
         *
         * @param targets A map of node IDs and their corresponding [SyncCrolangNodeCallbacks].
         * @return A map of node IDs and their [CrolangNodeConnectionResult] (the connected [CrolangNode] or
         * a [P2PConnectionFailedReason] if the connection attempt failed).
         *
         * @see SyncCrolangNodeCallbacks
         * @see CrolangNode
         * @see P2PConnectionFailedReason
         * @see CrolangNodeConnectionResult
         */
        @JvmStatic
        fun connectToMultipleNodesSync(
            targets: Map<String, SyncCrolangNodeCallbacks>
        ): Map<String, CrolangNodeConnectionResult> {
            return Kotlin.connectToMultipleNodesSync(targets).mapValues { (_, result) ->
                result.fold(
                    onSuccess = { CrolangNodeConnectionResult(Optional.of(it), Optional.empty()) },
                    onFailure = {
                        CrolangNodeConnectionResult(
                            Optional.empty(), Optional.of((it as ConnectionToNodeFailedReasonException).reason)
                        )
                    }
                )
            }
        }

        /**
         * Sends a message to a remote node via the Broker using WebSocket relay.
         *
         * @param id The ID of the remote node to send the message to.
         * @param channel The channel on which to send the message.
         * @param msg The message to send.
         * @throws SendSocketMsgException if the message could not be sent (e.g., not connected to the Broker, empty channel or id, or trying to send a message to self).
         *
         * @see SendSocketMsgException
         */
        @JvmStatic
        @Throws(SendSocketMsgException::class)
        fun sendSocketMsg(id: String, channel: String, msg: String) {
            return nonVoidKotlinResultCall { Kotlin.sendSocketMsg(id, channel, msg) }
        }

        /**
         * Sends an empty message to a remote node via the Broker using WebSocket relay.
         *
         * @param id The ID of the remote node to send the message to.
         * @param channel The channel on which to send the message.
         * @throws SendSocketMsgException if the message could not be sent (e.g., not connected to the Broker, empty channel or id, or trying to send a message to self).
         *
         * @see SendSocketMsgException
         */
        @JvmStatic
        @Throws(SendSocketMsgException::class)
        fun sendSocketMsg(id: String, channel: String) {
            return nonVoidKotlinResultCall { Kotlin.sendSocketMsg(id, channel, "") }
        }

        private fun voidKotlinResultCall(call: () -> Result<Unit>) {
            call().onFailure { throw it }
        }

        private fun <T> nonVoidKotlinResultCall(call: () -> Result<T>): T {
            val result = call()
            result.onFailure { throw it }
            return result.getOrThrow()
        }

    }

}
