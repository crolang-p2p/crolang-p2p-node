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

package internal.dependencies.socket

import internal.broker.OnConnectionToBrokerSettings

/**
 * Abstract factory for creating platform-specific socket implementations.
 * 
 * This factory provides a common interface for creating sockets while allowing
 * each platform to provide its own specific implementation details.
 */
abstract class CrolangP2PSocketCreator {

    /**
     * Creates a new socket instance configured for the CroLang P2P protocol.
     *
     * @param localNodeId Unique identifier for the local node
     * @param myVersion Version of the CroLang P2P library
     * @param onConnectionToBrokerSettings Configuration settings for broker connection
     * @return A configured socket instance
     */
    abstract fun create(
        localNodeId: String, myVersion: String, onConnectionToBrokerSettings: OnConnectionToBrokerSettings
    ): CrolangP2PSocket

    /**
     * Returns the event name used to indicate successful connection.
     *
     * @return The connection event name
     */
    abstract fun eventConnect(): String

    /**
     * Returns the event name used to indicate disconnection.
     *
     * @return The disconnection event name
     */
    abstract fun eventDisconnect(): String

    /**
     * Returns the event name used to indicate connection errors.
     *
     * @return The connection error event name
     */
    abstract fun eventConnectionError(): String
    
}
