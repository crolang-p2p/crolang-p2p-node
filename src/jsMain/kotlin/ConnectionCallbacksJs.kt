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

import org.crolangP2P.errors.P2PConnectionFailedError

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

/**
 * Callbacks for handling incoming P2P connection events.
 * 
 * This class configures how your node responds to connection attempts from other nodes
 * when you've enabled incoming connections with [CrolangP2PJs.allowIncomingConnections].
 * 
 * Use [IncomingCrolangNodesCallbacksJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class IncomingCrolangNodesCallbacksJs {

    private var onConnectionAttempt: (id: String, platform: String, version: String) -> Boolean = { _, _, _ -> true }
    private var onConnectionSuccess: (node: CrolangNodeJs) -> Unit = {}
    private var onConnectionFailed: (id: String, reason: P2PConnectionFailedError) -> Unit = { _, _ -> }
    private var onDisconnection: (id: String) -> Unit = {}
    private var onNewMsq: Map<String, (CrolangNodeJs, String) -> Unit> = emptyMap()

    /**
     * Sets the callback for incoming connection attempts.
     * 
     * This callback is called when another node attempts to connect to this node.
     * Return true to accept the connection, false to reject it.
     * 
     * @param callback Function that receives the connecting node's ID, platform, and version
     * @return This instance for method chaining
     */
    fun setOnConnectionAttempt(
        callback: (id: String, platform: String, version: String) -> Boolean
    ): IncomingCrolangNodesCallbacksJs {
        onConnectionAttempt = callback
        return this
    }

    /**
     * Gets the currently configured connection attempt callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionAttempt(): (id: String, platform: String, version: String) -> Boolean {
        return onConnectionAttempt
    }

    /**
     * Sets the callback for successful incoming connections.
     * 
     * This callback is triggered when an incoming connection is successfully established.
     * 
     * @param callback Function that receives the connected node instance
     * @return This instance for method chaining
     */
    fun setOnConnectionSuccess(callback: (node: CrolangNodeJs) -> Unit): IncomingCrolangNodesCallbacksJs {
        onConnectionSuccess = callback
        return this
    }

    /**
     * Gets the currently configured connection success callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionSuccess(): (node: CrolangNodeJs) -> Unit {
        return onConnectionSuccess
    }

    /**
     * Sets the callback for failed incoming connections.
     * 
     * This callback is triggered when an incoming connection attempt fails.
     * 
     * @param callback Function that receives the node ID and failure reason
     * @return This instance for method chaining
     */
    fun setOnConnectionFailed(
        callback: (id: String, reason: P2PConnectionFailedError) -> Unit
    ): IncomingCrolangNodesCallbacksJs {
        onConnectionFailed = callback
        return this
    }

    /**
     * Gets the currently configured connection failure callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionFailed(): (id: String, reason: P2PConnectionFailedError) -> Unit {
        return onConnectionFailed
    }

    /**
     * Sets the callback for node disconnections.
     * 
     * This callback is triggered when a previously connected node disconnects.
     * 
     * @param callback Function that receives the disconnected node's ID
     * @return This instance for method chaining
     */
    fun setOnDisconnection(callback: (id: String) -> Unit): IncomingCrolangNodesCallbacksJs {
        onDisconnection = callback
        return this
    }

    /**
     * Gets the currently configured disconnection callback.
     * 
     * @return The configured callback function
     */
    fun getOnDisconnection(): (id: String) -> Unit {
        return onDisconnection
    }

    /**
     * Adds a message callback for a specific channel.
     * 
     * This callback is triggered when a connected node sends a message on the specified channel.
     * You can register multiple callbacks for different channels.
     * 
     * @param channel The message channel to listen to
     * @param callback Function that receives the sender node and message content
     * @return This instance for method chaining
     */
    fun addOnNewMsgCallback(channel: String, callback: (CrolangNodeJs, String) -> Unit): IncomingCrolangNodesCallbacksJs {
        onNewMsq = onNewMsq + (channel to callback)
        return this
    }

    /**
     * Gets all configured message callbacks by channel.
     * 
     * @return Map of channel names to their callback functions
     */
    fun getOnNewMsgCallbacks(): Map<String, (CrolangNodeJs, String) -> Unit> {
        return onNewMsq
    }

}

/**
 * Builder for creating [IncomingCrolangNodesCallbacksJs] instances.
 * 
 * This builder provides a convenient way to create callback objects
 * for handling incoming P2P connections with default no-op implementations.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object IncomingCrolangNodesCallbacksJsBuilder {

    /**
     * Creates a new [IncomingCrolangNodesCallbacksJs] instance with default callbacks.
     * 
     * @return A new callback configuration instance ready for customization
     */
    fun create(): IncomingCrolangNodesCallbacksJs {
        return IncomingCrolangNodesCallbacksJs()
    }
}

