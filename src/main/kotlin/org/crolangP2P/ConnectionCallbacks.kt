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

import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException

/**
 * Represents a communication channel.
 */
typealias Channel = String

/**
 * Map of callbacks to be called when a new P2P message is received, keyed by channel.
 */
typealias ChannelMessageCallbacks = Map<Channel, (node: CrolangNode, msg: String) -> Unit>

/**
 * User-defined callbacks for a CrolangNode, common to every possible Node creation method;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onDisconnection Callback to be called when the node is disconnected.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel.
 */
abstract class BasicCrolangNodeCallbacks(
    val onDisconnection: (id: String) -> Unit,
    val onNewMsg: ChannelMessageCallbacks
)

/**
 * User-defined callbacks for a CrolangNode that will be connected synchronously;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel. Optional, defaults to an empty map.
 */
class SyncCrolangNodeCallbacks @JvmOverloads constructor(
    onDisconnection: (id: String) -> Unit = {},
    onNewMsg: ChannelMessageCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewMsg) {

    /**
     * Companion object used to access a [Builder] to create a [SyncCrolangNodeCallbacks] object.
     */
    companion object {
        /**
         * Returns a new [Builder] instance to construct a [SyncCrolangNodeCallbacks] object.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [SyncCrolangNodeCallbacks] in a Java-friendly way.
     */
    class Builder {
        private var onDisconnection: (id: String) -> Unit = {}
        private var onNewMsg: ChannelMessageCallbacks = emptyMap()

        /**
         * Sets the callback invoked when the node is disconnected (Java-friendly Consumer overload).
         *
         * @param callback Consumer receiving the node ID
         * @return this builder instance
         */
        fun onDisconnection(callback: java.util.function.Consumer<String>) = apply {
            this.onDisconnection = { id -> callback.accept(id) }
        }

        /**
         * Sets the map of callbacks for handling incoming P2P messages by channel.
         *
         * @param callbacks map where keys are channel names and values are message handlers
         * @return this builder instance
         */
        fun onNewMsg(callbacks: ChannelMessageCallbacks) = apply {
            this.onNewMsg = callbacks
        }

        /**
         * Builds the [SyncCrolangNodeCallbacks] instance.
         *
         * @return a new [SyncCrolangNodeCallbacks]
         */
        fun build(): SyncCrolangNodeCallbacks {
            return SyncCrolangNodeCallbacks(onDisconnection, onNewMsg)
        }
    }
}

/**
 * User-defined callbacks for a CrolangNode that will be connected asynchronously;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onConnectionSuccess Callback to be called when the node is successfully connected. Optional, defaults to an empty function.
 * @param onConnectionFailed Callback to be called when the node connection fails. Optional, defaults to an empty function.
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel. Optional, defaults to an empty map.
 */
class AsyncCrolangNodeCallbacks @JvmOverloads constructor(
    val onConnectionSuccess: (node: CrolangNode) -> Unit = {},
    val onConnectionFailed: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> },
    onDisconnection: (id: String) -> Unit = {},
    onNewMsg: ChannelMessageCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewMsg) {

    /**
     * Companion object used to access a [Builder] to create a [AsyncCrolangNodeCallbacks] object.
     */
    companion object {
        /**
         * Returns a new [Builder] instance to construct an [AsyncCrolangNodeCallbacks] object.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [AsyncCrolangNodeCallbacks] in a Java-friendly way.
     */
    class Builder {
        private var onConnectionSuccess: (CrolangNode) -> Unit = {}
        private var onConnectionFailed: (String, ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> }
        private var onDisconnection: (String) -> Unit = {}
        private var onNewMsg: ChannelMessageCallbacks = emptyMap()

        /**
         * Sets the callback invoked when the node successfully connects.
         *
         * @param callback function receiving the connected node
         * @return this builder instance
         */
        fun onConnectionSuccess(callback: (CrolangNode) -> Unit) = apply {
            this.onConnectionSuccess = callback
        }

        /**
         * Sets the callback invoked when the node connection fails.
         *
         * @param callback function receiving the node ID and failure reason
         * @return this builder instance
         */
        fun onConnectionFailed(callback: (String, ConnectionToNodeFailedReasonException) -> Unit) = apply {
            this.onConnectionFailed = callback
        }

        /**
         * Sets the callback invoked when the node connection fails (Java-friendly BiConsumer overload).
         *
         * @param callback BiConsumer receiving the node ID and failure reason
         * @return this builder instance
         */
        fun onConnectionFailed(callback: java.util.function.BiConsumer<String, ConnectionToNodeFailedReasonException>) = apply {
            this.onConnectionFailed = { id, reason -> callback.accept(id, reason) }
        }

        /**
         * Sets the callback invoked when the node is disconnected.
         *
         * @param callback function receiving the node ID
         * @return this builder instance
         */
        fun onDisconnection(callback: (String) -> Unit) = apply {
            this.onDisconnection = callback
        }

        /**
         * Sets the callback invoked when the node is disconnected (Java-friendly Consumer overload).
         *
         * @param callback Consumer receiving the node ID
         * @return this builder instance
         */
        fun onDisconnection(callback: java.util.function.Consumer<String>) = apply {
            this.onDisconnection = { id -> callback.accept(id) }
        }

        /**
         * Sets the map of callbacks for handling incoming P2P messages by channel.
         *
         * @param callbacks map where keys are channel names and values are message handlers
         * @return this builder instance
         */
        fun onNewMsg(callbacks: ChannelMessageCallbacks) = apply {
            this.onNewMsg = callbacks
        }

        /**
         * Builds the [AsyncCrolangNodeCallbacks] instance.
         *
         * @return a new [AsyncCrolangNodeCallbacks]
         */
        fun build(): AsyncCrolangNodeCallbacks {
            return AsyncCrolangNodeCallbacks(
                onConnectionSuccess = onConnectionSuccess,
                onConnectionFailed = onConnectionFailed,
                onDisconnection = onDisconnection,
                onNewMsg = onNewMsg
            )
        }
    }
}

/**
 * User-defined callbacks for a CrolangNode whose connection is initiated by another client;
 * all the callbacks are executed asynchronously on an executor service EXCEPT for the onConnectionAttempt callback.
 *
 * @param onConnectionAttempt Callback to be called when a connection attempt is made. Optional, defaults to always allowing the connection.
 * @param onConnectionSuccess Callback to be called when the node is successfully connected. Optional, defaults to an empty function.
 * @param onConnectionFailed Callback to be called when the node connection fails. Optional, defaults to an empty function.
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel. Optional, defaults to an empty map.
 */
class IncomingCrolangNodesCallbacks @JvmOverloads constructor(
    val onConnectionAttempt: (id: String) -> Boolean = { true },
    val onConnectionSuccess: (node: CrolangNode) -> Unit = {},
    val onConnectionFailed: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> },
    onDisconnection: (id: String) -> Unit = {},
    onNewMsg: ChannelMessageCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewMsg) {

    /**
     * Companion object used to access a [Builder] to create a [IncomingCrolangNodesCallbacks] object.
     */
    companion object {
        /**
         * Returns a new [Builder] instance to construct an [IncomingCrolangNodesCallbacks] object.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [IncomingCrolangNodesCallbacks] in a Java-friendly way.
     */
    class Builder {
        private var onConnectionAttempt: (String) -> Boolean = { true }
        private var onConnectionSuccess: (CrolangNode) -> Unit = {}
        private var onConnectionFailed: (String, ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> }
        private var onDisconnection: (String) -> Unit = {}
        private var onNewMsg: ChannelMessageCallbacks = emptyMap()

        /**
         * Sets the callback invoked when a connection attempt is made.
         *
         * @param callback function receiving the node ID and returning a Boolean indicating whether to accept the connection
         * @return this builder instance
         */
        fun onConnectionAttempt(callback: (String) -> Boolean) = apply {
            this.onConnectionAttempt = callback
        }

        /**
         * Sets the callback invoked when the node is successfully connected (Java-friendly Consumer overload).
         *
         * @param callback Consumer receiving the connected node
         * @return this builder instance
         */
        fun onConnectionSuccess(callback: java.util.function.Consumer<CrolangNode>) = apply {
            this.onConnectionSuccess = { node -> callback.accept(node) }
        }

        /**
         * Sets the callback invoked when the node connection fails (Java-friendly BiConsumer overload).
         *
         * @param callback BiConsumer receiving the node ID and failure reason
         * @return this builder instance
         */
        fun onConnectionFailed(callback: java.util.function.BiConsumer<String, ConnectionToNodeFailedReasonException>) = apply {
            this.onConnectionFailed = { id, reason -> callback.accept(id, reason) }
        }

        /**
         * Sets the callback invoked when the node is disconnected (Java-friendly Consumer overload).
         *
         * @param callback Consumer receiving the node ID
         * @return this builder instance
         */
        fun onDisconnection(callback: java.util.function.Consumer<String>) = apply {
            this.onDisconnection = { id -> callback.accept(id) }
        }

        /**
         * Sets the map of callbacks for handling incoming P2P messages by channel.
         *
         * @param callbacks map where keys are channel names and values are message handlers
         * @return this builder instance
         */
        fun onNewMsg(callbacks: ChannelMessageCallbacks) = apply {
            this.onNewMsg = callbacks
        }

        /**
         * Builds the [IncomingCrolangNodesCallbacks] instance.
         *
         * @return a new [IncomingCrolangNodesCallbacks]
         */
        fun build(): IncomingCrolangNodesCallbacks {
            return IncomingCrolangNodesCallbacks(
                onConnectionAttempt = onConnectionAttempt,
                onConnectionSuccess = onConnectionSuccess,
                onConnectionFailed = onConnectionFailed,
                onDisconnection = onDisconnection,
                onNewMsg = onNewMsg
            )
        }
    }
}
