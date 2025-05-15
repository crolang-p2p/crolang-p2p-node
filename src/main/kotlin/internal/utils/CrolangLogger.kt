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

import org.crolangP2P.LoggingOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Logger utility for Crolang that provides timestamped logging messages.
 * It supports both regular and debug logs for standard output and error streams.
 *
 * @param loggingOptions Configuration object that enables or disables specific logging levels.
 */
internal class CrolangLogger(private val loggingOptions: LoggingOptions) {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    /**
     * Generates a timestamp string formatted according to the specified pattern.
     * @return A formatted timestamp enclosed in square brackets.
     */
    private fun timestamp(): String {
        return "[${LocalDateTime.now().format(formatter)}]"
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
            System.err.println("${timestamp()} Crolang error: $msg")
        }
    }

    /**
     * Logs a debug error message to standard error if debug logging is enabled.
     * @param msg The debug error message to log.
     */
    fun debugErr(msg: String) {
        if (loggingOptions.enableDebugLogging) {
            System.err.println("${timestamp()} Crolang DEBUG error: $msg")
        }
    }
}
