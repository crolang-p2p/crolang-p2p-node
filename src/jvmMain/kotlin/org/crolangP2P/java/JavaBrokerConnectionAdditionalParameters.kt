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

import org.crolangP2P.BrokerConnectionAdditionalParameters
import org.crolangP2P.BrokerLifecycleCallbacks
import org.crolangP2P.CrolangSettings
import org.crolangP2P.LoggingOptions

/**
 * Java-friendly builder pattern for [BrokerConnectionAdditionalParameters].
 */
class JavaBrokerConnectionAdditionalParameters {
    
    /**
     * Factory methods for creating JavaBrokerConnectionAdditionalParameters instances.
     */
    companion object {
        /**
         * Creates a new builder instance.
         */
        @JvmStatic
        fun builder(): JavaBrokerConnectionAdditionalParameters = JavaBrokerConnectionAdditionalParameters()
    }
    
    private var lifecycleCallbacks: BrokerLifecycleCallbacks = BrokerLifecycleCallbacks()
    private var settings: CrolangSettings = CrolangSettings()
    private var logging: LoggingOptions = LoggingOptions()

    /**
     * Sets the lifecycle callbacks for broker events.
     *
     * @param callbacks the lifecycle callbacks
     * @return this builder instance
     */
    fun lifecycleCallbacks(callbacks: BrokerLifecycleCallbacks) = apply {
        this.lifecycleCallbacks = callbacks
    }

    /**
     * Sets the settings for the Crolang P2P library.
     *
     * @param settings the settings
     * @return this builder instance
     */
    fun settings(settings: CrolangSettings) = apply {
        this.settings = settings
    }

    /**
     * Sets the logging options.
     *
     * @param logging the logging options
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
        return BrokerConnectionAdditionalParameters(lifecycleCallbacks, settings, logging)
    }
}
