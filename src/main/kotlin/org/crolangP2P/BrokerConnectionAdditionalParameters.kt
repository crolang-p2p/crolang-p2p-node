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
 * Class representing additional parameters for a Broker connection.
 *
 * @property onConnectionAttemptData Optional data to be passed, used for authentication to the Broker.
 * @property lifecycleCallbacks Callbacks for various lifecycle events of the Broker reconnection/disconnection.
 * @property settings Settings for the Crolang P2P library.
 * @property logging Logging options for the Crolang P2P library.
 */
class BrokerConnectionAdditionalParameters @JvmOverloads constructor(
    /**
     * Optional data to be passed, used for authentication to the Broker.
     */
    val onConnectionAttemptData: String? = null,

    /**
     * Callbacks for various lifecycle events of the Broker reconnection/disconnection.
     */
    val lifecycleCallbacks: BrokerLifecycleCallbacks = BrokerLifecycleCallbacks(),

    /**
     * Settings for the Crolang P2P library.
     */
    val settings: CrolangSettings = CrolangSettings(),

    /**
     * Logging options for the Crolang library.
     */
    val logging: LoggingOptions = LoggingOptions()
) {

    /**
     * Companion object used to access a [Builder] to create a [BrokerConnectionAdditionalParameters] object.
     */
    companion object {
        /**
         * Returns a new Builder instance to construct a [BrokerConnectionAdditionalParameters] object.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [BrokerConnectionAdditionalParameters] in a Java-friendly way.
     */
    class Builder {

        private var onConnectionAttemptData: String? = null
        private var lifecycleCallbacks: BrokerLifecycleCallbacks = BrokerLifecycleCallbacks()
        private var settings: CrolangSettings = CrolangSettings()
        private var logging: LoggingOptions = LoggingOptions()

        /**
         * Sets optional data to be passed for authentication to the Broker.
         *
         * @param data authentication data
         * @return this builder instance
         */
        fun onConnectionAttemptData(data: String?) = apply {
            this.onConnectionAttemptData = data
        }

        /**
         * Sets the callbacks for various lifecycle events of the Broker reconnection/disconnection.
         *
         * @param callbacks the [BrokerLifecycleCallbacks] instance
         * @return this builder instance
         */
        fun lifecycleCallbacks(callbacks: BrokerLifecycleCallbacks) = apply {
            this.lifecycleCallbacks = callbacks
        }

        /**
         * Sets the Crolang settings.
         *
         * @param settings the [CrolangSettings] instance
         * @return this builder instance
         */
        fun settings(settings: CrolangSettings) = apply {
            this.settings = settings
        }

        /**
         * Sets the logging options.
         *
         * @param logging the [LoggingOptions] instance
         * @return this builder instance
         */
        fun logging(logging: LoggingOptions) = apply {
            this.logging = logging
        }

        /**
         * Builds the [BrokerConnectionAdditionalParameters] instance.
         *
         * @return a new [BrokerConnectionAdditionalParameters]
         */
        fun build(): BrokerConnectionAdditionalParameters {
            return BrokerConnectionAdditionalParameters(
                onConnectionAttemptData,
                lifecycleCallbacks,
                settings,
                logging
            )
        }
    }
}

