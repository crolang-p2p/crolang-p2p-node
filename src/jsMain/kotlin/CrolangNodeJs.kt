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

import org.crolangP2P.CrolangNode
import org.crolangP2P.CrolangNodeState

/**
 * Represents a P2P connection to a remote node.
 * 
 * This class provides the interface for interacting with a connected peer node, allowing you to:
 * - Send messages on different channels
 * - Monitor connection state
 * - Disconnect from the peer
 * - Access peer information (ID, platform, version)
 * 
 * Node instances are created automatically when connections are established and passed
 * to your connection success callbacks.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangNodeJs internal constructor(private val node: CrolangNode) {

    /** The unique identifier of the remote node */
    val id: String = node.id

    /** The platform/runtime of the remote node (e.g., "JVM", "JavaScript") */
    val platform: String = node.platform

    /** The library version of the remote node */
    val version: String = node.version

    /**
     * Sends a string message to the remote node on the specified channel.
     * 
     * Messages are transmitted directly over the P2P connection using WebRTC data channels.
     * Large messages are automatically split and reassembled on the receiving end.
     * 
     * @param channel The message channel to send on
     * @param msg The string message content to send
     * @return true if the message was queued for sending, false otherwise
     */
    fun sendString(channel: String, msg: String): Boolean {
        return node.sendString(channel, msg)
    }

    /**
     * Sends a byte array message to the remote node on the specified channel.
     *
     * This method allows sending binary data directly over the P2P connection.
     * The data is sent as a single message, without automatic splitting.
     *
     * @param channel The message channel to send on
     * @param msg The byte array content to send
     * @return true if the message was queued for sending, false otherwise
     */
    fun sendBytes(channel: String, msg: ByteArray): Boolean {
        return node.sendBytes(channel, msg)
    }

    /**
     * Gets the current connection state of this node.
     * 
     * @return The current state (CONNECTING, CONNECTED, or DISCONNECTED)
     */
    fun getState(): CrolangNodeStateJs {
        return when(node.getState()){
            CrolangNodeState.CONNECTING -> CrolangNodeStateJs.CONNECTING
            CrolangNodeState.CONNECTED -> CrolangNodeStateJs.CONNECTED
            CrolangNodeState.DISCONNECTED -> CrolangNodeStateJs.DISCONNECTED
        }
    }

    /**
     * Disconnects from the remote node.
     * 
     * This closes the P2P connection and triggers the disconnection callback.
     * After calling this method, the node cannot be used for sending messages.
     */
    fun disconnect() {
        node.disconnect()
    }

}

/**
 * Enumeration of possible P2P connection states.
 * 
 * This enum tracks the lifecycle of a P2P connection from initial negotiation
 * through active communication to final disconnection.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class CrolangNodeStateJs {
    /**
     * The node is in the process of establishing a P2P connection.
     * 
     * During this state, WebRTC negotiation is ongoing and messages cannot be sent yet.
     */
    CONNECTING,

    /**
     * The node has an active P2P connection.
     * 
     * In this state, messages can be sent and received reliably.
     */
    CONNECTED,

    /**
     * The node is disconnected and cannot communicate.
     * 
     * This is the final state after disconnection, either voluntary or due to errors.
     */
    DISCONNECTED
}
