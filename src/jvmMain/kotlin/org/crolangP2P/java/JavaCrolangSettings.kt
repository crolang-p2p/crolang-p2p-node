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

import org.crolangP2P.CrolangSettings

/**
 * Java-friendly builder pattern for [CrolangSettings].
 */
class JavaCrolangSettings {
    
    /**
     * Factory methods for creating JavaCrolangSettings instances.
     */
    companion object {
        /**
         * Creates a new builder instance.
         */
        @JvmStatic
        fun builder(): JavaCrolangSettings = JavaCrolangSettings()
    }
    
    private var p2pConnectionTimeoutMillis: Long = 30000L
    private var multipartP2PMessageTimeoutMillis: Long = 60000L
    private var reconnection: Boolean = true
    private var maxReconnectionAttempts: Int? = null
    private var reconnectionAttemptsDeltaMs: Long = 2000L

    /**
     * Sets the P2P connection timeout in milliseconds.
     *
     * @param timeout the timeout in milliseconds
     * @return this builder instance
     */
    fun p2pConnectionTimeoutMillis(timeout: Long) = apply {
        this.p2pConnectionTimeoutMillis = timeout
    }

    /**
     * Sets the multipart P2P message timeout in milliseconds.
     *
     * @param timeout the timeout in milliseconds
     * @return this builder instance
     */
    fun multipartP2PMessageTimeoutMillis(timeout: Long) = apply {
        this.multipartP2PMessageTimeoutMillis = timeout
    }

    /**
     * Sets whether reconnection is enabled.
     *
     * @param enable true to enable reconnection
     * @return this builder instance
     */
    fun reconnection(enable: Boolean) = apply {
        this.reconnection = enable
    }

    /**
     * Sets the maximum number of reconnection attempts.
     *
     * @param maxAttempts the maximum attempts, or empty for unlimited
     * @return this builder instance
     */
    fun maxReconnectionAttempts(maxAttempts: java.util.Optional<Int>) = apply {
        this.maxReconnectionAttempts = if (maxAttempts.isPresent) maxAttempts.get() else null
    }

    /**
     * Sets the delta time between reconnection attempts in milliseconds.
     *
     * @param delta the delta time in milliseconds
     * @return this builder instance
     */
    fun reconnectionAttemptsDeltaMs(delta: Long) = apply {
        this.reconnectionAttemptsDeltaMs = delta
    }

    /**
     * Builds the [CrolangSettings] instance.
     *
     * @return a new [CrolangSettings]
     */
    fun build(): CrolangSettings {
        return CrolangSettings(
            p2pConnectionTimeoutMillis = p2pConnectionTimeoutMillis,
            multipartP2PMessageTimeoutMillis = multipartP2PMessageTimeoutMillis,
            reconnection = reconnection,
            maxReconnectionAttempts = maxReconnectionAttempts,
            reconnectionAttemptsDeltaMs = reconnectionAttemptsDeltaMs
        )
    }
}
