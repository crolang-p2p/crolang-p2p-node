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

@file:JvmName("ConnectionCallbacksJavaKt")
package org.crolangP2P.java

import org.crolangP2P.SyncCrolangNodeCallbacks
import org.crolangP2P.AsyncCrolangNodeCallbacks
import org.crolangP2P.ChannelMessageCallbacks
import org.crolangP2P.CrolangNode
import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException

/**
 * Java-friendly builders for Kotlin callback classes.
 * These are specific to the JVM target and provide an idiomatic Java API for connection callbacks.
 */

/**
 * Java-friendly builder pattern for [SyncCrolangNodeCallbacks].
 */
class JavaSyncCrolangNodeCallbacks {
    
    /**
     * Factory methods for creating JavaSyncCrolangNodeCallbacks instances.
     */
    companion object {
        /**
         * Creates a new builder instance.
         */
        @JvmStatic
        fun builder(): JavaSyncCrolangNodeCallbacks = JavaSyncCrolangNodeCallbacks()
    }
    
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

/**
 * Java-friendly builder pattern for [AsyncCrolangNodeCallbacks].
 */
class JavaAsyncCrolangNodeCallbacks {
    
    /**
     * Factory methods for creating JavaAsyncCrolangNodeCallbacks instances.
     */
    companion object {
        /**
         * Creates a new builder instance.
         */
        @JvmStatic
        fun builder(): JavaAsyncCrolangNodeCallbacks = JavaAsyncCrolangNodeCallbacks()
    }
    
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
    fun onConnectionSuccess(callback: java.util.function.Consumer<CrolangNode>) = apply {
        this.onConnectionSuccess = { node -> callback.accept(node) }
    }

    /**
     * Sets the callback invoked when the node connection fails.
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