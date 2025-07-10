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
 * Configuration parameters for establishing a connection to the CrolangP2P broker.
 * 
 * This class allows you to configure various aspects of the broker connection including:
 * - Lifecycle callbacks for connection events (disconnection, reconnection)
 * - P2P connection and messaging settings (timeouts, reconnection behavior)
 * - Logging options for debugging and monitoring
 * 
 * Use [BrokerConnectionAdditionalParametersJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class BrokerConnectionAdditionalParametersJs {
    
    private var lifecycleCallbacks: BrokerLifecycleCallbacksJs = BrokerLifecycleCallbacksJs()
    private var settings: CrolangSettingsJs = CrolangSettingsJs()
    private var logging: LoggingOptionsJs = LoggingOptionsJs()
    
    /**
     * Sets the lifecycle callbacks for the broker connection.
     * 
     * @param lifecycleCallbacks The callbacks to handle broker connection lifecycle events
     * @return This instance for method chaining
     */
    fun setLifecycleCallbacks(
        lifecycleCallbacks: BrokerLifecycleCallbacksJs
    ): BrokerConnectionAdditionalParametersJs {
        this.lifecycleCallbacks = lifecycleCallbacks
        return this
    }

    /**
     * Gets the currently configured lifecycle callbacks.
     * 
     * @return The configured lifecycle callbacks
     */
    fun getLifecycleCallbacks(): BrokerLifecycleCallbacksJs {
        return lifecycleCallbacks
    }
    
    /**
     * Sets the settings for the Crolang P2P library.
     * 
     * @param settings The P2P connection and messaging settings
     * @return This instance for method chaining
     */
    fun setSettings(settings: CrolangSettingsJs): BrokerConnectionAdditionalParametersJs {
        this.settings = settings
        return this
    }

    /**
     * Gets the currently configured P2P settings.
     * 
     * @return The configured P2P settings
     */
    fun getSettings(): CrolangSettingsJs {
        return settings
    }
    
    /**
     * Sets the logging options for the Crolang P2P library.
     * 
     * @param logging The logging configuration for debugging and monitoring
     * @return This instance for method chaining
     */
    fun setLogging(logging: LoggingOptionsJs): BrokerConnectionAdditionalParametersJs {
        this.logging = logging
        return this
    }

    /**
     * Gets the currently configured logging options.
     * 
     * @return The configured logging options
     */
    fun getLogging(): LoggingOptionsJs {
        return logging
    }

}

/**
 * Builder for creating [BrokerConnectionAdditionalParametersJs] instances.
 * 
 * This builder provides a convenient way to create configuration objects
 * for broker connections with default values.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object BrokerConnectionAdditionalParametersJsBuilder {

    /**
     * Creates a new [BrokerConnectionAdditionalParametersJs] instance with default values.
     * 
     * @return A new configuration instance ready for customization
     */
    fun create(): BrokerConnectionAdditionalParametersJs {
        return BrokerConnectionAdditionalParametersJs()
    }

}
