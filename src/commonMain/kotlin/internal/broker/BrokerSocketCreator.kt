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

import internal.broker.mappings.BrokerEventsMapping
import internal.broker.mappings.BrokerMessagesMapping
import internal.dependencies.socket.CrolangP2PSocket
import internal.utils.SharedStore

/**
 * This object is responsible for creating a Socket instance for the Broker connection.
 * It sets up the socket with the appropriate URI, options, and event listeners.
 */
internal object BrokerSocketCreator {

    /**
     * Creates a Socket instance with the given URI and options.
     * The socket is configured to use WebSocket transport and has a query string with the local node ID.
     * The socket is registered with listeners for events (connected, authenticated, disconnected, etc...) and messages.
     *
     * @return A Socket instance.
     */
    fun createSocket(): CrolangP2PSocket {
        val socket = SharedStore.dependencies!!.socketCreator.create(
            SharedStore.localNodeId, SharedStore.dependencies!!.myVersion, SharedStore.onConnectionToBrokerSettings!!
        )
        BrokerEventsMapping.registerEventListeners(socket)
        BrokerMessagesMapping.registerMsgListeners(socket)
        return socket
    }

}
