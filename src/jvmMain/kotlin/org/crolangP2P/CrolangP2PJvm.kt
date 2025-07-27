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

import internal.RuntimeDependencyResolver
import internal.dependencies_injection.DependenciesInjectionProviderJvm
import org.crolangP2P.CrolangP2PJvm.Java
import org.crolangP2P.CrolangP2PJvm.Kotlin
import org.crolangP2P.errors.AllowIncomingConnectionsError
import org.crolangP2P.errors.ConnectionToBrokerError
import org.crolangP2P.errors.DisconnectionFromBrokerError
import org.crolangP2P.errors.P2PConnectionFailedError
import org.crolangP2P.errors.RemoteNodesConnectionStatusCheckError
import org.crolangP2P.errors.SendSocketMsgError
import java.util.*
import java.util.function.Consumer

/**
 * CrolangP2PJvm is a singleton object that manages the connection to the Crolang Broker and allows for connecting to
 * remote CrolangNodes.
 *
 * Contains both a Kotlin and Java interface, allowing for easy integration with both languages.
 *
 * @see Kotlin
 * @see Java
 */
object CrolangP2PJvm {

    init {
        RuntimeDependencyResolver.loadDependency()
    }

    /**
     * The Kotlin interface for CrolangP2P.
     */
    val Kotlin = CoreCrolangP2PFacade(DependenciesInjectionProviderJvm.getDependencies())

    /**
     * The Java interface for CrolangP2P.
     */
    object Java {

        /**
         * Checks if the local node is connected to the Crolang Broker.
         *
         * @param onResult callback that returns true if the local node is connected to the Broker, false otherwise.
         */
        @JvmStatic
        fun isLocalNodeConnectedToBroker(onResult: Consumer<Boolean>) {
            Kotlin.isLocalNodeConnectedToBroker{ onResult.accept(it) }
        }

        /**
         * Checks if the provided remote node is connected to the Crolang Broker.
         *
         * @param id The ID of the remote node to check.
         * @param onResult A callback function that is called with the connection status of the remote node (true if connected, false otherwise).
         * @param onError A callback function that is called if the connection attempt fails, providing a [RemoteNodesConnectionStatusCheckError].
         * @see RemoteNodesConnectionStatusCheckError
         */
        @JvmStatic
        fun isRemoteNodeConnectedToBroker(
            id: String,
            onResult: Consumer<Boolean>,
            onError: Consumer<RemoteNodesConnectionStatusCheckError>
        ) {
            Kotlin.isRemoteNodeConnectedToBroker(
                id,
                onResult = { onResult.accept(it) },
                onError = { onError.accept(it) }
            )
        }

        /**
         * Checks if the provided set of remote nodes are connected to the Crolang Broker.
         *
         * @param ids The set of remote node IDs to check.
         * @param onResult A callback function that is called with a map of node IDs and their connection status (true if connected, false otherwise).
         * @param onError A callback function that is called if the connection attempt fails, providing a [RemoteNodesConnectionStatusCheckError].
         * @see RemoteNodesConnectionStatusCheckError
         */
        @JvmStatic
        fun areRemoteNodesConnectedToBroker(
            ids: Set<String>,
            onResult: Consumer<Map<String, Boolean>>,
            onError: Consumer<RemoteNodesConnectionStatusCheckError>
        ) {
            Kotlin.areRemoteNodesConnectedToBroker(
                ids,
                onResult = { onResult.accept(it) },
                onError = { onError.accept(it) }
            )
        }

