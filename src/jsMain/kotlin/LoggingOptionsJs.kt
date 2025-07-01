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
class LoggingOptionsJs {
    
    private var enableBaseLogging: Boolean = false
    private var enableDebugLogging: Boolean = false
    
    /**
     * Enables or disables base logging for the Crolang library.
     */
    fun setEnableBaseLogging(enable: Boolean): LoggingOptionsJs {
        this.enableBaseLogging = enable
        return this
    }

    fun isBaseLoggingEnabled(): Boolean {
        return enableBaseLogging
    }
    
    /**
     * Enables or disables debug logging for the Crolang library.
     */
    fun setEnableDebugLogging(enable: Boolean): LoggingOptionsJs {
        this.enableDebugLogging = enable
        return this
    }

    fun isDebugLoggingEnabled(): Boolean {
        return enableDebugLogging
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
object LoggingOptionsJsBuilder {

    fun create(): LoggingOptionsJs {
        return LoggingOptionsJs()
    }

}
