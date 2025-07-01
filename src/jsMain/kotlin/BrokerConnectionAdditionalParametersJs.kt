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
class BrokerConnectionAdditionalParametersJs {
    
    private var lifecycleCallbacks: BrokerLifecycleCallbacksJs = BrokerLifecycleCallbacksJs()
    private var settings: CrolangSettingsJs = CrolangSettingsJs()
    private var logging: LoggingOptionsJs = LoggingOptionsJs()
    
    /**
     * Sets the lifecycle callbacks for the broker connection.
     */
    fun setLifecycleCallbacks(
        lifecycleCallbacks: BrokerLifecycleCallbacksJs
    ): BrokerConnectionAdditionalParametersJs {
        this.lifecycleCallbacks = lifecycleCallbacks
        return this
    }

    fun getLifecycleCallbacks(): BrokerLifecycleCallbacksJs {
        return lifecycleCallbacks
    }
    
    /**
     * Sets the settings for the Crolang P2P library.
     */
    fun setSettings(settings: CrolangSettingsJs): BrokerConnectionAdditionalParametersJs {
        this.settings = settings
        return this
    }

    fun getSettings(): CrolangSettingsJs {
        return settings
    }
    
    /**
     * Sets the logging options for the Crolang P2P library.
     */
    fun setLogging(logging: LoggingOptionsJs): BrokerConnectionAdditionalParametersJs {
        this.logging = logging
        return this
    }

    fun getLogging(): LoggingOptionsJs {
        return logging
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
object BrokerConnectionAdditionalParametersJsBuilder {

    fun create(): BrokerConnectionAdditionalParametersJs {
        return BrokerConnectionAdditionalParametersJs()
    }

}
