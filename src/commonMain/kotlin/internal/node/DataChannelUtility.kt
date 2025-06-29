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

package internal.node

import internal.events.data.PeerMsgPart
import internal.events.data.PeerMsgPartParsable
import internal.dependencies.event_loop.Event
import internal.utils.SharedStore
import internal.utils.SharedStore.cborParser
import internal.utils.SharedStore.logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException

/**
 * Utility object for handling data channel messages in a peer-to-peer connection.
 */
internal object DataChannelUtility {

    /**
     * Handles the byteArray of a P2P message received through a data channel.
     * The message is decoded and parsed into a [PeerMsgPartParsable] object, which is then converted into a [PeerMsgPart].
     * If the conversion is successful, an event is posted with the [PeerMsgPart] as a parameter.
     * If the conversion fails, an error message is logged.
     *
     * @param remoteNodeId The identifier of the remote node that sent the message.
     * @param byteArray The buffer containing the message.
     * @param incomingP2PMessagePartEventStrategy A strategy for creating an event with the [PeerMsgPart] as a parameter.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun onP2PMessageByteArray(
        remoteNodeId: String,
        byteArray: ByteArray,
        incomingP2PMessagePartEventStrategy: (String, PeerMsgPart) -> Event
    ){
        try {
            val msgPart = cborParser.decodeFromByteArray(PeerMsgPartParsable.serializer(), byteArray)
            msgPart.toChecked()?.let {
                SharedStore.dependencies!!.eventLoop.postEvent(incomingP2PMessagePartEventStrategy(remoteNodeId, it))
            } ?: run {
                logger.regularErr("received unparsable P2P msg from $remoteNodeId, discarding it")
            }
        } catch (e: SerializationException) {
            logger.regularErr("received unparsable P2P msg from $remoteNodeId, discarding it")
        } catch (e: IllegalArgumentException) {
            logger.regularErr("received unparsable P2P msg from $remoteNodeId, discarding it")
        }
    }

}
