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

import internal.BuildConfig
import internal.broker.mappings.BrokerEventsMapping
import internal.broker.mappings.BrokerMessagesMapping
import internal.utils.SharedStore
import internal.utils.SharedStore.localNodeId
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URI

/**
 * This object is responsible for creating a Socket.IO instance for the Broker connection.
 * It sets up the socket with the appropriate URI, options, and event listeners.
 */
internal object BrokerSocketCreator {

    /**
     * Creates a Socket.IO instance with the given URI and options.
     * The socket is configured to use WebSocket transport and has a query string with the local node ID.
     * The socket is registered with listeners for events (connected, authenticated, disconnected, etc...) and messages.
     *
     * @return A Socket.IO instance.
     */
    fun createSocketIO(): Socket {
        val onConnectionSettings = SharedStore.onConnectionToBrokerSettings.get()
        val socket: Socket = IO.socket(
            URI.create(onConnectionSettings.brokerAddr),
            IO.Options.builder()
                .setReconnection(false)
                .setTransports(arrayOf(WebSocket.NAME))
                .setQuery(createQuery(onConnectionSettings))
                .build()
        )
        BrokerEventsMapping.registerEventListeners(socket)
        BrokerMessagesMapping.registerMsgListeners(socket)
        return socket
    }

    /**
     * Creates a query string for the Socket.IO connection.
     * The query string includes the local node ID and optional additional data for authentication.
     *
     * @param onConnectionToBrokerSettings The settings for the connection to the Broker.
     * @return A query string for the Socket.IO connection.
     */
    private fun createQuery(onConnectionToBrokerSettings: OnConnectionToBrokerSettings): String {
        var query = "id=$localNodeId&version=${BuildConfig.VERSION}&runtime=JVM"
        if(onConnectionToBrokerSettings.data != null){
            query += "&data=${onConnectionToBrokerSettings.data}"
        }
        return query
    }

}
