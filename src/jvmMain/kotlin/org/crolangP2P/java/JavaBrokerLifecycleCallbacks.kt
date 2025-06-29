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

package org.crolangP2P.java

import org.crolangP2P.BrokerLifecycleCallbacks
import org.crolangP2P.InvoluntaryBrokerDisconnectionCause

/**
 * Java-friendly builder pattern for [BrokerLifecycleCallbacks].
 */
class JavaBrokerLifecycleCallbacks {
    
    /**
     * Factory methods for creating JavaBrokerLifecycleCallbacks instances.
     */
    companion object {
        /**
         * Creates a new builder instance.
         */
        @JvmStatic
        fun builder(): JavaBrokerLifecycleCallbacks = JavaBrokerLifecycleCallbacks()
    }
    
    private var onInvoluntaryDisconnection: (InvoluntaryBrokerDisconnectionCause) -> Unit = {}
    private var onReconnectionAttempt: () -> Unit = {}
    private var onSuccessfullyReconnected: () -> Unit = {}

    /**
     * Sets the callback invoked when there's an involuntary disconnection from the broker.
     *
     * @param callback Consumer receiving the disconnection cause
     * @return this builder instance
     */
    fun onInvoluntaryDisconnection(callback: java.util.function.Consumer<InvoluntaryBrokerDisconnectionCause>) = apply {
        this.onInvoluntaryDisconnection = { cause -> callback.accept(cause) }
    }

    /**
     * Sets the callback invoked when a reconnection attempt is made.
     *
     * @param callback Runnable to execute on reconnection attempt
     * @return this builder instance
     */
    fun onReconnectionAttempt(callback: Runnable) = apply {
        this.onReconnectionAttempt = { callback.run() }
    }

    /**
     * Sets the callback invoked when successfully reconnected.
     *
     * @param callback Runnable to execute on successful reconnection
     * @return this builder instance
     */
    fun onSuccessfullyReconnected(callback: Runnable) = apply {
        this.onSuccessfullyReconnected = { callback.run() }
    }

    /**
     * Builds the [BrokerLifecycleCallbacks] instance.
     *
     * @return a new [BrokerLifecycleCallbacks]
     */
    fun build(): BrokerLifecycleCallbacks {
        return BrokerLifecycleCallbacks(onInvoluntaryDisconnection, onReconnectionAttempt, onSuccessfullyReconnected)
    }
}
