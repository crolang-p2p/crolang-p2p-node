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

import internal.broker.ConnectionToBrokerErrorReason.CLIENT_WITH_SAME_ID_ALREADY_CONNECTED
import internal.broker.ConnectionToBrokerErrorReason.ERROR_PARSING_RTC_CONFIGURATION
import internal.broker.ConnectionToBrokerErrorReason.SOCKET_ERROR
import internal.broker.ConnectionToBrokerErrorReason.UNAUTHORIZED
import internal.broker.ConnectionToBrokerErrorReason.UNKNOWN_ERROR

/**
 * Exception class representing various errors that can occur when trying to connect to the Broker.
 *
 * @param message The error message associated with the exception.
 *
 * @see LocalClientAlreadyConnectedToBroker
 * @see UnknownError
 * @see SocketError
 * @see ClientWithSameIdAlreadyConnected
 * @see Unauthorized
 * @see ErrorParsingRTCConfiguration
 * @see UnsupportedArchitecture
 */
sealed class ConnectToBrokerException(message: String) : Exception(message) {

    /**
     * Exception thrown when the local client is already connected to the Broker.
     */
    data object LocalClientAlreadyConnectedToBroker : ConnectToBrokerException("LOCAL_CLIENT_ALREADY_CONNECTED_TO_BROKER") {
        private fun readResolve(): Any = LocalClientAlreadyConnectedToBroker
    }

    /**
     * Exception thrown when an unknown error occurs while trying to connect to the Broker.
     */
    data object UnknownError : ConnectToBrokerException(UNKNOWN_ERROR.name) {
        private fun readResolve(): Any = UnknownError
    }

    /**
     * Exception thrown when a socket error occurs while trying to connect to the Broker (network error or Broker server is down).
     */
    data object SocketError : ConnectToBrokerException(SOCKET_ERROR.name) {
        private fun readResolve(): Any = SocketError
    }

    /**
     * Exception thrown when a client with the same ID is already connected to the Broker.
     */
    data object ClientWithSameIdAlreadyConnected : ConnectToBrokerException(CLIENT_WITH_SAME_ID_ALREADY_CONNECTED.name) {
        private fun readResolve(): Any = ClientWithSameIdAlreadyConnected
    }

    /**
     * Exception thrown when the client is unauthorized to connect to the Broker.
     */
    data object Unauthorized : ConnectToBrokerException(UNAUTHORIZED.name) {
        private fun readResolve(): Any = Unauthorized
    }

    /**
     * Exception thrown when there is an error parsing the RTC configuration received from the Broker.
     */
    data object ErrorParsingRTCConfiguration : ConnectToBrokerException(ERROR_PARSING_RTC_CONFIGURATION.name) {
        private fun readResolve(): Any = ErrorParsingRTCConfiguration
    }

    /**
     * Exception thrown when the architecture of the client is not supported by this library.
     */
    data object UnsupportedArchitecture : ConnectToBrokerException("UNSUPPORTED_ARCHITECTURE") {
        private fun readResolve(): Any = UnsupportedArchitecture
    }

}
