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

package internal.utils

import dev.onvoid.webrtc.RTCConfiguration
import internal.broker.BrokerConnectionHelper
import internal.broker.OnConnectionToBrokerSettings
import io.socket.client.Socket
import org.crolangP2P.IncomingCrolangNodesCallbacks
import internal.node.NodeState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import org.crolangP2P.CrolangSettings
import org.crolangP2P.BrokerLifecycleCallbacks
import org.crolangP2P.LoggingOptions
import java.util.*
import java.util.concurrent.Executors

/**
 * Internal shared store for centralizing data structures useful throughout the event flow and execution in the library.
 * This object serves as a global state container for various configurations, utilities, and runtime data.
 */
internal object SharedStore {

    /**
     * Executor service for managing threads in the library.
     * This is used for executing tasks asynchronously and concurrently.
     */
    private val executor = Executors.newCachedThreadPool()

    /**
     * Map of callbacks for handling direct messages received via the Broker's WebSocket relay.
     *
     * The key is the logical channel name, and the value is a function invoked when a message is received on that channel.
     * Each callback receives the sender node ID (`from`) and the message content (`msg`).
     *
     * This structure enables nodes to register handlers for specific channels, allowing for flexible and modular message processing
     * when using the SOCKET_MSG_EXCHANGE mechanism (see [SocketMsgType.SOCKET_MSG_EXCHANGE] and [SocketDirectMsg]).
     *
     * The callbacks are typically set during the Broker connection phase (see CrolangP2P) and are invoked automatically
     * when a direct message is relayed by the Broker.
     *
     * If not provided by the user, this map is empty by default and no callbacks will be invoked for incoming messages.
     */
    var onNewSocketMsgCallbacks: Map<String, (from: String, msg: String) -> Unit> = emptyMap()

    /**
     * Container for the Broker peers (initiator and responder nodes) involved in the current session.
     * This property holds the initiator and responder nodes, making them available globally for the current Broker session.
     */
    val brokerPeersContainer = CrolangNodesContainer()

    /**
     * Helper for managing the connection and disconnection events to the Broker.
     * This is used to track and control the connection status with the Broker.
     */
    val brokerConnectionHelper = BrokerConnectionHelper()

    /**
     * JSON parser used for serializing and deserializing objects.
     * Provides utility methods to convert between JSON strings and objects.
     */
    val parser = JsonParser()

    /**
     * CBOR parser used for serializing and deserializing objects in CBOR format.
     */
    @OptIn(ExperimentalSerializationApi::class)
    val cborParser = Cbor {  }

    /**
     * Logger used throughout the library for logging messages, including debug and error logs.
     * It is initialized with default logging options.
     */
    var logger: CrolangLogger = CrolangLogger(LoggingOptions())

    /**
     * Local node ID, initialized as "Uninitialized" and can be set during execution.
     * This property identifies the local node within the Broker.
     */
    var localNodeId: String = "Uninitialized"

    /**
     * Optional settings for the connection to the Broker, initially empty.
     * This property holds the settings related to the connection to the Broker such as broker address and data.
     * Initialized on connection to the Broker.
     */
    var onConnectionToBrokerSettings: Optional<OnConnectionToBrokerSettings> = Optional.empty()

    /**
     * Number of reconnection attempts made to the Broker, initialized to 0.
     * This property tracks the number of attempts made to reconnect to the Broker after a disconnection.
     */
    var reconnectionAttempts: Long = 0

    /**
     * Optional socket connection to the remote server, initially empty.
     * This property holds the socket connection to the server when it's established.
     */
    var socketIO: Optional<Socket> = Optional.empty()

    /**
     * Global settings related to Crolang.
     * Contains the configuration settings that influence the behavior of the Broker and connection logic.
     */
    var settings: CrolangSettings = CrolangSettings()

    /**
     * Lifecycle callbacks for the Broker, initially empty initialization.
     * This allows the user to define custom behavior related to the Broker lifecycle events.
     */
    var brokerLifecycleCallbacks: BrokerLifecycleCallbacks = BrokerLifecycleCallbacks()

    /**
     * Optional WebRTC configuration used for peer-to-peer communication, initially empty.
     * This holds the WebRTC configuration necessary for establishing connections between peers.
     */
    var rtcConfiguration: Optional<RTCConfiguration> = Optional.empty()

    /**
     * Optional callbacks for incoming nodes connections, initially empty.
     * This allows the user to define custom behavior related to responder nodes
     */
    var incomingCrolangNodesCallbacks: Optional<IncomingCrolangNodesCallbacks> = Optional.empty()

    /**
     * Resets the shared store by re-initializing the logger, clearing the local node ID, socket, settings,
     * and incoming connection callbacks.
     */
    fun flush() {
        //logger = CrolangLogger(LoggingOptions()) // Re-initialize logger
        reconnectionAttempts = 0
        onNewSocketMsgCallbacks = emptyMap()
        onConnectionToBrokerSettings = Optional.empty()
        localNodeId = "Uninitialized" // Reset local node ID to default
        socketIO = Optional.empty() // Clear socket connection
        settings = CrolangSettings() // Reset settings to default
        incomingCrolangNodesCallbacks = Optional.empty() // Clear incoming connection callbacks
    }

    /**
     * Disconnects all initiator nodes that are not in connected state.
     */
    fun disconnectAllInitiatorNodesNotConnected() {
        val notConnectedNodeIds = brokerPeersContainer.initiatorNodes.values
            .filter { it.state != NodeState.CONNECTED }
            .map { it.remoteNodeId }
        notConnectedNodeIds.forEach { brokerPeersContainer.initiatorNodes.remove(it)?.forceClose(NodeState.DISCONNECTED) }
    }

    /**
     * Disconnects all responder nodes that are not in connected state.
     */
    fun disconnectAllResponderNodesNotConnected() {
        val notConnectedNodedIds = brokerPeersContainer.responderNodes.values
            .filter { it.state != NodeState.CONNECTED }
            .map { it.remoteNodeId }
        notConnectedNodedIds.forEach { brokerPeersContainer.responderNodes.remove(it)?.forceClose(NodeState.DISCONNECTED) }
    }

    /**
     * Executes a user-defined callback within the context of the executor service.
     */
    fun executeCallbackOnExecutor(callbackContainer: () -> Unit) {
        try {
            executor.submit{
                callbackContainer()
            }
        } catch (e: Exception) {
            logger.regularErr("Error executing user-defined callback")
        }
    }
}
