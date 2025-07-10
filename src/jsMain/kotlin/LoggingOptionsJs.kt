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
 * Configuration options for library logging and debugging.
 * 
 * This class allows you to control the verbosity of library logging:
 * - Base logging: Essential operational messages and errors
 * - Debug logging: Detailed internal operations for troubleshooting
 * 
 * Use [LoggingOptionsJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class LoggingOptionsJs {
    
    private var enableBaseLogging: Boolean = false
    private var enableDebugLogging: Boolean = false
    
    /**
     * Enables or disables base logging for the Crolang library.
     * 
     * Base logging includes essential operational messages such as connection
     * establishment, disconnections, and important errors.
     * 
     * @param enable true to enable base logging, false to disable
     * @return This instance for method chaining
     */
    fun setEnableBaseLogging(enable: Boolean): LoggingOptionsJs {
        this.enableBaseLogging = enable
        return this
    }

    /**
     * Gets whether base logging is currently enabled.
     * 
     * @return true if base logging is enabled, false otherwise
     */
    fun isBaseLoggingEnabled(): Boolean {
        return enableBaseLogging
    }
    
    /**
     * Enables or disables debug logging for the Crolang library.
     * 
     * Debug logging includes detailed internal operations, protocol messages,
     * and comprehensive troubleshooting information. Only enable when needed
     * as it can be quite verbose.
     * 
     * @param enable true to enable debug logging, false to disable
     * @return This instance for method chaining
     */
    fun setEnableDebugLogging(enable: Boolean): LoggingOptionsJs {
        this.enableDebugLogging = enable
        return this
    }

    /**
     * Gets whether debug logging is currently enabled.
     * 
     * @return true if debug logging is enabled, false otherwise
     */
    fun isDebugLoggingEnabled(): Boolean {
        return enableDebugLogging
    }

}

/**
 * Builder for creating [LoggingOptionsJs] instances.
 * 
 * This builder provides a convenient way to create logging configuration objects
 * with default settings (both logging types disabled).
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object LoggingOptionsJsBuilder {

    /**
     * Creates a new [LoggingOptionsJs] instance with default settings.
     * 
     * @return A new logging configuration instance ready for customization
     */
    fun create(): LoggingOptionsJs {
        return LoggingOptionsJs()
    }

}
