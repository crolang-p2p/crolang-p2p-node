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

package internal.dependencies_injection.socket

import internal.broker.OnConnectionToBrokerSettings
import internal.dependencies.socket.CrolangP2PSocket
import internal.dependencies.socket.CrolangP2PSocketCreator

/**
 * JavaScript implementation of CrolangP2PSocketCreator using Socket.IO client.
 * 
 * This implementation creates socket connections to CroLang P2P brokers using
 * the Socket.IO JavaScript client library through Kotlin/JS bindings.
 */
internal class ConcreteCrolangP2PSocketCreatorJs : CrolangP2PSocketCreator() {

    override fun create(
        localNodeId: String, myVersion: String, onConnectionToBrokerSettings: OnConnectionToBrokerSettings
    ): CrolangP2PSocket {
        
        // Create Socket.IO options object
        val options = js("{}").unsafeCast<SocketIOOptions>()
        options.transports = arrayOf("websocket")
        options.autoConnect = false
        options.reconnection = false
        options.query = createQuery(localNodeId, myVersion, onConnectionToBrokerSettings)
        
        // Create socket using Socket.IO client
        val socket = SocketIOClient.io(onConnectionToBrokerSettings.brokerAddr, options)
        
        return ConcreteCrolangP2PSocketJs(socket)
    }

    /**
     * Creates a query object for the Socket.IO connection.
     * The query includes the local node ID, version, runtime platform, and optional additional data for authentication.
     *
     * @param localNodeId The local node ID.
     * @param myVersion The version of the application.
     * @param onConnectionToBrokerSettings The settings for the connection to the Broker.
     * @return A query object for the Socket.IO connection.
     */
    private fun createQuery(
        localNodeId: String,
        myVersion: String,
        onConnectionToBrokerSettings: OnConnectionToBrokerSettings
    ): dynamic {
        val query = js("{}")
        query["id"] = localNodeId
        query["version"] = myVersion
        query["runtime"] = "JavaScript"
        
        if (onConnectionToBrokerSettings.data != null) {
            query["data"] = onConnectionToBrokerSettings.data
        }
        
        return query
    }

    override fun eventConnect(): String {
        return SocketIOEvents.CONNECT
    }

    override fun eventDisconnect(): String {
        return SocketIOEvents.DISCONNECT
    }

    override fun eventConnectionError(): String {
        return SocketIOEvents.CONNECT_ERROR
    }
}
