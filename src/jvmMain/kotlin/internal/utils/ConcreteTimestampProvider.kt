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

import internal.dependencies.utils.TimestampProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * JVM implementation of TimestampProvider using java.time APIs.
 * Provides formatted timestamps with millisecond precision for logging purposes.
 */
internal class ConcreteTimestampProvider : TimestampProvider() {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    /**
     * Generates a formatted timestamp string with millisecond precision.
     * Format: [yyyy-MM-dd HH:mm:ss.SSS]
     * 
     * @return A formatted timestamp enclosed in square brackets.
     */
    override fun getCurrentTimestamp(): String {
        return "[${LocalDateTime.now().format(formatter)}]"
    }
}
