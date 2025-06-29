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

package internal.events.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import internal.events.data.abstractions.ParsableMsg

/**
 * Represents a direct message sent between nodes via the Broker's WebSocket relay.
 *
 * This message is used to exchange arbitrary data between peers over a specified channel. It contains the sender and receiver node IDs,
 * the logical channel for the message, and the message content itself. The message is relayed by the Broker using WebSocket.
 *
 * @param from The ID of the sender node.
 * @param to The ID of the receiver node.
 * @param channel The logical channel on which the message is sent.
 * @param content The message content to be delivered.
 */
internal class SocketDirectMsg(
    val from: String,
    val to: String,
    val channel: String,
    val content: String
)

/**
 * Represents a direct message in a parsable format, used for exchanging messages between nodes via the Broker's WebSocket relay.
 *
 * This class is responsible for parsing the JSON payload of a direct message and converting it into a concrete [SocketDirectMsg] object.
 * The message is relayed by the Broker using WebSocket and is used for communication between nodes over logical channels.
 *
 * The fields correspond to the sender, receiver, channel, and content of the message.
 *
 * @property from The ID of the sender node.
 * @property to The ID of the receiver node.
 * @property channel The logical channel on which the message is sent.
 * @property content The message content to be delivered.
 */
@Serializable
internal class ParsableSocketDirectMsg: ParsableMsg<SocketDirectMsg>(){

    @SerialName("from") var from: String? = null

    @SerialName("to") var to: String? = null

    @SerialName("channel") var channel: String? = null

    @SerialName("content") var content: String? = null

    override fun toChecked(): SocketDirectMsg? {
        return if(from == null || to == null || channel == null || content == null) {
            null
        } else {
            SocketDirectMsg(from!!, to!!, channel!!, content!!)
        }
    }

    companion object {
        fun fromChecked(msg: SocketDirectMsg): ParsableSocketDirectMsg {
            return ParsableSocketDirectMsg().apply {
                from = msg.from
                to = msg.to
                channel = msg.channel
                content = msg.content
            }
        }
    }

}
