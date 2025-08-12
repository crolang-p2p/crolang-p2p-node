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

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ConnectionToBrokerErrorJs {
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

    companion object {
        fun fromErrorMessage(errorMessage: String): ConnectionToBrokerErrorJs {
            return when (errorMessage) {
                LOCAL_CLIENT_ALREADY_CONNECTED.name -> LOCAL_CLIENT_ALREADY_CONNECTED
                ALREADY_PERFORMING_CONNECTION.name -> ALREADY_PERFORMING_CONNECTION
                CLIENT_WITH_SAME_ID_ALREADY_CONNECTED.name -> CLIENT_WITH_SAME_ID_ALREADY_CONNECTED
                UNSUPPORTED_ARCHITECTURE.name -> UNSUPPORTED_ARCHITECTURE
                UNAUTHORIZED.name -> UNAUTHORIZED
                SOCKET_ERROR.name -> SOCKET_ERROR
                ERROR_PARSING_RTC_CONFIGURATION.name -> ERROR_PARSING_RTC_CONFIGURATION
                else -> UNKNOWN_ERROR
            }
        }
    }

}
