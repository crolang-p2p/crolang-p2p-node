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

@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangSettingsJs {
    
    private var p2pConnectionTimeoutMillis: Long = 30000
    private var multipartP2PMessageTimeoutMillis: Long = 60000
    private var reconnection: Boolean = true
    private var maxReconnectionAttempts: Int? = null
    private var reconnectionAttemptsDeltaMs: Long = 2000
    
    /**
     * Sets the timeout for P2P connection attempts in milliseconds.
     */
    fun setP2pConnectionTimeoutMillis(timeout: Long): CrolangSettingsJs {
        this.p2pConnectionTimeoutMillis = timeout
        return this
    }

    fun getP2pConnectionTimeoutMillis(): Long {
        return p2pConnectionTimeoutMillis
    }
    
    /**
     * Sets the timeout for multipart P2P messages in milliseconds.
     */
    fun setMultipartP2PMessageTimeoutMillis(timeout: Long): CrolangSettingsJs {
        this.multipartP2PMessageTimeoutMillis = timeout
        return this
    }

    fun getMultipartP2PMessageTimeoutMillis(): Long {
        return multipartP2PMessageTimeoutMillis
    }
    
    /**
     * Sets whether to attempt reconnection after disconnection.
     */
    fun setReconnection(reconnection: Boolean): CrolangSettingsJs {
        this.reconnection = reconnection
        return this
    }

    fun isReconnectionEnabled(): Boolean {
        return reconnection
    }
    
    /**
     * Sets the maximum number of reconnection attempts.
     */
    fun setMaxReconnectionAttempts(maxAttempts: Int?): CrolangSettingsJs {
        this.maxReconnectionAttempts = maxAttempts
        return this
    }

    fun getMaxReconnectionAttempts(): Int? {
        return maxReconnectionAttempts
    }
    
    /**
     * Sets the time in milliseconds between reconnection attempts.
     */
    fun setReconnectionAttemptsDeltaMs(deltaMs: Long): CrolangSettingsJs {
        this.reconnectionAttemptsDeltaMs = deltaMs
        return this
    }

    fun getReconnectionAttemptsDeltaMs(): Long {
        return reconnectionAttemptsDeltaMs
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangSettingsJsBuilder {

    fun create(): CrolangSettingsJs {
        return CrolangSettingsJs()
    }

}
