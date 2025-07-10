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

/**
 * Configuration settings for P2P connections and messaging behavior.
 * 
 * This class allows you to customize various aspects of the P2P networking:
 * - Connection timeouts for establishing P2P links
 * - Message handling timeouts for large messages
 * - Automatic reconnection behavior
 * - Reconnection timing and retry limits
 * 
 * Use [CrolangSettingsJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangSettingsJs {
    
    private var p2pConnectionTimeoutMillis: Int = 30000
    private var multipartP2PMessageTimeoutMillis: Int = 60000
    private var reconnection: Boolean = true
    private var maxReconnectionAttempts: Int? = null
    private var reconnectionAttemptsDeltaMs: Int = 2000
    
    /**
     * Sets the timeout for P2P connection attempts in milliseconds.
     * 
     * This controls how long to wait for WebRTC connection establishment before giving up.
     * 
     * @param timeout Timeout in milliseconds (default: 30000)
     * @return This instance for method chaining
     */
    fun setP2pConnectionTimeoutMillis(timeout: Int): CrolangSettingsJs {
        this.p2pConnectionTimeoutMillis = timeout
        return this
    }

    /**
     * Gets the current P2P connection timeout setting.
     * 
     * @return Timeout in milliseconds
     */
    fun getP2pConnectionTimeoutMillis(): Int {
        return p2pConnectionTimeoutMillis
    }
    
    /**
     * Sets the timeout for multipart P2P messages in milliseconds.
     * 
     * Large messages are split into parts and reassembled on the receiving end.
     * This controls how long to wait for all parts before giving up.
     * 
     * @param timeout Timeout in milliseconds (default: 60000)
     * @return This instance for method chaining
     */
    fun setMultipartP2PMessageTimeoutMillis(timeout: Int): CrolangSettingsJs {
        this.multipartP2PMessageTimeoutMillis = timeout
        return this
    }

    /**
     * Gets the current multipart message timeout setting.
     * 
     * @return Timeout in milliseconds
     */
    fun getMultipartP2PMessageTimeoutMillis(): Int {
        return multipartP2PMessageTimeoutMillis
    }
    
    /**
     * Sets whether to attempt reconnection after disconnection.
     * 
     * When enabled, the library will automatically try to reconnect to the broker
     * if the connection is lost involuntarily.
     * 
     * @param reconnection true to enable auto-reconnection, false to disable
     * @return This instance for method chaining
     */
    fun setReconnection(reconnection: Boolean): CrolangSettingsJs {
        this.reconnection = reconnection
        return this
    }

    /**
     * Gets whether automatic reconnection is enabled.
     * 
     * @return true if auto-reconnection is enabled, false otherwise
     */
    fun isReconnectionEnabled(): Boolean {
        return reconnection
    }
    
    /**
     * Sets the maximum number of reconnection attempts.
     * 
     * When auto-reconnection is enabled, this limits how many times the library
     * will try to reconnect before giving up. Set to null for unlimited attempts.
     * 
     * @param maxAttempts Maximum number of attempts, or null for unlimited
     * @return This instance for method chaining
     */
    fun setMaxReconnectionAttempts(maxAttempts: Int?): CrolangSettingsJs {
        this.maxReconnectionAttempts = maxAttempts
        return this
    }

    /**
     * Gets the maximum number of reconnection attempts.
     * 
     * @return Maximum attempts, or null if unlimited
     */
    fun getMaxReconnectionAttempts(): Int? {
        return maxReconnectionAttempts
    }
    
    /**
     * Sets the time in milliseconds between reconnection attempts.
     * 
     * This controls the delay between consecutive reconnection attempts
     * when auto-reconnection is enabled.
     * 
     * @param deltaMs Time in milliseconds between attempts (default: 2000)
     * @return This instance for method chaining
     */
    fun setReconnectionAttemptsDeltaMs(deltaMs: Int): CrolangSettingsJs {
        this.reconnectionAttemptsDeltaMs = deltaMs
        return this
    }

    /**
     * Gets the time between reconnection attempts.
     * 
     * @return Time in milliseconds between attempts
     */
    fun getReconnectionAttemptsDeltaMs(): Int {
        return reconnectionAttemptsDeltaMs
    }

}

/**
 * Builder for creating [CrolangSettingsJs] instances.
 * 
 * This builder provides a convenient way to create settings objects
 * with sensible default values.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangSettingsJsBuilder {

    /**
     * Creates a new [CrolangSettingsJs] instance with default settings.
     * 
     * @return A new settings instance ready for customization
     */
    fun create(): CrolangSettingsJs {
        return CrolangSettingsJs()
    }

}
