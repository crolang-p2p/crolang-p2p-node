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

import java.util.*

private const val DEFAULT_P2P_NODES_CONNECTION_TIMEOUT_MILLIS: Long = 30000
private const val DEFAULT_MULTIPART_P2P_MESSAGE_TIMEOUT_MILLIS: Long = 60000
private const val DEFAULT_RECONNECTION: Boolean = true
private const val DEFAULT_RECONNECTION_ATTEMPTS_DELTA_MS: Long = 2000

/**
 * Class representing settings for the Crolang library.
 *
 * @property p2pConnectionTimeoutMillis Timeout for P2P connection attempts in milliseconds; 30000 by default.
 * @property multipartP2PMessageTimeoutMillis Timeout for multipart P2P messages in milliseconds
 * (if a message is too big it gets automatically split into smaller messages; if all the parts are not delivered in time, a timeout occurs); 60000 by default.
 * @property reconnection Flag indicating whether to attempt reconnection after disconnection; true by default.
 * @property maxReconnectionAttempts Optional maximum number of reconnection attempts; if not set, the default is empty (no limit). Depends on the reconnection flag.
 * @property reconnectionAttemptsDeltaMs Time in milliseconds between reconnection attempts; 2000 by default. Depends on the reconnection flag.
 *
 * @see BrokerConnectionAdditionalParameters
 */
class CrolangSettings @JvmOverloads constructor(
    /**
     * Timeout for P2P connection attempts in milliseconds; 30000 by default.
     */
    val p2pConnectionTimeoutMillis: Long = DEFAULT_P2P_NODES_CONNECTION_TIMEOUT_MILLIS,

    /**
     * Timeout for multipart P2P messages in milliseconds
     * (if a message is too big it gets automatically split into smaller messages;
     * if all the parts are not delivered in time, a timeout occurs); 60000 by default.
     */
    val multipartP2PMessageTimeoutMillis: Long = DEFAULT_MULTIPART_P2P_MESSAGE_TIMEOUT_MILLIS,

    /**
     * Flag indicating whether to attempt reconnection after disconnection; true by default.
     */
    val reconnection: Boolean = DEFAULT_RECONNECTION,

    /**
     * Optional maximum number of reconnection attempts; if not set, the default is empty (no limit). Depends on the reconnection flag.
     */
    val maxReconnectionAttempts: Optional<Int> = Optional.empty(),

    /**
     * Time in milliseconds between reconnection attempts; 2000 by default. Depends on the reconnection flag.
     */
    val reconnectionAttemptsDeltaMs: Long = DEFAULT_RECONNECTION_ATTEMPTS_DELTA_MS
) {

    /**
     * Companion object used to access a [Builder] to create a [CrolangSettings] object.
     */
    companion object {
        /**
         * Returns a new [Builder] for constructing a [CrolangSettings] instance.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [CrolangSettings] in a Java-friendly way.
     */
    class Builder {
        private var p2pConnectionTimeoutMillis: Long = DEFAULT_P2P_NODES_CONNECTION_TIMEOUT_MILLIS
        private var multipartP2PMessageTimeoutMillis: Long = DEFAULT_MULTIPART_P2P_MESSAGE_TIMEOUT_MILLIS
        private var reconnection: Boolean = DEFAULT_RECONNECTION
        private var maxReconnectionAttempts: Optional<Int> = Optional.empty()
        private var reconnectionAttemptsDeltaMs: Long = DEFAULT_RECONNECTION_ATTEMPTS_DELTA_MS

        /**
         * Sets the timeout for P2P connection attempts.
         *
         * @param millis timeout in milliseconds
         * @return this builder instance
         */
        fun p2pConnectionTimeoutMillis(millis: Long) = apply {
            this.p2pConnectionTimeoutMillis = millis
        }

        /**
         * Sets the timeout for multipart P2P messages.
         *
         * @param millis timeout in milliseconds
         * @return this builder instance
         */
        fun multipartP2PMessageTimeoutMillis(millis: Long) = apply {
            this.multipartP2PMessageTimeoutMillis = millis
        }

        /**
         * Sets whether to attempt reconnection after disconnection.
         *
         * @param value true to enable reconnection
         * @return this builder instance
         */
        fun reconnection(value: Boolean) = apply {
            this.reconnection = value
        }

        /**
         * Sets the optional maximum number of reconnection attempts.
         *
         * @param attempts maximum number of attempts, or empty if no limit is set
         * @return this builder instance
         */
        fun maxReconnectionAttempts(attempts: Optional<Int>) = apply {
            this.maxReconnectionAttempts = attempts
        }

        /**
         * Sets the delay between reconnection attempts.
         *
         * @param millis delay in milliseconds
         * @return this builder instance
         */
        fun reconnectionAttemptsDeltaMs(millis: Long) = apply {
            this.reconnectionAttemptsDeltaMs = millis
        }

        /**
         * Builds the [CrolangSettings] instance.
         *
         * @return a new [CrolangSettings]
         */
        fun build(): CrolangSettings {
            return CrolangSettings(
                p2pConnectionTimeoutMillis,
                multipartP2PMessageTimeoutMillis,
                reconnection,
                maxReconnectionAttempts,
                reconnectionAttemptsDeltaMs
            )
        }
    }
}
