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

package internal.broker.mappings

import internal.events.OnAuthenticationMsgParsingError
import internal.events.OnBrokerConnectError
import internal.events.OnBrokerDisconnection
import internal.events.OnValidAuthenticationMsg
import internal.events.data.ParsableRTCConfigurationMsg
import internal.events.data.RTCConfigurationMsg
import internal.events.data.abstractions.SocketMsgType.Companion.AUTHENTICATED
import internal.utils.EventLoop
import internal.utils.SharedStore.reconnectionAttempts
import io.socket.client.Socket

/**
 * This object is responsible for registering event listeners for the Broker socket.
 */
internal object BrokerEventsMapping {

    /**
     * Registers event listeners for the Broker socket.
     *
     * @param socket The socket to register the event listeners on.
     */
    fun registerEventListeners(socket: Socket){

        socket.on(Socket.EVENT_CONNECT) {
            // AUTHENTICATED is the important one
            reconnectionAttempts = 0
        }

        BrokerMessagesMapping.registerSpecificMsgListener<ParsableRTCConfigurationMsg, RTCConfigurationMsg>(
            socket,
            AUTHENTICATED,
            { msg -> EventLoop.postEvent(OnValidAuthenticationMsg(msg)) },
            { EventLoop.postEvent(OnAuthenticationMsgParsingError()) }
        )

        socket.on(Socket.EVENT_CONNECT_ERROR) {
            EventLoop.postEvent(OnBrokerConnectError(it))
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            EventLoop.postEvent(OnBrokerDisconnection())
        }

    }

}
