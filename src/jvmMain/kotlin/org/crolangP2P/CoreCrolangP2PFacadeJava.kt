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

import org.crolangP2P.exceptions.AllowIncomingConnectionsException
import org.crolangP2P.exceptions.ConnectToBrokerException
import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException
import org.crolangP2P.exceptions.RemoteNodesConnectionStatusCheckException
import org.crolangP2P.exceptions.SendSocketMsgException
import java.util.*

/**
 * The Java interface for CrolangP2P.
 */
class CoreCrolangP2PFacadeJava(private val kotlinFacade: CoreCrolangP2PFacadeKotlin) {

    /**
     * Checks if the local node is connected to the Crolang Broker.
     * This method returns true if the socket is present and connected, false otherwise.
     *
     * @return true if the local node is connected to the Broker, false otherwise.
     */
    fun isLocalNodeConnectedToBroker(): Boolean {
        return kotlinFacade.isLocalNodeConnectedToBroker()
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
    @Throws(RemoteNodesConnectionStatusCheckException::class)
    fun isRemoteNodeConnectedToBroker(id: String): Boolean {
        return nonVoidKotlinResultCall { kotlinFacade.isRemoteNodeConnectedToBroker(id) }
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
    @Throws(RemoteNodesConnectionStatusCheckException::class)
    fun areRemoteNodesConnectedToBroker(ids: Set<String>): Map<String, Boolean> {
        return nonVoidKotlinResultCall { kotlinFacade.areRemoteNodesConnectedToBroker(ids) }
    }

    /**
     * Connects to the Crolang Broker using the provided broker address and Node ID.
     * This method initiates a connection attempt to the Broker and handles the connection process.
     *
     * @param brokerAddr The address of the Broker to connect to.
     * @param nodeId The ID of the local node.
     * @throws ConnectToBrokerException if the connection attempt fails.
     */
    @Throws(ConnectToBrokerException::class)
    fun connectToBroker(brokerAddr: String, nodeId: String) {
        voidKotlinResultCall { kotlinFacade.connectToBroker(brokerAddr, nodeId) }
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
    @Throws(ConnectToBrokerException::class)
    fun connectToBroker(
        brokerAddr: String, nodeId: String, additionalParameters: BrokerConnectionAdditionalParameters
    ) {
        voidKotlinResultCall { kotlinFacade.connectToBroker(
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
    @Throws(ConnectToBrokerException::class)
    fun connectToBroker(
        brokerAddr: String, nodeId: String, onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>
    ){
        voidKotlinResultCall { kotlinFacade.connectToBroker(
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
    @Throws(ConnectToBrokerException::class)
    fun connectToBroker(
        brokerAddr: String,
        nodeId: String,
        onConnectionAttemptData: String
    ) {
        voidKotlinResultCall { kotlinFacade.connectToBroker(brokerAddr, nodeId, onConnectionAttemptData) }
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
    @Throws(ConnectToBrokerException::class)
    fun connectToBroker(
        brokerAddr: String,
        nodeId: String,
        onConnectionAttemptData: String,
        onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>
    ) {
        voidKotlinResultCall { kotlinFacade.connectToBroker(brokerAddr, nodeId, onConnectionAttemptData, onNewSocketMsg) }
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
    @Throws(ConnectToBrokerException::class)
    fun connectToBroker(
        brokerAddr: String,
        nodeId: String,
        onConnectionAttemptData: String,
        onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>,
        additionalParameters: BrokerConnectionAdditionalParameters
    ) {
        voidKotlinResultCall {
            kotlinFacade.connectToBroker(
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
    fun disconnectFromBroker() {
        kotlinFacade.disconnectFromBroker()
    }

    /**
     * Checks if incoming connections from other Nodes are allowed.
     * This method returns true if incoming connections are allowed and the socket is connected.
     *
     * @return true if incoming connections are allowed, false otherwise.
     */
    fun areIncomingConnectionsAllowed(): Boolean {
        return kotlinFacade.areIncomingConnectionsAllowed()
    }

    /**
     * Allows incoming connections from other nodes.
     *
     * @param callbacks The callbacks to be used for incoming connections.
     * @see IncomingCrolangNodesCallbacks
     * @throws AllowIncomingConnectionsException
     */
    fun allowIncomingConnections(callbacks: IncomingCrolangNodesCallbacks) {
        voidKotlinResultCall { kotlinFacade.allowIncomingConnections(callbacks) }
    }

    /**
     * Allows incoming connections from other nodes.
     *
     * @throws AllowIncomingConnectionsException
     */
    fun allowIncomingConnections() {
        voidKotlinResultCall { kotlinFacade.allowIncomingConnections() }
    }

    /**
     * Stops accepting incoming connections from other nodes.
     *
     * Stopping incoming connections will not disconnect any currently connected Nodes; on the other hand,
     * any connection attempt from other Nodes will be refused.
     */
    fun stopIncomingConnections() {
        kotlinFacade.stopIncomingConnections()
    }

    /**
     * Returns a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
     * @return A map of all connected nodes.
     */
    fun getAllConnectedNodes(): Map<String, CrolangNode> {
        return kotlinFacade.getAllConnectedNodes()
    }

    /**
     * Returns the connected node with the given id, if it exists.
     * @param id The id of the node to retrieve.
     * @return An Optional containing the connected node if found, or an empty Optional if not found.
     */
    fun getConnectedNode(id: String): Optional<CrolangNode> {
        return kotlinFacade.getConnectedNode(id)
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
    fun connectToMultipleNodesAsync(
        targets: Map<String, AsyncCrolangNodeCallbacks>,
        onConnectionAttemptConcluded: java.util.function.Consumer<Map<String, CrolangNodeConnectionResult>>
    ): ConnectionAttempt {
        return kotlinFacade.connectToMultipleNodesAsync(targets) {
            onConnectionAttemptConcluded.accept(it.mapValues { (_, result) ->
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
    fun connectToMultipleNodesAsync(targets: Map<String, AsyncCrolangNodeCallbacks>): ConnectionAttempt {
        return kotlinFacade.connectToMultipleNodesAsync(targets)
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
    fun connectToSingleNodeAsync(id: String, callbacks: AsyncCrolangNodeCallbacks): ConnectionAttempt {
        return kotlinFacade.connectToSingleNodeAsync(id, callbacks)
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
    fun connectToSingleNodeAsync(id: String): ConnectionAttempt {
        return kotlinFacade.connectToSingleNodeAsync(id)
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
    @Throws(ConnectionToNodeFailedReasonException::class)
    fun connectToSingleNodeSync(id: String, callbacks: SyncCrolangNodeCallbacks): CrolangNode {
        return nonVoidKotlinResultCall { kotlinFacade.connectToSingleNodeSync(id, callbacks) }
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
    @Throws(ConnectionToNodeFailedReasonException::class)
    fun connectToSingleNodeSync(id: String): CrolangNode {
        return nonVoidKotlinResultCall { kotlinFacade.connectToSingleNodeSync(id) }
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
    fun connectToMultipleNodesSync(
        targets: Map<String, SyncCrolangNodeCallbacks>
    ): Map<String, CrolangNodeConnectionResult> {
        return kotlinFacade.connectToMultipleNodesSync(targets).mapValues { (_, result) ->
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
    @Throws(SendSocketMsgException::class)
    fun sendSocketMsg(id: String, channel: String, msg: String) {
        return nonVoidKotlinResultCall { kotlinFacade.sendSocketMsg(id, channel, msg) }
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
    @Throws(SendSocketMsgException::class)
    fun sendSocketMsg(id: String, channel: String) {
        return nonVoidKotlinResultCall { kotlinFacade.sendSocketMsg(id, channel, "") }
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
