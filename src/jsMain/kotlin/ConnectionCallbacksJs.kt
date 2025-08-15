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
 * Callbacks for handling incoming byte array messages.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class IncomingByteArrayMsgCallbacksJs{
    var onNewMsgPartReceived: (node: CrolangNodeJs, msgId: Int, part: Int, total: Int) -> Unit = { _, _, _, _ -> }
    var onNewCompleteMsgReceived: (node: CrolangNodeJs, msgId: Int, msg: ByteArray) -> Unit = { _, _, _ -> }
    var onMsgCorruption: (node: CrolangNodeJs, msgId: Int) -> Unit = { _, _ -> }

    /**
     * Sets the callback for receiving a new part of a byte array message.
     *
     * This callback is triggered when a new part of a byte array message is received.
     *
     * @param callback Function that receives the node, message ID, part number, and total parts
     * @return This instance for method chaining
     */
    fun setOnNewMsgPartReceived(
        callback: (node: CrolangNodeJs, msgId: Int, part: Int, total: Int) -> Unit
    ): IncomingByteArrayMsgCallbacksJs {
        onNewMsgPartReceived = callback
        return this
    }

    /**
     * Gets the currently configured callback for new message parts.
     *
     * @return The configured callback function
     */
    fun getOnNewMsgPartReceived(): (node: CrolangNodeJs, msgId: Int, part: Int, total: Int) -> Unit {
        return onNewMsgPartReceived
    }

    /**
     * Sets the callback for receiving a complete byte array message.
     *
     * This callback is triggered when a complete byte array message is received.
     *
     * @param callback Function that receives the node, message ID, and complete message
     * @return This instance for method chaining
     */
    fun setOnNewCompleteMsgReceived(
        callback: (node: CrolangNodeJs, msgId: Int, msg: ByteArray) -> Unit
    ): IncomingByteArrayMsgCallbacksJs {
        onNewCompleteMsgReceived = callback
        return this
    }

    /**
     * Gets the currently configured callback for complete messages.
     *
     * @return The configured callback function
     */
    fun getOnNewCompleteMsgReceived(): (node: CrolangNodeJs, msgId: Int, msg: ByteArray) -> Unit {
        return onNewCompleteMsgReceived
    }

    /**
     * Sets the callback for handling message corruption.
     *
     * This callback is triggered when a byte array message is detected as corrupted.
     *
     * @param callback Function that receives the node and message ID of the corrupted message
     * @return This instance for method chaining
     */
    fun setOnMsgCorruption(
        callback: (node: CrolangNodeJs, msgId: Int) -> Unit
    ): IncomingByteArrayMsgCallbacksJs {
        onMsgCorruption = callback
        return this
    }

    /**
     * Gets the currently configured callback for message corruption.
     *
     * @return The configured callback function
     */
    fun getOnMsgCorruption(): (node: CrolangNodeJs, msgId: Int) -> Unit {
        return onMsgCorruption
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
/**
 * Builder for creating [IncomingByteArrayMsgCallbacksJs] instances.
 *
 * This builder provides a convenient way to create callback objects
 * for handling incoming byte array messages with default no-op implementations.
 */
object IncomingByteArrayMsgCallbacksJsBuilder {

    /**
     * Creates a new [IncomingByteArrayMsgCallbacksJs] instance with default callbacks.
     *
     * @return A new callback configuration instance ready for customization
     */
    fun create(): IncomingByteArrayMsgCallbacksJs {
        return IncomingByteArrayMsgCallbacksJs()
    }
}

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
    private var onConnectionFailed: (id: String, reason: P2PConnectionFailedErrorJS) -> Unit = { _, _ -> }
    private var onDisconnection: (id: String) -> Unit = {}
    private var onNewStringMsq: Map<String, (CrolangNodeJs, String) -> Unit> = emptyMap()
    private var onNewByteArrayMsg: Map<String, IncomingByteArrayMsgCallbacksJs> = emptyMap()

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
        callback: (id: String, reason: P2PConnectionFailedErrorJS) -> Unit
    ): IncomingCrolangNodesCallbacksJs {
        onConnectionFailed = callback
        return this
    }

    /**
     * Gets the currently configured connection failure callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionFailed(): (id: String, reason: P2PConnectionFailedErrorJS) -> Unit {
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
     * Adds a string message callback for a specific channel.
     * 
     * This callback is triggered when a connected node sends a string message on the specified channel.
     * You can register multiple callbacks for different channels.
     * 
     * @param channel The string message channel to listen to
     * @param callback Function that receives the sender node and string message content
     * @return This instance for method chaining
     */
    fun addOnNewStringMsgCallback(channel: String, callback: (CrolangNodeJs, String) -> Unit): IncomingCrolangNodesCallbacksJs {
        onNewStringMsq = onNewStringMsq + (channel to callback)
        return this
    }

    /**
     * Gets all configured string message callbacks by channel.
     * 
     * @return Map of channel names to their callback functions
     */
    fun getOnNewStringMsgCallbacks(): Map<String, (CrolangNodeJs, String) -> Unit> {
        return onNewStringMsq
    }

    /**
     * Adds a byte array message callback for a specific channel.
     * This callback is triggered when a connected node sends a byte array message on the specified channel.
     * You can register multiple callbacks for different channels.
     * @param channel The byte array message channel to listen to
     * @param callbacks The callbacks to handle byte array messages
     * @return This instance for method chaining
     */
    fun addOnNewByteArrayMsgCallback(
        channel: String,
        callbacks: IncomingByteArrayMsgCallbacksJs
    ): IncomingCrolangNodesCallbacksJs {
        onNewByteArrayMsg = onNewByteArrayMsg + (channel to callbacks)
        return this
    }

    /**
     * Gets all configured byte array message callbacks by channel.
     *
     * @return Map of channel names to their callback configurations
     */
    fun getOnNewByteArrayMsgCallbacks(): Map<String, IncomingByteArrayMsgCallbacksJs> {
        return onNewByteArrayMsg
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
    private var onConnectionFailed: (id: String, reason: P2PConnectionFailedErrorJS) -> Unit = { _, _ -> }
    private var onDisconnection: (id: String) -> Unit = {}
    private var onNewStringMsg: Map<String, (CrolangNodeJs, String) -> Unit> = emptyMap()
    private var onNewByteArrayMsg: Map<String, IncomingByteArrayMsgCallbacksJs> = emptyMap()

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
    fun setOnConnectionFailed(callback: (id: String, reason: P2PConnectionFailedErrorJS) -> Unit): CrolangNodeCallbacksJs {
        onConnectionFailed = callback
        return this
    }

    /**
     * Gets the currently configured connection failure callback.
     * 
     * @return The configured callback function
     */
    fun getOnConnectionFailed(): (id: String, reason: P2PConnectionFailedErrorJS) -> Unit {
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
        onNewStringMsg = onNewStringMsg + (channel to callback)
        return this
    }

    /**
     * Gets all configured message callbacks by channel.
     * 
     * @return Map of channel names to their callback functions
     */
    fun getOnNewMsgCallbacks(): Map<String, (CrolangNodeJs, String) -> Unit> {
        return onNewStringMsg
    }

    /**
     * Adds a byte array message callback for a specific channel.
     *
     * This callback is triggered when the connected node sends a byte array message on the specified channel.
     * You can register multiple callbacks for different channels.
     *
     * @param channel The byte array message channel to listen to
     * @param callbacks The callbacks to handle byte array messages
     * @return This instance for method chaining
     */
    fun addOnNewByteArrayMsgCallback(
        channel: String,
        callbacks: IncomingByteArrayMsgCallbacksJs
    ): CrolangNodeCallbacksJs {
        onNewByteArrayMsg = onNewByteArrayMsg + (channel to callbacks)
        return this
    }

    /**
     * Gets all configured byte array message callbacks by channel.
     *
     * @return Map of channel names to their callback configurations
     */
    fun getOnNewByteArrayMsgCallbacks(): Map<String, IncomingByteArrayMsgCallbacksJs> {
        return onNewByteArrayMsg
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
class CrolangNodeConnectionTargetsJs {

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
