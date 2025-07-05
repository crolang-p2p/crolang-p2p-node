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

import internal.dependencies_injection.DependenciesInjectionProviderJs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.crolangP2P.CoreCrolangP2PFacade

/**
 * CrolangP2P is a class that manages the connection to the Crolang Broker and allows for connecting to
 * remote CrolangNodes.
 *
 * This is the JavaScript/Node.js interface that provides direct access to all functionalities.
 */
@JsExport
class CrolangP2P {

    private val coreFacade = CoreCrolangP2PFacade(DependenciesInjectionProviderJs.getDependencies())

    /**
     * Checks if the local node is connected to the Crolang Broker.
     * This method returns true if the socket is present and connected, false otherwise.
     *
     * @return true if the local node is connected to the Broker, false otherwise.
     */
    fun isLocalNodeConnectedToBroker(): Boolean {
        return coreFacade.isLocalNodeConnectedToBroker()
    }

    /**
     * Connects to the Crolang Broker asynchronously.
     * This method returns a Promise that resolves when the connection attempt is complete.
     *
     * @param brokerAddress the address of the broker (e.g., "ws://localhost:8080")
     * @param nodeId the unique identifier for this node
     * @param authData optional authentication data
     * @return Promise that resolves to true if connection was successful, false otherwise
     */
    fun connectToBroker(
        brokerAddress: String,
        nodeId: String
    ): kotlin.js.Promise<Boolean> {
        return GlobalScope.promise {
            try {
                val result = coreFacade.connectToBroker(brokerAddress, nodeId)
                println("Risultato della connessione: $result")
                result.isSuccess
            } catch (e: Exception) {
                println("L'errore: $e")
                false
            }
        }
    }
}
