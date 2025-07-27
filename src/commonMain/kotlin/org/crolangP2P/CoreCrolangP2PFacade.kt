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

import internal.dependencies.DependenciesInjection
import internal.dependencies.event_loop.EventLoop
import internal.events.OnAllowIncomingConnectionsRequested
import internal.events.OnAreIncomingConnectionsAllowedRequested
import internal.events.OnAreRemoteNodesConnectedToBrokerRequested
import internal.events.OnConnectToBrokerRequested
import internal.events.OnDisconnectFromBrokerRequested
import internal.events.OnGetAllConnectedNodesRequested
import internal.events.OnGetConnectedNodeRequested
import internal.events.OnIsLocalNodeConnectedToBrokerRequested
import internal.events.OnSendSocketMsgRequested
import internal.events.OnStopIncomingConnectionsRequested
import internal.utils.SharedStore
import internal.utils.SharedStore.logger
import org.crolangP2P.errors.AllowIncomingConnectionsError
import org.crolangP2P.errors.ConnectionToBrokerError
import org.crolangP2P.errors.DisconnectionFromBrokerError
import org.crolangP2P.errors.P2PConnectionFailedError
import org.crolangP2P.errors.RemoteNodesConnectionStatusCheckError
import org.crolangP2P.errors.SendSocketMsgError

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

    private val eventLoop: EventLoop

    init {
        SharedStore.dependencies = dependencies
        eventLoop = dependencies.eventLoop
    }

    /**
     * Checks if the local node is connected to the Crolang Broker.
     *
     * @param onResult callback that returns true if the local node is connected to the Broker, false otherwise.
     */
    fun isLocalNodeConnectedToBroker(onResult: (isConnected: Boolean) -> Unit) {
        eventLoop.postEvent(OnIsLocalNodeConnectedToBrokerRequested(onResult))
    }

    /**
     * Checks if the provided remote node is connected to the Crolang Broker.
     *
     * @param id The ID of the remote node to check.
     * @param onResult callback returning a flag containing true if the node is connected, false otherwise.
     * @param onError callback handing errors while performing the operation; empty by default.
     */
    fun isRemoteNodeConnectedToBroker(
        id: String,
        onResult: (isRemoteNodeConnected: Boolean) -> Unit,
        onError: (err: RemoteNodesConnectionStatusCheckError) -> Unit = { _ -> }
    ) {
        return areRemoteNodesConnectedToBroker(
            setOf(id),
            onResult = {
                val result = it[id]
                if (result != null) {
                    onResult(result)
                } else {
                    onError(RemoteNodesConnectionStatusCheckError.UNKNOWN_ERROR)
                }
            },
            onError = { onError(it) }
        )
    }

    /**
     * Checks if the provided set of remote nodes are connected to the Crolang Broker.
     *
     * @param ids The set of remote node IDs to check.
     * @param onResult callback returning the map of node IDs and their connection status.
     * @param onError callback handing errors while performing the operation; empty by default.
     */
    fun areRemoteNodesConnectedToBroker(
        ids: Set<String>,
        onResult: (resultMap: Map<String, Boolean>) -> Unit,
        onError: (err: RemoteNodesConnectionStatusCheckError) -> Unit = { _ -> }
    ) {
        eventLoop.postEvent(OnAreRemoteNodesConnectedToBrokerRequested(ids, onResult, onError))
    }

    /**
     * Sends a message to a remote node via the Broker using WebSocket relay.
     *
     * @param id The ID of the remote node to send the message to.
     * @param channel The channel on which to send the message.
     * @param msg The message to send (optional). If not provided, an empty string will be sent.
     * @param onMsgSent Callback invoked when the WebSocket message is successfully sent to the Broker that will relay it to the target Node; empty by default.
     * @param onError Callback invoked when the WebSocket message could not be delivered to the Broker and, consequently, to the target Node; empty by default.
     * @see SendSocketMsgError
     */
    fun sendSocketMsg(
        id: String,
        channel: String,
        msg: String?,
        onMsgSent: () -> Unit = {},
        onError: (err: SendSocketMsgError) -> Unit = {}
    ) {
        eventLoop.postEvent(OnSendSocketMsgRequested(id, channel, msg, onMsgSent, onError))
    }

    /**
     * Connects to the Crolang Broker using the provided broker address and Node ID.
     * This method initiates a connection attempt to the Broker and handles the connection process.
     *
     * @param brokerAddr The address of the Broker to connect to.
     * @param nodeId The ID of the local node.
     * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
     * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
     * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
     * @param onNewSocketMsg Optional Map of callbacks for handling direct messages received via the Broker's WebSocket relay.
     * @param additionalParameters Optional additional parameters for the connection, including logging options, settings and lifecycle callbacks.
     *
     * @see ConnectionToBrokerError
     */
    fun connectToBroker(
        brokerAddr: String,
        nodeId: String,
        onSuccess: () -> Unit,
        onError: (err: ConnectionToBrokerError) -> Unit = { _ -> },
        onConnectionAttemptData: String = "",
        onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit> = emptyMap(),
        additionalParameters: BrokerConnectionAdditionalParameters = BrokerConnectionAdditionalParameters()
    ) {
        eventLoop.postEvent(
            OnConnectToBrokerRequested(
                brokerAddr, nodeId, onSuccess, onError, onConnectionAttemptData, onNewSocketMsg, additionalParameters
            )
        )
    }

    /**
     * Disconnects from the Crolang Broker.
     *
     * Connected Nodes will NOT be disconnected; on the other hand, the connection process of Nodes that are still
     * attempting a connection will be forcefully stopped.
     *
     * @param onSuccess Callback invoked when the disconnection from the Broker is successfully completed
     * @param onError Callback invoked when there was a problem performing the disconnection from the Broker
     *
     * @see DisconnectionFromBrokerError
     */
    fun disconnectFromBroker(
        onSuccess: () -> Unit,
        onError: (err: DisconnectionFromBrokerError) -> Unit
    ) {
        eventLoop.postEvent(OnDisconnectFromBrokerRequested(onSuccess, onError))
    }

    /**
     * Checks if incoming connections from other Nodes are allowed.
     *
     * @param onResult callback returning true if incoming connections are allowed, false otherwise.
     */
    fun areIncomingConnectionsAllowed(onResult: (areAllowed: Boolean) -> Unit) {
        eventLoop.postEvent(OnAreIncomingConnectionsAllowedRequested(onResult))
    }

    /**
     * Allows incoming connections from other nodes.
     *
     * @param callbacks The callbacks to be used for incoming connections.
     * @return A Result indicating success or failure.
     * @param onSuccess Callback invoked when incoming connections are successfully allowed; empty by default.
     * @param onError Callback invoked when an error occurs while allowing incoming connections; empty by default.
     * @see IncomingCrolangNodesCallbacks
     * @see AllowIncomingConnectionsError
     */
    fun allowIncomingConnections(
        callbacks: IncomingCrolangNodesCallbacks = IncomingCrolangNodesCallbacks(),
        onSuccess: () -> Unit = {},
        onError: (err: AllowIncomingConnectionsError) -> Unit = { _ -> }
    ) {
        eventLoop.postEvent(OnAllowIncomingConnectionsRequested(callbacks, onSuccess, onError))
    }

    /**
     * Stops accepting incoming connections from other nodes.
     *
     * Stopping incoming connections will not disconnect any currently connected Nodes; on the other hand,
     * any connection attempt from other Nodes will be refused.
     *
     * @param onStopped Callback invoked when incoming connections are successfully stopped.
     */
    fun stopIncomingConnections(onStopped: () -> Unit){
        eventLoop.postEvent(OnStopIncomingConnectionsRequested(onStopped))
    }

    /**
     * @param onResult A callback returning a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
     */
    fun getAllConnectedNodes(onResult: (connectedNodes: Map<String, CrolangNode>) -> Unit) {
        eventLoop.postEvent(OnGetAllConnectedNodesRequested(onResult))
    }

    /**
     * @param id The id of the node to retrieve.
     * @param onResult Callback that returns the connected node if found, null otherwise.
     */
    fun getConnectedNode(id: String, onResult: (CrolangNode?) -> Unit) {
        eventLoop.postEvent(OnGetConnectedNodeRequested(id, onResult))
    }

    /**
     * Connects to multiple nodes asynchronously.
     *
     * This method initiates a connection attempt to the specified nodes and returns a [ConnectionAttempt] object
     * that can be used to manage the connection process.
     *
     * @param targets A map of node IDs and their corresponding [OutgoingCrolangNodeCallbacks].
     * @param onConnectionAttemptConcluded A callback function that is called when the connection attempt is concluded;
     * the callback returns two maps: one with the successfully connected nodes and another with the errors encountered during the connection attempt.
     * @return A [ConnectionAttempt] object representing the connection attempt.
     *
     * @see OutgoingCrolangNodeCallbacks
     * @see ConnectionAttempt
     * @see P2PConnectionFailedError
     */
    fun connectToMultipleNodes(
        targets: Map<String, OutgoingCrolangNodeCallbacks>,
        onConnectionAttemptConcluded: (connected: Map<String, CrolangNode>, errors: Map<String, P2PConnectionFailedError>) -> Unit = { _, _ -> }
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
     * @param id The ID of the node to connect to.
     * @param callbacks The [OutgoingCrolangNodeCallbacks] to be used for the connection attempt, defaulting to empty callbacks.
     * @return A [ConnectionAttempt] object representing the connection attempt.
     *
     * @see OutgoingCrolangNodeCallbacks
     * @see ConnectionAttempt
     */
    fun connectToSingleNode(
        id: String,
        callbacks: OutgoingCrolangNodeCallbacks = OutgoingCrolangNodeCallbacks()
    ): ConnectionAttempt {
        return connectToMultipleNodes(
            mapOf(id to callbacks)
        ) { _, _ -> /* do nothing, already handled by connection success and failed handlers */ }
    }

}
