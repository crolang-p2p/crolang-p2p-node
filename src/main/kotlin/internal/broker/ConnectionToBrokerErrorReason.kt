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

package internal.broker

import org.crolangP2P.InvoluntaryBrokerDisconnectionCause
import org.crolangP2P.exceptions.ConnectToBrokerException

/**
 * Enum class representing the reasons for connection errors to the Broker.
 *
 * @property UNKNOWN_ERROR An unknown error occurred.
 * @property SOCKET_ERROR A socket error occurred (Broker not reachable, probably).
 * @property CLIENT_WITH_SAME_ID_ALREADY_CONNECTED Another client with the same ID is already connected to the Broker.
 * @property UNAUTHORIZED The client is unauthorized to connect to the Broker.
 * @property ERROR_PARSING_RTC_CONFIGURATION An error occurred while parsing the RTC configuration received from the Broker.
 */
internal enum class ConnectionToBrokerErrorReason {
    UNKNOWN_ERROR,
    SOCKET_ERROR,
    CLIENT_WITH_SAME_ID_ALREADY_CONNECTED,
    UNAUTHORIZED,
    ERROR_PARSING_RTC_CONFIGURATION;

    /**
     * Converts the current instance of [ConnectionToBrokerErrorReason] to a [ConnectToBrokerException].
     *
     * @return The corresponding [ConnectToBrokerException] based on the current instance.
     */
    fun toConnectToBrokerException(): ConnectToBrokerException {
        return when (this) {
            UNKNOWN_ERROR -> ConnectToBrokerException.UnknownError
            SOCKET_ERROR -> ConnectToBrokerException.SocketError
            CLIENT_WITH_SAME_ID_ALREADY_CONNECTED -> ConnectToBrokerException.ClientWithSameIdAlreadyConnected
            UNAUTHORIZED -> ConnectToBrokerException.Unauthorized
            ERROR_PARSING_RTC_CONFIGURATION -> ConnectToBrokerException.ErrorParsingRTCConfiguration
        }
    }

    /**
     * Converts the current instance of [ConnectionToBrokerErrorReason] to an [InvoluntaryBrokerDisconnectionCause].
     *
     * @return The corresponding [InvoluntaryBrokerDisconnectionCause] based on the current instance.
     */
    fun toInvoluntaryBrokerDisconnectionCause(): InvoluntaryBrokerDisconnectionCause {
        return when (this) {
            UNAUTHORIZED -> InvoluntaryBrokerDisconnectionCause.UNAUTHORIZED
            CLIENT_WITH_SAME_ID_ALREADY_CONNECTED -> InvoluntaryBrokerDisconnectionCause.CLIENT_WITH_SAME_ID_ALREADY_CONNECTED
            SOCKET_ERROR -> InvoluntaryBrokerDisconnectionCause.CONNECTION_ERROR
            else -> InvoluntaryBrokerDisconnectionCause.UNKNOWN_ERROR
        }
    }
}
