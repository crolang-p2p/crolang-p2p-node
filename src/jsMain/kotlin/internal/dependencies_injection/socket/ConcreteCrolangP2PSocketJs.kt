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

package internal.dependencies_injection.socket

import internal.dependencies.socket.CrolangP2PSocket

/**
 * JavaScript implementation of CrolangP2PSocket using Socket.IO client.
 * 
 * This implementation wraps a Socket.IO socket instance and provides
 * the CroLang P2P socket interface for JavaScript environments.
 * 
 * @param socket The underlying Socket.IO socket instance
 */
internal class ConcreteCrolangP2PSocketJs(private val socket: Socket) : CrolangP2PSocket() {

    /**
     * Closes the Socket.IO connection to the broker.
     * 
     * This method terminates the WebSocket connection and cleans up resources.
     */
    override fun close() {
        socket.close()
    }

    /**
     * Establishes a connection to the Socket.IO server.
     * 
     * This method initiates the WebSocket connection to the broker server.
     */
    override fun connect() {
        socket.connect()
    }

    /**
     * Checks if the socket is currently connected to the broker.
     * 
     * @return true if the socket is connected, false otherwise
     */
    override fun connected(): Boolean {
        return socket.connected
    }

    /**
     * Disconnects from the Socket.IO server without closing the socket.
     * 
     * This method temporarily disconnects but allows for reconnection.
     */
    override fun disconnect() {
        socket.disconnect()
    }

    /**
     * Sends a message to the broker with an acknowledgment callback.
     * 
     * @param event The event name to emit
     * @param msg The message payload to send
     * @param onAck Callback function that receives acknowledgment data from the server
     */
    override fun emit(event: String, msg: String, onAck: (args: Array<out Any>) -> Unit) {
        socket.emit(event, msg, onAck)
    }

    /**
     * Registers an event listener for incoming messages from the broker.
     * 
     * @param event The event name to listen for
     * @param callback Function to handle incoming event data
     */
    override fun on(event: String, callback: (args: Array<Any>) -> Unit) {
        socket.on(event) { args ->
            callback(args)
        }
    }
}