/**
 * Callbacks for handling outgoing P2P connection events.
 * 
 * This class configures how your node handles the lifecycle of connections it initiates
 * to other nodes using [CrolangP2PJs.connectToSingleNode] or [CrolangP2PJs.connectToMultipleNodes].
 * 
 * Use [CrolangNodeCallbacksJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangNodeCallbacksJs {

    private var onConnectionSuccess: (node: CrolangNodeJs) -> Unit = {}
    private var onConnectionFailed: (id: String, reason: P2PConnectionFailedError) -> Unit = { _, _ -> }
    private var onDisconnection: (id: String) -> Unit = {}
    private var onNewMsq: Map<String, (CrolangNodeJs, String) -> Unit> = emptyMap()

    /**
     * Sets the callback for successful outgoing connections.
     * 
     * This callback is triggered when an outgoing connection is successfully established.
     * 
     * @param callback Function that receives the connected node instance
     * @return This instance for method chaining
     */
    fun setOnConnectionSuccess(callback: (node: CrolangNodeJs) -> Unit): CrolangNodeCallbacksJs {
        onConnectionSuccess = callback
        return this
    }

    /**
     * Gets the currently configured connection success callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionSuccess(): (node: CrolangNodeJs) -> Unit {
        return onConnectionSuccess
    }

    /**
     * Sets the callback for failed outgoing connections.
     * 
     * This callback is triggered when an outgoing connection attempt fails.
     * 
     * @param callback Function that receives the node ID and failure reason
     * @return This instance for method chaining
     */
    fun setOnConnectionFailed(callback: (id: String, reason: P2PConnectionFailedError) -> Unit): CrolangNodeCallbacksJs {
        onConnectionFailed = callback
        return this
    }

    /**
     * Gets the currently configured connection failure callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionFailed(): (id: String, reason: P2PConnectionFailedError) -> Unit {
        return onConnectionFailed
    }

    /**
     * Sets the callback for node disconnections.
     * 
     * This callback is triggered when a connected node disconnects.
     * 
     * @param callback Function that receives the disconnected node's ID
     * @return This instance for method chaining
     */
    fun setOnDisconnection(callback: (id: String) -> Unit): CrolangNodeCallbacksJs {
        onDisconnection = callback
        return this
    }

    /**
     * Gets the currently configured disconnection callback.
     * 
     * @return The configured callback function
     */
    fun getOnDisconnection(): (id: String) -> Unit {
        return onDisconnection
    }

    /**
     * Adds a message callback for a specific channel.
     * 
     * This callback is triggered when the connected node sends a message on the specified channel.
     * You can register multiple callbacks for different channels.
     * 
     * @param channel The message channel to listen to
     * @param callback Function that receives the sender node and message content
     * @return This instance for method chaining
     */
    fun addOnNewMsgCallback(channel: String, callback: (CrolangNodeJs, String) -> Unit): CrolangNodeCallbacksJs {
        onNewMsq = onNewMsq + (channel to callback)
        return this
    }

    /**
     * Gets all configured message callbacks by channel.
     * 
     * @return Map of channel names to their callback functions
     */
    fun getOnNewMsgCallbacks(): Map<String, (CrolangNodeJs, String) -> Unit> {
        return onNewMsq
    }
}

/**
 * Builder for creating [CrolangNodeCallbacksJs] instances.
 * 
 * This builder provides a convenient way to create callback objects
 * for handling outgoing P2P connections with default no-op implementations.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangNodeCallbacksJsBuilder {

    /**
     * Creates a new [CrolangNodeCallbacksJs] instance with default callbacks.
     * 
     * @return A new callback configuration instance ready for customization
     */
    fun create(): CrolangNodeCallbacksJs {
        return CrolangNodeCallbacksJs()
    }
}

/**
 * Configuration for connecting to multiple nodes simultaneously.
 * 
 * This class allows you to specify multiple target nodes and their respective callbacks
 * when using [CrolangP2PJs.connectToMultipleNodes].
 * 
 * Use [CrolangNodeConnectionTargetsJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangNodeConnectionTargetsJs() {

    private val targets: MutableMap<String, CrolangNodeCallbacksJs> = mutableMapOf()

    /**
     * Adds a target node with its callbacks to the connection list.
     * 
     * @param id The unique identifier of the target node
     * @param callbacks The callbacks to handle events for this specific node
     * @return This instance for method chaining
     */
    fun addTarget(id: String, callbacks: CrolangNodeCallbacksJs): CrolangNodeConnectionTargetsJs {
        targets[id] = callbacks
        return this
    }

    /**
     * Gets all configured target nodes and their callbacks.
     * 
     * @return Map of node IDs to their callback configurations
     */
    fun getTargets(): Map<String, CrolangNodeCallbacksJs> {
        return targets
    }
}

/**
 * Builder for creating [CrolangNodeConnectionTargetsJs] instances.
 * 
 * This builder provides a convenient way to create multi-node connection configurations.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangNodeConnectionTargetsJsBuilder {

    /**
     * Creates a new [CrolangNodeConnectionTargetsJs] instance.
     * 
     * @return A new connection targets configuration instance ready for customization
     */
    fun create(): CrolangNodeConnectionTargetsJs {
        return CrolangNodeConnectionTargetsJs()
    }
}
