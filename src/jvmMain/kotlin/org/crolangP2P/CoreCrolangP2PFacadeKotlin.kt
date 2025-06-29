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
import org.crolangP2P.CoreCrolangP2PFacade
import java.util.Optional

/**
 * The Kotlin interface for CrolangP2P.
 */
class CoreCrolangP2PFacadeKotlin(dependencies: DependenciesInjection) {

    private val coreFacade = CoreCrolangP2PFacade(dependencies)

    /**
     * Checks if the local node is connected to the Crolang Broker.
     * @return true if the local node is connected to the Broker, false otherwise.
     */
    fun isLocalNodeConnectedToBroker(): Boolean = coreFacade.isLocalNodeConnectedToBroker()

    /**
     * Checks if the provided remote node is connected to the Crolang Broker.
     * @param id The ID of the remote node to check.
     * @return A Result containing true if the node is connected, false otherwise.
     */
    fun isRemoteNodeConnectedToBroker(id: String): Result<Boolean> = 
        coreFacade.isRemoteNodeConnectedToBroker(id)

    /**
     * Checks if the provided set of remote nodes are connected to the Crolang Broker.
     * @param ids The set of remote node IDs to check.
     * @return A Result containing a map of node IDs and their connection status.
     */
    fun areRemoteNodesConnectedToBroker(ids: Set<String>): Result<Map<String, Boolean>> = 
        coreFacade.areRemoteNodesConnectedToBroker(ids)

    /**
     * Sends a message to a remote node via the Broker using WebSocket relay.
     * @param id The ID of the remote node to send the message to.
     * @param channel The channel on which to send the message.
     * @param msg The message to send (optional). If not provided, an empty string will be sent.
     * @return A Result indicating success or failure.
     */
    fun sendSocketMsg(id: String, channel: String, msg: String?): Result<Unit> = 
        coreFacade.sendSocketMsg(id, channel, msg)

    /**
     * Connects to the Crolang Broker using the provided broker address and Node ID.
     * @param brokerAddr The address of the Broker to connect to.
     * @param nodeId The ID of the local node.
     * @return A Result indicating success or failure of the connection attempt.
     */
    fun connectToBroker(brokerAddr: String, nodeId: String): Result<Unit> = 
        coreFacade.connectToBroker(brokerAddr, nodeId)

    /**
     * Connects to the Crolang Broker using the provided broker address and Node ID.
     * @param brokerAddr The address of the Broker to connect to.
     * @param nodeId The ID of the local node.
     * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
     * @param onNewSocketMsg Optional Map of callbacks for handling direct messages received via the Broker's WebSocket relay.
     * @param additionalParameters Optional additional parameters for the connection.
     * @return A Result indicating success or failure of the connection attempt.
     */
    fun connectToBroker(
        brokerAddr: String,
        nodeId: String,
        onConnectionAttemptData: String = "",
        onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit> = emptyMap(),
        additionalParameters: BrokerConnectionAdditionalParameters = BrokerConnectionAdditionalParameters()
    ): Result<Unit> = coreFacade.connectToBroker(brokerAddr, nodeId, onConnectionAttemptData, onNewSocketMsg, additionalParameters)

    /**
     * Disconnects from the Crolang Broker.
     */
    fun disconnectFromBroker() = coreFacade.disconnectFromBroker()

    /**
     * Checks if incoming connections from other Nodes are allowed.
     * @return true if incoming connections are allowed, false otherwise.
     */
    fun areIncomingConnectionsAllowed(): Boolean = coreFacade.areIncomingConnectionsAllowed()

    /**
     * Allows incoming connections from other nodes.
     * @param callbacks The callbacks to be used for incoming connections.
     * @return A Result indicating success or failure.
     */
    fun allowIncomingConnections(
        callbacks: IncomingCrolangNodesCallbacks = IncomingCrolangNodesCallbacks()
    ): Result<Unit> = coreFacade.allowIncomingConnections(callbacks)

    /**
     * Stops accepting incoming connections from other nodes.
     */
    fun stopIncomingConnections() = coreFacade.stopIncomingConnections()

    /**
     * Returns a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
     * @return A map of all connected nodes.
     */
    fun getAllConnectedNodes(): Map<String, CrolangNode> = coreFacade.getAllConnectedNodes()

    /**
     * Returns the connected node with the given id, if it exists.
     * @param id The id of the node to retrieve.
     * @return An Optional containing the connected node if found, or an empty Optional if not found.
     */
    fun getConnectedNode(id: String): Optional<CrolangNode> {
        val node = coreFacade.getConnectedNode(id)
        return if (node != null) Optional.of(node) else Optional.empty()
    }

    /**
     * Connects to multiple nodes asynchronously.
     * @param targets A map of node IDs and their corresponding AsyncCrolangNodeCallbacks.
     * @param onConnectionAttemptConcluded A callback function that is called when the connection attempt is concluded.
     * @return A ConnectionAttempt object representing the connection attempt.
     */
    fun connectToMultipleNodesAsync(
        targets: Map<String, AsyncCrolangNodeCallbacks>,
        onConnectionAttemptConcluded: (result: Map<String, Result<CrolangNode>>) -> Unit = {}
    ): ConnectionAttempt = coreFacade.connectToMultipleNodesAsync(targets, onConnectionAttemptConcluded)

    /**
     * Connects to a single node asynchronously.
     * @param id The ID of the node to connect to.
     * @param callbacks The AsyncCrolangNodeCallbacks to be used for the connection attempt.
     * @return A ConnectionAttempt object representing the connection attempt.
     */
    fun connectToSingleNodeAsync(
        id: String,
        callbacks: AsyncCrolangNodeCallbacks = AsyncCrolangNodeCallbacks()
    ): ConnectionAttempt = coreFacade.connectToSingleNodeAsync(id, callbacks)

    /**
     * Connects to a single node synchronously.
     * @param id The ID of the node to connect to.
     * @param callbacks The SyncCrolangNodeCallbacks to be used for the connection attempt.
     * @return A Result containing the connected CrolangNode or an exception if the connection failed.
     */
    fun connectToSingleNodeSync(
        id: String,
        callbacks: SyncCrolangNodeCallbacks = SyncCrolangNodeCallbacks()
    ): Result<CrolangNode> = coreFacade.connectToSingleNodeSync(id, callbacks)

    /**
     * Connects to multiple nodes synchronously.
     * @param targets A map of node IDs and their corresponding SyncCrolangNodeCallbacks.
     * @return A map of node IDs and their connection results.
     */
    fun connectToMultipleNodesSync(
        targets: Map<String, SyncCrolangNodeCallbacks>
    ): Map<String, Result<CrolangNode>> = coreFacade.connectToMultipleNodesSync(targets)
}
