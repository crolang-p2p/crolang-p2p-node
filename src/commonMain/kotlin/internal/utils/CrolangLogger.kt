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

package internal.utils

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import internal.utils.SharedStore
import org.crolangP2P.LoggingOptions

/**
 * Logger utility for Crolang that provides timestamped logging messages.
 * It supports both regular and debug logs for standard output and error streams.
 *
 * @param loggingOptions Configuration object that enables or disables specific logging levels.
 */
internal class CrolangLogger(private val loggingOptions: LoggingOptions) {

    private val logger = Logger(
        config = StaticConfig(
            logWriterList = listOf(platformLogWriter())
        ),
        tag = "Crolang"
    )

    /**
     * Generates a timestamp string using the injected TimestampProvider.
     * @return A formatted timestamp enclosed in square brackets.
     */
    private fun timestamp(): String {
        return SharedStore.dependencies!!.timestampProvider.getCurrentTimestamp()
    }

    /**
     * Logs an informational message to standard output if base logging is enabled.
     * @param msg The message to log.
     */
    fun regularInfo(msg: String) {
        if (loggingOptions.enableBaseLogging) {
            println("${timestamp()} Crolang info: $msg")
        }
    }

    /**
     * Logs a debug informational message to standard output if debug logging is enabled.
     * @param msg The debug message to log.
     */
    fun debugInfo(msg: String) {
        if (loggingOptions.enableDebugLogging) {
            println("${timestamp()} Crolang DEBUG info: $msg")
        }
    }

    /**
     * Logs an error message to standard error if base logging is enabled.
     * @param msg The error message to log.
     */
    fun regularErr(msg: String) {
        if (loggingOptions.enableBaseLogging) {
            logger.e { "${timestamp()}: $msg" }
        }
    }

    /**
     * Logs a debug error message to standard error if debug logging is enabled.
     * @param msg The debug error message to log.
     */
    fun debugErr(msg: String) {
        if (loggingOptions.enableDebugLogging) {
            logger.e { "${timestamp()}[DEBUG]: $msg" }
        }
    }
}
