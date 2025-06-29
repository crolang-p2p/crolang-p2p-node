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

package internal.events

import internal.events.data.SocketDirectMsg
import internal.dependencies.event_loop.Event
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.onNewSocketMsgCallbacks

/*
 * This file contains the events related to the reception and handling of direct messages sent between nodes via the Broker's WebSocket relay (SOCKET_MSG_EXCHANGE).
 *
 * This mechanism enables flexible and modular handling of application-level messages between nodes, even if they are not directly connected.
 */

/**
 * Event triggered when a direct message is received from another node via the Broker's WebSocket relay (SOCKET_MSG_EXCHANGE).
 *
 * This event retrieves the appropriate callback for the logical channel from [SharedStore.onNewSocketMsgCallbacks] and invokes it,
 * passing the sender node ID and the message content. If no callback is registered for the channel, the message is ignored.
 *
 * This mechanism allows application-level message handling between nodes, even if they are not directly connected.
 *
 * @property msg The received [SocketDirectMsg] containing sender, channel, and content information.
 */
internal class OnSocketDirectMsgReceived(val msg: SocketDirectMsg) : Event {

    override fun process() {
        logger.debugInfo("Received socket msg from ${msg.from} on channel ${msg.channel}: ${msg.content}")
        val callback: ((from: String, msg: String) -> Unit)? = onNewSocketMsgCallbacks[msg.channel]
        if(callback != null) {
            executeCallbackOnExecutor {
                callback(msg.from, msg.content)
            }
        }

    }

}
