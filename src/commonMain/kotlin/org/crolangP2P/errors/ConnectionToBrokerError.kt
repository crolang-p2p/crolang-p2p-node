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

package org.crolangP2P.errors

import org.crolangP2P.InvoluntaryBrokerDisconnectionCause
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Enum class representing the reasons for connection errors to the Broker.
 *
 * @property UNKNOWN_ERROR An unknown error occurred.
 * @property LOCAL_CLIENT_ALREADY_CONNECTED The local client is already connected to the Broker.
 * @property SOCKET_ERROR A socket error occurred (Broker not reachable, probably).
 * @property CLIENT_WITH_SAME_ID_ALREADY_CONNECTED Another client with the same ID is already connected to the Broker.
 * @property UNAUTHORIZED The client is unauthorized to connect to the Broker.
 * @property ERROR_PARSING_RTC_CONFIGURATION An error occurred while parsing the RTC configuration received from the Broker.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ConnectionToBrokerError {
    /**
     * An unknown error occurred.
     */
    UNKNOWN_ERROR,

    /**
     * The local client is already connected to the Broker.
     */
    LOCAL_CLIENT_ALREADY_CONNECTED,

    /**
     * The local client is already performing a connection to the Broker.
     * This is used to prevent multiple connection attempts at the same time.
     */
    ALREADY_PERFORMING_CONNECTION,

    /**
     * A socket error occurred (e.g., the Broker is not reachable).
     */
    SOCKET_ERROR,
    /**
     * Another client with the same ID is already connected to the Broker.
     */
    CLIENT_WITH_SAME_ID_ALREADY_CONNECTED,
    /**
     * The client is unauthorized to connect to the Broker.
     */
    UNAUTHORIZED,
    /**
     * An error occurred while parsing the RTC configuration received from the Broker.
     */
    ERROR_PARSING_RTC_CONFIGURATION,

    /**
     * The client version is not supported by the Broker.
     * This is used to indicate that the client and broker versions are incompatible.
     */
    UNSUPPORTED_ARCHITECTURE;

    /**
     * Converts the current instance of [ConnectionToBrokerError] to an [InvoluntaryBrokerDisconnectionCause].
     *
     * @return The corresponding [InvoluntaryBrokerDisconnectionCause] based on the current instance.
     */
    internal fun toInvoluntaryBrokerDisconnectionCause(): InvoluntaryBrokerDisconnectionCause {
        return when (this) {
            UNAUTHORIZED -> InvoluntaryBrokerDisconnectionCause.UNAUTHORIZED
            CLIENT_WITH_SAME_ID_ALREADY_CONNECTED -> InvoluntaryBrokerDisconnectionCause.CLIENT_WITH_SAME_ID_ALREADY_CONNECTED
            UNSUPPORTED_ARCHITECTURE -> InvoluntaryBrokerDisconnectionCause.UNSUPPORTED_ARCHITECTURE
            SOCKET_ERROR -> InvoluntaryBrokerDisconnectionCause.CONNECTION_ERROR
            else -> InvoluntaryBrokerDisconnectionCause.UNKNOWN_ERROR
        }
    }
}
