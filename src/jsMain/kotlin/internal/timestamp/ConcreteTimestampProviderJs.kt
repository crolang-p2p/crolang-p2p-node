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

package internal.timestamp

import internal.dependencies.utils.TimestampProvider
import kotlin.js.Date

/**
 * JavaScript/Node.js implementation of TimestampProvider using JavaScript Date API.
 * Provides formatted timestamps with millisecond precision for logging purposes.
 */
internal class ConcreteTimestampProviderJs : TimestampProvider() {

    /**
     * Generates a formatted timestamp string with millisecond precision.
     * Format: [yyyy-MM-dd HH:mm:ss.SSS]
     * 
     * @return A formatted timestamp enclosed in square brackets.
     */
    override fun getCurrentTimestamp(): String {
        val now = Date()
        
        val year = now.getFullYear()
        val month = (now.getMonth() + 1).toString().padStart(2, '0')
        val day = now.getDate().toString().padStart(2, '0')
        val hour = now.getHours().toString().padStart(2, '0')
        val minute = now.getMinutes().toString().padStart(2, '0')
        val second = now.getSeconds().toString().padStart(2, '0')
        val millisecond = now.getMilliseconds().toString().padStart(3, '0')
        
        return "[$year-$month-$day $hour:$minute:$second.$millisecond]"
    }
}
