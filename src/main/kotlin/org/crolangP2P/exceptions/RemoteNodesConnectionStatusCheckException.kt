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

package org.crolangP2P.exceptions

/**
 * Sealed class representing exceptions related to remote nodes connection status checks.
 *
 * @param message The error message associated with the exception.
 *
 * @see NotConnectedToBroker
 * @see UnknownError
 */
sealed class RemoteNodesConnectionStatusCheckException(message: String) : Exception(message) {

    /**
     * Exception thrown when the local Node is not connected to the Broker and therefore cannot check the connection status of remote Nodes.
     */
    data object NotConnectedToBroker : RemoteNodesConnectionStatusCheckException("NOT_CONNECTED_TO_BROKER") {
        private fun readResolve(): Any = NotConnectedToBroker
    }

    /**
     * Exception thrown when an unknown error occurs while checking the connection status of remote Nodes.
     */
    data object UnknownError : RemoteNodesConnectionStatusCheckException("UNKNOWN_ERROR") {
        private fun readResolve(): Any = UnknownError
    }

}
