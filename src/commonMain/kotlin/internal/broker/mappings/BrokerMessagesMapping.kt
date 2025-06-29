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

import internal.dependencies.event_loop.EventLoop
import internal.dependencies.socket.CrolangP2PSocket
import internal.events.OnConnectionAttemptRefusedInitiatorNode
import internal.events.OnIncomingConnectionAttemptMsg
import internal.events.OnIncomingConnectionsNotAllowedInitiatorNode
import internal.events.OnIncomingP2PConnectionAcceptanceMsg
import internal.events.OnIncomingP2PIceCandidatesExchangeMsgInitiatorNode
import internal.events.OnIncomingP2PIceCandidatesExchangeMsgResponderNode
import internal.events.OnSocketDirectMsgReceived
import internal.events.data.ConnectionRefusalMsg
import internal.events.data.IceCandidateMsg
import internal.events.data.IncomingConnectionsNotAllowedMsg
import internal.events.data.ParsableConnectionRefusalMsg
import internal.events.data.ParsableIceCandidateMsg
import internal.events.data.ParsableIncomingConnectionsNotAllowedMsg
import internal.events.data.ParsableSessionDescriptionMsg
import internal.events.data.ParsableSocketDirectMsg
import internal.events.data.SessionDescriptionMsg
import internal.events.data.SocketDirectMsg
import internal.events.data.abstractions.ParsableMsg
import internal.events.data.abstractions.SocketMsgType
import internal.utils.SharedStore
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.parser

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
    fun registerMsgListeners(socket: CrolangP2PSocket){
        registerSpecificMsgListener<ParsableSessionDescriptionMsg, SessionDescriptionMsg>(
            socket, SocketMsgType.CONNECTION_ATTEMPT
        ) { SharedStore.dependencies!!.eventLoop.postEvent(OnIncomingConnectionAttemptMsg(it)) }

        registerSpecificMsgListener<ParsableSessionDescriptionMsg, SessionDescriptionMsg>(
            socket, SocketMsgType.CONNECTION_ACCEPTANCE
        ){ SharedStore.dependencies!!.eventLoop.postEvent(OnIncomingP2PConnectionAcceptanceMsg(it)) }

        registerSpecificMsgListener<ParsableConnectionRefusalMsg, ConnectionRefusalMsg>(
            socket, SocketMsgType.CONNECTION_REFUSAL
        ){ SharedStore.dependencies!!.eventLoop.postEvent(OnConnectionAttemptRefusedInitiatorNode(it)) }

        registerSpecificMsgListener<ParsableIncomingConnectionsNotAllowedMsg, IncomingConnectionsNotAllowedMsg>(
            socket, SocketMsgType.INCOMING_CONNECTIONS_NOT_ALLOWED
        ){ SharedStore.dependencies!!.eventLoop.postEvent(OnIncomingConnectionsNotAllowedInitiatorNode(it)) }

        registerSpecificMsgListener<ParsableIceCandidateMsg, IceCandidateMsg>(
            socket, SocketMsgType.ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER
        ){ SharedStore.dependencies!!.eventLoop.postEvent(OnIncomingP2PIceCandidatesExchangeMsgResponderNode(it)) }

        registerSpecificMsgListener<ParsableIceCandidateMsg, IceCandidateMsg>(
            socket, SocketMsgType.ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR
        ){ SharedStore.dependencies!!.eventLoop.postEvent(OnIncomingP2PIceCandidatesExchangeMsgInitiatorNode(it)) }

        registerSpecificMsgListener<ParsableSocketDirectMsg, SocketDirectMsg>(
            socket, SocketMsgType.SOCKET_MSG_EXCHANGE
        ){ SharedStore.dependencies!!.eventLoop.postEvent(OnSocketDirectMsgReceived(it)) }
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
        socket: CrolangP2PSocket,
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
        socket: CrolangP2PSocket,
        msgType: String,
        crossinline eventStrategy: (msg: C) -> Unit,
        crossinline onMsgParsingErrorStrategy: () -> Unit
    ){
        socket.on(msgType){ payload ->
            if(payload.size == 1){
                fromJsonToCheckedMsg<U, C>(payload[0].toString())?.let {
                    eventStrategy(it)
                } ?: run {
                    logger.debugErr("Received unparsable $msgType socket msg, discarding it")
                    onMsgParsingErrorStrategy()
                }
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
     * @return a nullable containing the parsed and validated message, or null if parsing failed
     */
    private inline fun <reified U : ParsableMsg<C>, C> fromJsonToCheckedMsg(msg: String): C? {
        return parser.fromJson<U>(msg)?.toChecked()
    }
}
