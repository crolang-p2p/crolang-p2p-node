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

import internal.events.*
import internal.events.data.*
import internal.events.data.abstractions.ParsableMsg
import internal.events.data.abstractions.SocketMsgType
import internal.utils.EventLoop
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.parser
import io.socket.client.Socket
import java.util.*

/**
 * Object responsible for registering and handling mappings between incoming socket messages
 * and corresponding internal events.
 */
internal object BrokerMessagesMapping {

    /**
     * Registers all supported socket message listeners on the given [socket] instance.
     *
     * For each supported message type, a corresponding event is posted to the [EventLoop]
     * once a valid message is parsed.
     *
     * @param socket the socket to register listeners on
     */
    fun registerMsgListeners(socket: Socket){
        registerSpecificMsgListener<ParsableSessionDescriptionMsg, SessionDescriptionMsg>(
            socket, SocketMsgType.CONNECTION_ATTEMPT
        ) { EventLoop.postEvent(OnIncomingConnectionAttemptMsg(it)) }

        registerSpecificMsgListener<ParsableSessionDescriptionMsg, SessionDescriptionMsg>(
            socket, SocketMsgType.CONNECTION_ACCEPTANCE
        ){ EventLoop.postEvent(OnIncomingP2PConnectionAcceptanceMsg(it)) }

        registerSpecificMsgListener<ParsableConnectionRefusalMsg, ConnectionRefusalMsg>(
            socket, SocketMsgType.CONNECTION_REFUSAL
        ){ EventLoop.postEvent(OnConnectionAttemptRefusedInitiatorNode(it)) }

        registerSpecificMsgListener<ParsableIncomingConnectionsNotAllowedMsg, IncomingConnectionsNotAllowedMsg>(
            socket, SocketMsgType.INCOMING_CONNECTIONS_NOT_ALLOWED
        ){ EventLoop.postEvent(OnIncomingConnectionsNotAllowedInitiatorNode(it)) }

        registerSpecificMsgListener<ParsableIceCandidateMsg, IceCandidateMsg>(
            socket, SocketMsgType.ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER
        ){ EventLoop.postEvent(OnIncomingP2PIceCandidatesExchangeMsgResponderNode(it)) }

        registerSpecificMsgListener<ParsableIceCandidateMsg, IceCandidateMsg>(
            socket, SocketMsgType.ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR
        ){ EventLoop.postEvent(OnIncomingP2PIceCandidatesExchangeMsgInitiatorNode(it)) }
    }

    /**
     * Registers a socket listener for a specific message type and binds it to a given event strategy.
     *
     * @param U the message wrapper implementing [ParsableMsg]
     * @param C the concrete internal message type
     * @param socket the socket on which to register the listener
     * @param msgType the socket message type string identifier
     * @param eventStrategy a function to execute when the message is successfully parsed
     */
    private inline fun <reified U : ParsableMsg<C>, C> registerSpecificMsgListener(
        socket: Socket,
        msgType: String,
        crossinline eventStrategy: (msg: C) -> Unit
    ){
        registerSpecificMsgListener<U, C>(socket, msgType, eventStrategy, onMsgParsingErrorStrategy = {})
    }

    /**
     * Registers a socket listener for a specific message type with optional error handling strategy.
     *
     * @param U the message wrapper implementing [ParsableMsg]
     * @param C the concrete internal message type
     * @param socket the socket on which to register the listener
     * @param msgType the socket message type string identifier
     * @param eventStrategy a function to execute when the message is successfully parsed
     * @param onMsgParsingErrorStrategy a function to execute in case of parsing error or malformed payload
     */
    inline fun <reified U : ParsableMsg<C>, C> registerSpecificMsgListener(
        socket: Socket,
        msgType: String,
        crossinline eventStrategy: (msg: C) -> Unit,
        crossinline onMsgParsingErrorStrategy: () -> Unit
    ){
        socket.on(msgType){ payload ->
            if(payload != null && payload.size == 1){
                fromJsonToCheckedMsg<U, C>(payload[0].toString()).ifPresentOrElse(
                    { eventStrategy(it) },
                    {
                        logger.debugErr("Received unparsable $msgType socket msg, discarding it")
                        onMsgParsingErrorStrategy()
                    }
                )
            } else {
                logger.debugErr("Error on incoming $msgType socket msg's payload, discarding it")
                onMsgParsingErrorStrategy()
            }
        }
    }

    /**
     * Converts a raw JSON message to a validated message object.
     *
     * @param U the message wrapper implementing [ParsableMsg]
     * @param C the concrete internal message type
     * @param msg the raw JSON string
     * @return an [Optional] containing the parsed and validated message, or empty if parsing failed
     */
    private inline fun <reified U : ParsableMsg<C>, C> fromJsonToCheckedMsg(msg: String): Optional<C & Any> {
        val fromJson = parser.fromJson(msg, U::class.java)
        return if (fromJson.isEmpty) {
            Optional.empty()
        } else {
            fromJson.get().toChecked()
        }
    }
}
