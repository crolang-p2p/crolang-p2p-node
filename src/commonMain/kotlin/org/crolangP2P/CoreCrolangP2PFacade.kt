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

import internal.broker.BrokerSocketCreator.createSocket
import internal.broker.OnConnectionToBrokerSettings
import internal.dependencies.DependenciesInjection
import internal.events.data.AreNodesConnectedToBrokerMsg
import internal.events.data.AreNodesConnectedToBrokerMsgResponse
import internal.events.data.ParsableSocketDirectMsg
import internal.events.data.SocketDirectMsg
import internal.events.data.abstractions.SocketMsgType.Companion.ARE_NODES_CONNECTED_TO_BROKER
import internal.events.data.abstractions.SocketMsgType.Companion.SOCKET_MSG_EXCHANGE
import internal.events.data.abstractions.SocketResponses
import internal.node.NodeState
import internal.utils.AwaitAsyncEventGuard
import internal.utils.CrolangLogger
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
import internal.utils.SharedStore.socket
import org.crolangP2P.exceptions.AllowIncomingConnectionsException
import org.crolangP2P.exceptions.ConnectToBrokerException
import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException
import org.crolangP2P.exceptions.RemoteNodesConnectionStatusCheckException
import org.crolangP2P.exceptions.SendSocketMsgException

/**
 * Core facade for the CroLang P2P networking library.
 * 
 * This class provides the main API for establishing peer-to-peer connections,
 * communicating with brokers, and managing the P2P network lifecycle.
 * It serves as the primary entry point for all CroLang P2P operations.
 * 
 * @param dependencies Platform-specific dependency injection container
 */
open class CoreCrolangP2PFacade(dependencies: DependenciesInjection) {

    init {
        SharedStore.dependencies = dependencies
    }

    /**
     * Checks if the local node is connected to the Crolang Broker.
     * This method returns true if the socket is present and connected, false otherwise.
     *
     * @return true if the local node is connected to the Broker, false otherwise.
     */
    fun isLocalNodeConnectedToBroker(): Boolean {
        return socket != null && socket!!.connected()
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
        val guard = AwaitAsyncEventGuard(SharedStore.dependencies!!.uuidGenerator.generateRandomUUID())
        guard.startNewCountdown()
        socket!!.emit(
            ARE_NODES_CONNECTED_TO_BROKER,
            parser.toJson(AreNodesConnectedToBrokerMsg(ids))
        ) { args ->
            if (args.size != 1) {
                guard.stepDown()
                return@emit
            }
            val res = parser.fromJson<AreNodesConnectedToBrokerMsgResponse>(args[0].toString())
            if(res != null){
                if (res.results != null && res.results!!.all { it.id != null && it.connected != null }) {
                    result = res.results!!.associate { it.id!! to it.connected!! }
                }
                guard.stepDown()
            } else {
                guard.stepDown()
            }
        }
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
        val guard = AwaitAsyncEventGuard(SharedStore.dependencies!!.uuidGenerator.generateRandomUUID())
        guard.startNewCountdown()
        socket!!.emit(
            SOCKET_MSG_EXCHANGE,
            parser.toJson(ParsableSocketDirectMsg.fromChecked(SocketDirectMsg(localNodeId, id, channel, msg ?: "")))
        ) { args ->
            if (args.size != 1 || args[0] !is String) {
                err = SocketResponses.ERROR
                guard.stepDown()
                return@emit
            }
            val response = args[0] as String
            if (SocketResponses.ALL.contains(response)) {
                if (!SocketResponses.isOk(response)) {
                    err = response
                }
            } else {
                err = SocketResponses.ERROR
            }
            guard.stepDown()
        }
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
        if(socket != null){
            logger.regularErr("trying to connect to Broker while already connected")
            return Result.failure(ConnectToBrokerException.LocalClientAlreadyConnectedToBroker)
        }
        onNewSocketMsgCallbacks = onNewSocketMsg
        brokerLifecycleCallbacks = additionalParameters.lifecycleCallbacks
        logger = CrolangLogger(additionalParameters.logging)
        settings = additionalParameters.settings
        logger.regularInfo("initiating Broker connection attempt")
        localNodeId = nodeId
        onConnectionToBrokerSettings = OnConnectionToBrokerSettings(brokerAddr, onConnectionAttemptData)
        val socket = createSocket()
        SharedStore.socket = socket
        SharedStore.brokerConnectionHelper.connectionToBrokerGuard.startNewCountdown()
        socket.connect()
        SharedStore.brokerConnectionHelper.connectionToBrokerGuard.await()
        val connectionToBrokerError = SharedStore.brokerConnectionHelper.connectionToBrokerErrorReason
        if(connectionToBrokerError != null){
            logger.regularErr("failed to connect to Broker")
            socket.disconnect()
            onConnectionToBrokerSettings = null
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
        if(socket != null){
            logger.regularInfo("already disconnected from Broker")
            return
        }
        SharedStore.brokerConnectionHelper.disconnectionFromBrokerGuard.startNewCountdown()
        socket!!.disconnect()
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
        return SharedStore.areIncomingConnectionsAllowed()
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
        if (socket == null || !socket!!.connected()) {
            logger.regularErr("cannot allow incoming connections: not connected to the Crolang Broker")
            return Result.failure(AllowIncomingConnectionsException.NotConnectedToBroker)
        } else if (incomingCrolangNodesCallbacks != null) {
            logger.regularErr("cannot allow incoming connections: incoming connections already allowed")
            return Result.failure(AllowIncomingConnectionsException.IncomingConnectionsAlreadyAllowed)
        }
        incomingCrolangNodesCallbacks = callbacks
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
        if(socket == null){
            logger.regularErr("cannot stop incoming connections: not connected to the Crolang Broker")
        } else if(incomingCrolangNodesCallbacks == null){
            logger.regularErr("cannot stop incoming connections: incoming connections already stopped")
        }
        incomingCrolangNodesCallbacks = null
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
     * @return The connected node if found, or null if not found.
     */
    fun getConnectedNode(id: String): CrolangNode? {
        val initiator = SharedStore.brokerPeersContainer.responderNodes[id]
        if(initiator != null && initiator.state == NodeState.CONNECTED){
            return initiator.crolangNode
        }
        val responder = SharedStore.brokerPeersContainer.initiatorNodes[id]
        if(responder != null && responder.state == NodeState.CONNECTED){
            return responder.crolangNode
        }
        return null
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
