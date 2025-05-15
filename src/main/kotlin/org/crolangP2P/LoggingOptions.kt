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
 * Options for logging in the Crolang library.
 *
 * @property enableBaseLogging If true, enables base logging for the Crolang library; base logs involve connections, messages received, etc...
 * @property enableDebugLogging If true, enables debug logging for the Crolang library; debug logging is more verbose and includes detailed information about the internal workings of the library.
 */
class LoggingOptions @JvmOverloads constructor(
    /**
     * If true, enables base logging for the Crolang library; base logs involve connections, messages received, etc...
     */
    val enableBaseLogging: Boolean = false,

    /**
     * If true, enables debug logging for the Crolang library; debug logging is more verbose and includes detailed information about the internal workings of the library.
     */
    val enableDebugLogging: Boolean = false
) {

    /**
     * Companion object used to access a [Builder] to create a [LoggingOptions] object.
     */
    companion object {
        /**
         * Returns a new Builder instance to construct a [LoggingOptions] object.
         *
         * @return a new [Builder]
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    /**
     * Builder class for constructing [LoggingOptions] in a Java-friendly way.
     */
    class Builder {
        private var enableBaseLogging: Boolean = false
        private var enableDebugLogging: Boolean = false

        /**
         * Sets whether base logging should be enabled.
         *
         * @param enable true to enable base logging
         * @return this builder instance
         */
        fun enableBaseLogging(enable: Boolean) = apply {
            this.enableBaseLogging = enable
        }

        /**
         * Sets whether debug logging should be enabled.
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
            return LoggingOptions(
                enableBaseLogging,
                enableDebugLogging
            )
        }
    }
}

