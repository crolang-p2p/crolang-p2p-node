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

/**
 * This class contains callbacks that are invoked during the lifecycle of the Broker connection.
 * These callbacks are useful for handling events related to involuntary disconnections from the Broker.
 * The callbacks are executed asynchronously on an executor service.
 *
 * The reconnection behavior is controlled by the BrokerConnectionAdditionalParameters passed when connecting to the Broker.
 *
 * @param onInvoluntaryDisconnection Callback invoked when the local Node is involuntarily disconnected from the Broker and no more
 * reconnection attempts will be made.
 * @param onReconnectionAttempt Callback invoked when a reconnection attempt is made after an involuntary disconnection.
 * @param onSuccessfullyReconnected Callback invoked when the local Node successfully reconnects to the Broker.
 *
 * @see BrokerConnectionAdditionalParameters
 * @see InvoluntaryBrokerDisconnectionCause
 */
class BrokerLifecycleCallbacks @JvmOverloads constructor(

    /**
     * Callback invoked when the local Node is involuntarily disconnected from the Broker and no more reconnection attempts
     * will be made, meaning that the Node is no longer connected to the Broker; the callback is executed asynchronously
     * on an executor service.
     *
     * If the CrolangP2P.disconnectFromBroker() method is called, this callback will not be invoked since the
     * disconnection is voluntary.
     *
     * The callback provides the cause of the disconnection, which can be used to handle specific scenarios.
     *
     * This will be called only after the reconnection attempts have been exhausted or the reconnection is disabled in the
     * BrokerConnectionAdditionalParameters.
     *
     * An involuntary disconnection can occur due to various reasons, such as network issues or the Broker decides
     * to disconnect the Node due to business logics defined in the Broker's custom webhook set by the
     * NODES_VALIDITY_CHECK_WEBHOOK_URL environment variable.
     *
     * @see InvoluntaryBrokerDisconnectionCause
     * @see BrokerConnectionAdditionalParameters
     */
    val onInvoluntaryDisconnection: (cause: InvoluntaryBrokerDisconnectionCause) -> Unit = {},

    /**
     * Callback invoked when the local Node is involuntarily disconnected from the Broker and tries to reconnect;
     * the callback is executed asynchronously on an executor service.
     *
     * If the CrolangP2P.disconnectFromBroker() method is called, this callback will not be invoked since the
     * disconnection is voluntary.
     *
     * This callback is useful for logging or notifying the user about the reconnection attempt.
     *
     * The reconnection attempt is made only if the reconnection is enabled in the BrokerConnectionAdditionalParameters and
     * will be performed until the maximum number of reconnection attempts or the reconnection is impossible
     * due to the Node being unauthorized or already connected to the Broker (another Node is using the same ID).
     */
    val onReconnectionAttempt: () -> Unit = {},

    /**
     * Callback invoked when the local Node successfully reconnects to the Broker after a reconnection attempt due to being
     * involuntarily disconnected; the callback is executed asynchronously on an executor service.
     *
     * If the CrolangP2P.disconnectFromBroker() method is called, this callback will not be invoked since the
     * disconnection is voluntary.
     */
    val onSuccessfullyReconnected: () -> Unit = {}
) {

    /**
     * Companion object to provide a static builder for creating [BrokerLifecycleCallbacks] instances.
     */
    companion object {
        /**
         * Returns a new Builder instance to construct a [BrokerLifecycleCallbacks] object.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [BrokerLifecycleCallbacks] in a Java-friendly way.
     */
    class Builder {
        private var onInvoluntaryDisconnection: (InvoluntaryBrokerDisconnectionCause) -> Unit = {}
        private var onReconnectionAttempt: () -> Unit = {}
        private var onSuccessfullyReconnected: () -> Unit = {}

        /**
         * Sets the callback invoked when the local Node is involuntarily disconnected from the Broker and no more
         * reconnection attempts will be made. The callback receives the disconnection cause and is executed asynchronously.
         *
         * @param callback the disconnection handler
         * @return this builder instance
         *
         * @see InvoluntaryBrokerDisconnectionCause
         * @see BrokerConnectionAdditionalParameters
         */
        fun onInvoluntaryDisconnection(callback: (InvoluntaryBrokerDisconnectionCause) -> Unit) = apply {
            this.onInvoluntaryDisconnection = callback
        }

        /**
         * Sets the callback invoked when a reconnection attempt is made after an involuntary disconnection.
         * The callback is executed asynchronously on an executor service.
         *
         * @param callback the reconnection attempt handler
         * @return this builder instance
         */
        fun onReconnectionAttempt(callback: () -> Unit) = apply {
            this.onReconnectionAttempt = callback
        }

        /**
         * Sets the callback invoked when the local Node successfully reconnects to the Broker after an involuntary disconnection.
         * The callback is executed asynchronously on an executor service.
         *
         * @param callback the successful reconnection handler
         * @return this builder instance
         */
        fun onSuccessfullyReconnected(callback: () -> Unit) = apply {
            this.onSuccessfullyReconnected = callback
        }

        /**
         * Builds the [BrokerLifecycleCallbacks] instance.
         *
         * @return a new [BrokerLifecycleCallbacks]
         */
        fun build(): BrokerLifecycleCallbacks {
            return BrokerLifecycleCallbacks(
                onInvoluntaryDisconnection,
                onReconnectionAttempt,
                onSuccessfullyReconnected
            )
        }
    }
}
