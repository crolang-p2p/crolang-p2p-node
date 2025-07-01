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

/**
 * External declaration for the socket.io-client library.
 * This provides Kotlin bindings for the Socket.IO client implementation.
 *
 * @see [Socket.IO Client Documentation](https://socket.io/docs/v4/client-api/)
 */
@JsModule("socket.io-client")
@JsNonModule
external object SocketIOClient {
    
    /**
     * Creates a new Socket.IO client instance.
     *
     * @param url The URL to connect to (e.g., "http://localhost:8080")
     * @param options Configuration options for the socket connection
     * @return A new Socket instance
     */
    fun io(url: String, options: SocketIOOptions = definedExternally): Socket
}

/**
 * Configuration options for Socket.IO client connection.
 * Maps to the options object passed to io() function.
 */
external interface SocketIOOptions {
    /**
     * List of transport methods to use. Default is ["polling", "websocket"].
     * For CroLang P2P, we typically use ["websocket"] only.
     */
    var transports: Array<String>?
    
    /**
     * Whether to automatically connect when creating the socket. Default is true.
     * Set to false if you want to manually control connection timing.
     */
    var autoConnect: Boolean?
    
    /**
     * Whether to automatically reconnect when disconnected. Default is true.
     * For CroLang P2P broker connections, typically set to false.
     */
    var reconnection: Boolean?
    
    /**
     * Query parameters to include in the connection handshake.
     * Used for passing node ID, version, authentication data, etc.
     */
    var query: dynamic
    
    /**
     * Connection timeout in milliseconds. Default is 20000 (20 seconds).
     */
    var timeout: Int?
    
    /**
     * Whether to force a new connection. Default is false.
     */
    var forceNew: Boolean?
}

/**
 * Socket.IO client socket instance.
 * Provides methods for connection management, event handling, and message emission.
 */
external interface Socket {
    
    /**
     * The unique identifier for this socket connection.
     */
    val id: String?
    
    /**
     * Whether the socket is currently connected to the server.
     */
    val connected: Boolean
    
    /**
     * Whether the socket is currently disconnected from the server.
     */
    val disconnected: Boolean
    
    /**
     * Manually connects the socket to the server.
     * Only needed if autoConnect was set to false.
     *
     * @return The socket instance for chaining
     */
    fun connect(): Socket
    
    /**
     * Disconnects the socket from the server.
     * The socket can be reconnected later with connect().
     *
     * @return The socket instance for chaining
     */
    fun disconnect(): Socket
    
    /**
     * Closes the socket connection permanently.
     * The socket cannot be reconnected after calling this method.
     *
     * @return The socket instance for chaining
     */
    fun close(): Socket
    
    /**
     * Registers an event listener for the specified event.
     *
     * @param event The name of the event to listen for
     * @param listener The callback function to execute when the event is received
     * @return The socket instance for chaining
     */
    fun on(event: String, listener: (Array<Any>) -> Unit): Socket
    
    /**
     * Registers a one-time event listener that will be removed after first execution.
     *
     * @param event The name of the event to listen for
     * @param listener The callback function to execute when the event is received
     * @return The socket instance for chaining
     */
    fun once(event: String, listener: (Array<Any>) -> Unit): Socket
    
    /**
     * Removes event listeners for the specified event.
     *
     * @param event The name of the event to remove listeners for
     * @param listener Optional specific listener to remove. If not provided, removes all listeners for the event.
     * @return The socket instance for chaining
     */
    fun off(event: String, listener: ((Array<Any>) -> Unit)? = definedExternally): Socket
    
    /**
     * Emits an event to the server without acknowledgment.
     *
     * @param event The name of the event to emit
     * @param args The arguments to send with the event
     * @return The socket instance for chaining
     */
    fun emit(event: String, vararg args: Any): Socket
    
    /**
     * Emits an event to the server with a callback for acknowledgment.
     * This is the traditional callback-style emit method.
     *
     * @param event The name of the event to emit
     * @param data The data to send with the event
     * @param callback The callback function to handle acknowledgment
     * @return The socket instance for chaining
     */
    fun emit(event: String, data: String, callback: (Array<Any>) -> Unit): Socket
    
    /**
     * Emits an event to the server and returns a Promise for acknowledgment.
     * This is the modern Promise-based acknowledgment mechanism used by the Node.js implementation.
     *
     * @param event The name of the event to emit
     * @param args The arguments to send with the event
     * @return A Promise that resolves with the acknowledgment data from the server
     */
    fun emitWithAck(event: String, vararg args: Any): dynamic
    
    /**
     * Sets a timeout for acknowledgments.
     *
     * @param timeout Timeout in milliseconds
     * @return The socket instance for chaining
     */
    fun timeout(timeout: Int): Socket
}

/**
 * Standard Socket.IO event names that are built into the protocol.
 */
object SocketIOEvents {
    /** Fired when the socket connects to the server */
    const val CONNECT = "connect"
    
    /** Fired when the socket connects to the server after a reconnection */
    const val CONNECT_AFTER_RECONNECT = "reconnect"
    
    /** Fired when the socket disconnects from the server */
    const val DISCONNECT = "disconnect"
    
    /** Fired when a connection attempt fails */
    const val CONNECT_ERROR = "connect_error"
    
    /** Fired when a reconnection attempt fails */
    const val RECONNECT_ERROR = "reconnect_error"
    
    /** Fired when the socket starts trying to reconnect */
    const val RECONNECT_ATTEMPT = "reconnect_attempt"
    
    /** Fired when the socket gives up trying to reconnect */
    const val RECONNECT_FAILED = "reconnect_failed"
}
