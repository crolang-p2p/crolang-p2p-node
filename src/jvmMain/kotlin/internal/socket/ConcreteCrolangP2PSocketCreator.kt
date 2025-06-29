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

package internal.socket

import internal.broker.OnConnectionToBrokerSettings
import internal.dependencies.socket.CrolangP2PSocket
import internal.dependencies.socket.CrolangP2PSocketCreator
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URI


internal class ConcreteCrolangP2PSocketCreator : CrolangP2PSocketCreator() {

    override fun create(
        localNodeId: String, myVersion: String, onConnectionToBrokerSettings: OnConnectionToBrokerSettings
    ): CrolangP2PSocket {
        return ConcreteCrolangP2PSocket(IO.socket(
            URI.create(onConnectionToBrokerSettings.brokerAddr),
            IO.Options.builder()
                .setReconnection(false)
                .setTransports(arrayOf(WebSocket.NAME))
                .setQuery(createQuery(localNodeId, myVersion, onConnectionToBrokerSettings))
                .build()
        ))
    }

    /**
     * Creates a query string for the Socket.IO connection.
     * The query string includes the local node ID and optional additional data for authentication.
     *
     * @param localNodeId The local node ID.
     * @param myVersion The version of the application.
     * @param onConnectionToBrokerSettings The settings for the connection to the Broker.
     * @return A query string for the Socket.IO connection.
     */
    private fun createQuery(
        localNodeId: String,
        myVersion: String,
        onConnectionToBrokerSettings: OnConnectionToBrokerSettings
    ): String {
        var query = "id=$localNodeId&version=$myVersion&runtime=JVM"
        if(onConnectionToBrokerSettings.data != null){
            query += "&data=${onConnectionToBrokerSettings.data}"
        }
        return query
    }

    override fun eventConnect(): String {
        return Socket.EVENT_CONNECT
    }

    override fun eventDisconnect(): String {
        return Socket.EVENT_DISCONNECT
    }

    override fun eventConnectionError(): String {
        return Socket.EVENT_CONNECT_ERROR
    }

}
