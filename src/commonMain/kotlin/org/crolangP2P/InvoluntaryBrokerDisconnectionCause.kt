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

package org.crolangP2P

/**
 * Enum class representing the causes of involuntary disconnection from the Broker.
 *
 * @property MAX_RECONNECTION_ATTEMPTS_EXCEEDED Indicates that the maximum number of reconnection attempts has been exceeded.
 * @property UNAUTHORIZED Indicates that the disconnection was due to unauthorized access.
 * @property CLIENT_WITH_SAME_ID_ALREADY_CONNECTED Indicates that a client with the same ID is already connected to the Broker.
 * @property CONNECTION_ERROR Indicates that there was a connection error.
 * @property UNKNOWN_ERROR Indicates an unknown error occurred.
 */
enum class InvoluntaryBrokerDisconnectionCause {
    /**
     * Indicates that the maximum number of reconnection attempts has been exceeded.
     */
    MAX_RECONNECTION_ATTEMPTS_EXCEEDED,
    /**
     * Indicates that the disconnection was due to unauthorized access.
     */
    UNAUTHORIZED,
    /**
     * Indicates that a client with the same ID is already connected to the Broker.
     */
    CLIENT_WITH_SAME_ID_ALREADY_CONNECTED,
    /**
     * Indicates that there was a connection error.
     */
    CONNECTION_ERROR,
    /**
     * Indicates an unknown error occurred.
     */
    UNKNOWN_ERROR;
}
