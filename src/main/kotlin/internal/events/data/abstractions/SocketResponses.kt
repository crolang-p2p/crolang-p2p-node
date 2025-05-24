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

package internal.events.data.abstractions

/**
 * Contains the possible response codes used in socket message exchanges between nodes and the Broker.
 *
 * These responses are used to indicate the outcome of operations such as relaying direct messages or checking node status.
 *
 * - [OK]: The operation was successful.
 * - [ERROR]: A generic error occurred during the operation.
 * - [UNAUTHORIZED]: The sender is not authorized to perform the requested operation (e.g., sending a message to a node).
 * - [NOT_CONNECTED]: The target node is not connected to the Broker.
 *
 * The [ALL] set contains all possible response codes, and [isOk] is a utility to check for a successful response.
 */
internal object SocketResponses {
    const val OK = "OK"
    const val ERROR = "ERROR"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val NOT_CONNECTED = "NOT_CONNECTED"

    val ALL: Set<String> = setOf(OK, ERROR, UNAUTHORIZED, NOT_CONNECTED)

    fun isOk(response: String): Boolean = response == OK
}