        /**
         * Connects to the Crolang Broker.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
         * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
         *
         * @see ConnectionToBrokerError
         */
        @JvmStatic
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onSuccess: Runnable,
            onError: Consumer<ConnectionToBrokerError>
        ) {
            Kotlin.connectToBroker(
                brokerAddr = brokerAddr,
                nodeId = nodeId,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) }
            )
        }

        /**
         * Connects to the Crolang Broker.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
         * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
         * @param onNewSocketMsg Map of callbacks for handling direct messages received via the Broker's WebSocket relay (optional).
         *
         * @see ConnectionToBrokerError
         */
        @JvmStatic
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onSuccess: Runnable,
            onError: Consumer<ConnectionToBrokerError>,
            onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>
        ){
            Kotlin.connectToBroker(
                brokerAddr = brokerAddr,
                nodeId = nodeId,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) },
                onNewSocketMsg = onNewSocketMsg
            )
        }

        /**
         * Connects to the Crolang Broker.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
         * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
         * @param additionalParameters Additional parameters for the connection, including logging options, settings and lifecycle callbacks.
         *
         * @see ConnectionToBrokerError
         */
        @JvmStatic
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onSuccess: Runnable,
            onError: Consumer<ConnectionToBrokerError>,
            additionalParameters: BrokerConnectionAdditionalParameters
        ) {
            Kotlin.connectToBroker(
                brokerAddr = brokerAddr,
                nodeId = nodeId,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) },
                additionalParameters = additionalParameters
            )
        }

        /**
         * Connects to the Crolang Broker.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
         * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         *
         * @see ConnectionToBrokerError
         */
        @JvmStatic
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onSuccess: Runnable,
            onError: Consumer<ConnectionToBrokerError>,
            onConnectionAttemptData: String
        ) {
            Kotlin.connectToBroker(
                brokerAddr = brokerAddr,
                nodeId = nodeId,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) },
                onConnectionAttemptData = onConnectionAttemptData
            )
        }

        /**
         * Connects to the Crolang Broker.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
         * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         * @param onNewSocketMsg Map of callbacks for handling direct messages received via the Broker's WebSocket relay (optional).
         *
         * @see ConnectionToBrokerError
         */
        @JvmStatic
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onSuccess: Runnable,
            onError: Consumer<ConnectionToBrokerError>,
            onConnectionAttemptData: String,
            onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>
        ) {
            Kotlin.connectToBroker(
                brokerAddr = brokerAddr,
                nodeId = nodeId,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) },
                onConnectionAttemptData = onConnectionAttemptData,
                onNewSocketMsg = onNewSocketMsg
            )
        }

        /**
         * Connects to the Crolang Broker.
         *
         * @param brokerAddr The address of the Broker to connect to.
         * @param nodeId The ID of the local node.
         * @param onSuccess Callback invoked when the connection to the Broker is successfully established.
         * @param onError Callback invoked when an error occurs while connecting to the Broker; empty by default.
         * @param onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
         * @param onNewSocketMsg Map of callbacks for handling direct messages received via the Broker's WebSocket relay (optional).
         * @param additionalParameters Additional parameters for the connection, including logging options, settings and lifecycle callbacks.
         *
         * @see ConnectionToBrokerError
         */
        @JvmStatic
        fun connectToBroker(
            brokerAddr: String,
            nodeId: String,
            onSuccess: Runnable,
            onError: Consumer<ConnectionToBrokerError>,
            onConnectionAttemptData: String,
            onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>,
            additionalParameters: BrokerConnectionAdditionalParameters
        ) {
            Kotlin.connectToBroker(
                brokerAddr = brokerAddr,
                nodeId = nodeId,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) },
                onConnectionAttemptData = onConnectionAttemptData,
                onNewSocketMsg = onNewSocketMsg,
                additionalParameters = additionalParameters
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
        @JvmStatic
        fun disconnectFromBroker(
            onSuccess: Runnable,
            onError: Consumer<DisconnectionFromBrokerError>
        ) {
            Kotlin.disconnectFromBroker(
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) }
            )
        }

        /**
         * Checks if incoming connections from other Nodes are allowed.
         *
         * @param onResult callback returning true if incoming connections are allowed, false otherwise.
         */
        @JvmStatic
        fun areIncomingConnectionsAllowed(
            onResult: Consumer<Boolean>
        ) {
            Kotlin.areIncomingConnectionsAllowed { onResult.accept(it) }
        }

        /**
         * Allows incoming connections from other nodes.
         *
         * @param callbacks The callbacks to be used for incoming connections.
         * @param onSuccess A callback function that is called when incoming connections are successfully allowed.
         * @param onError A callback function that is called if an error occurs while allowing incoming connections.
         * @see IncomingCrolangNodesCallbacks
         */
        @JvmStatic
        fun allowIncomingConnections(
            callbacks: IncomingCrolangNodesCallbacks,
            onSuccess: Runnable,
            onError: Consumer<AllowIncomingConnectionsError>
        ) {
            Kotlin.allowIncomingConnections(
                callbacks,
                onSuccess = { onSuccess.run() },
                onError = { onError.accept(it) }
            )
        }

        /**
         * Allows incoming connections from other nodes.
         *
         * @param callbacks The callbacks to be used for incoming connections.
         * @param onSuccess A callback function that is called when incoming connections are successfully allowed.
         * @see IncomingCrolangNodesCallbacks
         */
        @JvmStatic
        fun allowIncomingConnections(
            callbacks: IncomingCrolangNodesCallbacks,
            onSuccess: Runnable
        ) {
            Kotlin.allowIncomingConnections(callbacks){ onSuccess.run() }
        }

        /**
         * Stops accepting incoming connections from other nodes.
         *
         * Stopping incoming connections will not disconnect any currently connected Nodes; on the other hand,
         * any connection attempt from other Nodes will be refused.
         *
         * @param onStopped Callback invoked when incoming connections are successfully stopped.
         */
        @JvmStatic
        fun stopIncomingConnections(
            onStopped: Runnable
        ) {
            Kotlin.stopIncomingConnections{ onStopped.run() }
        }

        /**
         * Returns a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
         * @param onResult A callback returning a map of all connected nodes, where the key is the node ID and the value is the CrolangNode.
         */
        @JvmStatic
        fun getAllConnectedNodes(
            onResult: Consumer<Map<String, CrolangNode>>
        ) {
            Kotlin.getAllConnectedNodes{ onResult.accept(it) }
        }

        /**
         * Returns the connected node with the given id, if it exists.
         * @param id The id of the node to retrieve.
         * @param onResult Callback that returns the connected node if found, empty otherwise.
         */
        @JvmStatic
        fun getConnectedNode(
            id: String,
            onResult: Consumer<Optional<CrolangNode>>
        ) {
            Kotlin.getConnectedNode(id){ onResult.accept(Optional.ofNullable(it)) }
        }

        /**
         * Connects to multiple nodes asynchronously.
         *
         * This method initiates a connection attempt to the specified nodes and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * @param targets A map of node IDs and their corresponding [OutgoingCrolangNodeCallbacks].
         * @param onConnectionAttemptConcluded A callback function that is called when the connection attempt is concluded;
         * the callback returns two maps: one with the successfully connected nodes and another with the connection errors.
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see OutgoingCrolangNodeCallbacks
         * @see ConnectionAttempt
         * @see CrolangNodeConnectionResult
         * @see P2PConnectionFailedError
         */
        @JvmStatic
        fun connectToMultipleNodes(
            targets: Map<String, OutgoingCrolangNodeCallbacks>,
            onConnectionAttemptConcluded: java.util.function.BiConsumer<Map<String, CrolangNode>, Map<String, P2PConnectionFailedError>>
        ): ConnectionAttempt {
            return Kotlin.connectToMultipleNodes(targets) {
                    results: Map< String, CrolangNode>, errors: Map<String, P2PConnectionFailedError> ->
                        onConnectionAttemptConcluded.accept(results, errors)
            }
        }

        /**
         * Connects to multiple nodes asynchronously.
         *
         * This method initiates a connection attempt to the specified nodes and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * @param targets A map of node IDs and their corresponding [OutgoingCrolangNodeCallbacks].
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see OutgoingCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        @JvmStatic
        fun connectToMultipleNodes(targets: Map<String, OutgoingCrolangNodeCallbacks>): ConnectionAttempt {
            return Kotlin.connectToMultipleNodes(targets)
        }

        /**
         * Connects to a single node asynchronously.
         *
         * This method initiates a connection attempt to the specified node and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * @param id The ID of the node to connect to.
         * @param callbacks The [OutgoingCrolangNodeCallbacks] to be used for the connection attempt.
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see OutgoingCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        @JvmStatic
        fun connectToSingleNode(id: String, callbacks: OutgoingCrolangNodeCallbacks): ConnectionAttempt {
            return Kotlin.connectToSingleNode(id, callbacks)
        }

        /**
         * Connects to a single node asynchronously.
         *
         * This method initiates a connection attempt to the specified node and returns a [ConnectionAttempt] object
         * that can be used to manage the connection process.
         *
         * @param id The ID of the node to connect to.
         * @return A [ConnectionAttempt] object representing the connection attempt.
         *
         * @see OutgoingCrolangNodeCallbacks
         * @see ConnectionAttempt
         */
        @JvmStatic
        fun connectToSingleNode(id: String): ConnectionAttempt {
            return Kotlin.connectToSingleNode(id)
        }

        /**
         * Sends a message to a remote node via the Broker using WebSocket relay.
         *
         * @param id The ID of the remote node to send the message to.
         * @param channel The channel on which to send the message.
         * @param msg The message to send.
         * @param onMsgSent Callback invoked when the WebSocket message is successfully sent to the Broker that will relay it to the target Node.
         * @param onError Callback invoked when the WebSocket message could not be delivered to the Broker and, consequently, to the target Node.
         *
         * @see SendSocketMsgError
         */
        @JvmStatic
        fun sendSocketMsg(
            id: String,
            channel: String,
            msg: String,
            onMsgSent: Runnable,
            onError: Consumer<SendSocketMsgError>
        ) {
            Kotlin.sendSocketMsg(
                id,
                channel,
                msg,
                onMsgSent = { onMsgSent.run() },
                onError = { onError.accept(it) }
            )
        }

        /**
         * Sends a message to a remote node via the Broker using WebSocket relay.
         *
         * @param id The ID of the remote node to send the message to.
         * @param channel The channel on which to send the message.
         * @param msg The message to send.
         *
         * @see SendSocketMsgError
         */
        @JvmStatic
        fun sendSocketMsg(
            id: String,
            channel: String,
            msg: String
        ) {
            Kotlin.sendSocketMsg(id, channel, msg)
        }

    }

}
