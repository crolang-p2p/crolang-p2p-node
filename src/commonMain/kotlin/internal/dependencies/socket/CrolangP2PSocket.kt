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

package internal.dependencies.socket

/**
 * Abstract base class for platform-specific socket implementations.
 * 
 * This class provides a common interface for socket communication with CroLang P2P brokers,
 * abstracting away platform-specific networking details.
 */
abstract class CrolangP2PSocket {

    /**
     * Establishes a connection to the remote endpoint.
     */
    abstract fun connect()

    /**
     * Disconnects from the remote endpoint while keeping the socket instance available for reconnection.
     */
    abstract fun disconnect()

    /**
     * Permanently closes the socket connection and releases associated resources.
     */
    abstract fun close()

    /**
     * Checks whether the socket is currently connected to the remote endpoint.
     *
     * @return true if connected, false otherwise
     */
    abstract fun connected(): Boolean

    /**
     * Registers an event listener for the specified event type.
     *
     * @param event The name of the event to listen for
     * @param callback The function to call when the event is received
     */
    abstract fun on(event: String, callback: (args: Array<Any>) -> Unit)

    /**
     * Emits an event to the remote endpoint with optional acknowledgment handling.
     *
     * @param event The name of the event to emit
     * @param msg The message payload to send
     * @param onAck Callback function to handle acknowledgment from the remote endpoint
     */
    abstract fun emit(event: String, msg: String, onAck: (args: Array<out Any>) -> Unit)

}
