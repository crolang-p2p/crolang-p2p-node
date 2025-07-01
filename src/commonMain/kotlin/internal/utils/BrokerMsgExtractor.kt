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

internal object BrokerMsgExtractor {

    /**
     * Extracts the message string from the socket payload, handling JVM (Array), JS (String), and JS Object formats.
     *
     * @param payload the payload received from the socket
     * @return the extracted message string, or null if extraction failed
     */
    fun extractMessageFromPayload(payload: Any): String? {
        return when (payload) {
            is String -> payload
            is Array<*> -> {
                if (payload.size == 1) {
                    payload[0].toString()
                } else {
                    null
                }
            }
            else -> null
        }
    }

}
