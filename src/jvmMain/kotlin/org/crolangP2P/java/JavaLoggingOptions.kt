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

import org.crolangP2P.LoggingOptions

/**
 * Java-friendly builder pattern for [LoggingOptions].
 */
class JavaLoggingOptions {
    
    /**
     * Factory methods for creating JavaLoggingOptions instances.
     */
    companion object {
        /**
         * Creates a new builder instance.
         */
        @JvmStatic
        fun builder(): JavaLoggingOptions = JavaLoggingOptions()
    }
    
    private var enableBaseLogging: Boolean = false
    private var enableDebugLogging: Boolean = false

    /**
     * Sets whether to enable base logging.
     *
     * @param enable true to enable base logging
     * @return this builder instance
     */
    fun enableBaseLogging(enable: Boolean) = apply {
        this.enableBaseLogging = enable
    }

    /**
     * Sets whether to enable debug logging.
     *
     * @param enable true to enable debug logging
     * @return this builder instance
     */
    fun enableDebugLogging(enable: Boolean) = apply {
        this.enableDebugLogging = enable
    }

    /**
     * Builds the [LoggingOptions] instance.
     *
     * @return a new [LoggingOptions]
     */
    fun build(): LoggingOptions {
        return LoggingOptions(enableBaseLogging, enableDebugLogging)
    }
}
