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

import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelBuffer
import dev.onvoid.webrtc.RTCDataChannelObserver
import dev.onvoid.webrtc.RTCDataChannelState
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCPeerConnectionState
import internal.events.data.IceCandidateMsg
import internal.events.data.IncomingMultipartP2PMsg
import internal.events.data.ParsableIceCandidateMsg
import internal.events.data.PeerMsg
import internal.events.data.PeerMsgPart
import internal.events.data.PeerMsgPartParsable
import internal.events.data.abstractions.SocketResponses
import internal.events.data.adapters.IceCandidateAdapter
import internal.utils.EventLoop
import internal.utils.SharedStore.cborParser
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.parser
import internal.utils.SharedStore.settings
import internal.utils.SharedStore.socketIO
import internal.utils.TimeoutTimer
import io.socket.client.Ack
import kotlinx.serialization.ExperimentalSerializationApi
import org.crolangP2P.BasicCrolangNodeCallbacks
import org.crolangP2P.CrolangNode
import java.nio.ByteBuffer
import java.util.*

private const val DEFAULT_PAYLOAD_SIZE_BYTES: Int = 20000
private const val MAX_BUFFERED_AMOUNT = 256 * 1024 // 256 KB

/**
 * Represents an abstract node in a peer-to-peer (P2P) connection.
 * It acts as a container for data, while also offering methods to manage the WebRTC peer connection,
 * send and receiving of messages, track connection states and handle message splitting and reassembly
 * for multipart P2P messages.
 *
 * This abstract file is implemented by the [InitiatorNode] and [ResponderNode] classes.
 *
 * @property crolangNode The Crolang node (seen by the library user) associated with this instance.
 * @property remoteNodeId The ID of the remote node.
 * @property sessionId The session ID of the connection.
 * @property newDataChannelRemotelyCreatedObserver The observer for new data channels created remotely, used to determine the behaviour of the concrete [InitiatorNode] or [ResponderNode]
 * @property state The current state of the node (e.g., CREATED, CONNECTED).
 * @property peer The WebRTC peer connection object.
 * @property dataChannel The optional data channel for communication.
 * @property connectionTimeoutTimer The timer for connection timeout events.
 * @property nextP2PMsgSentId The next message ID to be sent.
 * @property incomingMultipartP2PMsgs A map tracking incoming multipart messages.
 * @property suspendedOutgoingTeamDetailsMsgs A list of suspended outgoing team details messages.
 * @property suspendedIncomingTeamDetailsMsgs A list of suspended incoming team details messages.
 *
 * @param userDefinedCallbacks The user-defined callbacks called in various moments of the node lifecycle.
 * @param concreteNodeEventParameters The concrete node event parameters, used to determine the behaviour of the concrete [InitiatorNode] or [ResponderNode].
 */
internal abstract class AbstractNode(
    rtcConfiguration: RTCConfiguration,
    val remoteNodeId: String,
    val sessionId: String,
    private val userDefinedCallbacks: BasicCrolangNodeCallbacks,
    val newDataChannelRemotelyCreatedObserver: (channel: RTCDataChannel) -> RTCDataChannelObserver,
    private val concreteNodeEventParameters: ConcreteNodeEventParameters
) {

    val crolangNode: CrolangNode = CrolangNode.create(this)
    var state: NodeState = NodeState.CREATED
    val peer: RTCPeerConnection = createPeer(rtcConfiguration)
    var dataChannel: Optional<RTCDataChannel> = Optional.empty()
    val connectionTimeoutTimer = TimeoutTimer(settings.p2pConnectionTimeoutMillis){
        EventLoop.postEvent(concreteNodeEventParameters.onConnectionAttemptTimeout())
    }
    private var nextP2PMsgSentId = 0
    private val incomingMultipartP2PMsgs: MutableMap<Int, IncomingMultipartP2PMsg> = mutableMapOf()
    val suspendedOutgoingTeamDetailsMsgs: MutableList<ParsableIceCandidateMsg> = mutableListOf()
    val suspendedIncomingTeamDetailsMsgs: MutableList<IceCandidateMsg> = mutableListOf()

    /**
     * Forces the closure of the connection with the remote node.
     *
     * Closes the data channel and the peer connection, removes the remote node from the nodes' container, and calls
     * the appropriate event handler, depending on the current state of the node.
     *
     * @param newState The new state of the node.
     */
    fun forceClose(newState: NodeState): Boolean {
        if(!isClosed(state)){
            logger.debugInfo("force closing connection with remote id $remoteNodeId. New state: $newState")
            val oldState = state
            state = newState
            dataChannel.ifPresent {
                if(it.state == RTCDataChannelState.CONNECTING || it.state == RTCDataChannelState.OPEN){
                    it.close()
                }
            }
            if(peer.connectionState == RTCPeerConnectionState.CONNECTED || peer.connectionState == RTCPeerConnectionState.CONNECTING){
                peer.close()
            }
            concreteNodeEventParameters.nodesContainer.remove(remoteNodeId)
            if(isNegotiating(oldState)){
                concreteNodeEventParameters.onNegotiationClosure()
            } else {
                concreteNodeEventParameters.onConnectedClosure()
            }
            return true
        }
        return false
    }

    /**
     * Checks if the node is in the negotiating state.
     *
     * @param state The current state of the node.
     * @return True if the node is in the negotiating state, false otherwise.
     */
    fun isNegotiating(state: NodeState): Boolean {
        return state == NodeState.CREATED || state == NodeState.DESCRIPTIONS_EXCHANGE || state == NodeState.ICE_CANDIDATES_EXCHANGE
    }

    /**
     * Checks if the node is closed.
     *
     * @param state The current state of the node.
     * @return True if the node is closed, false otherwise.
     */
    private fun isClosed(state: NodeState): Boolean {
        return !isNegotiating(state) && state != NodeState.CONNECTED
    }

    /**
     * Sends a socket message to the Broker that will redirect it to the remote Node.
     *
     * @param type The type of the message.
     * @param msg The message to be sent.
     * @param onBrokerResponse The callback to be called when the broker responds.
     */
    fun sendSocketMsg(type: String, msg: Any, onBrokerResponse: (response: String) -> Any){
        if(!isNegotiating(state)){
            logger.debugErr("Tried to send a socket msg to remote node $remoteNodeId while the CrolangNode is not negotiating")
        } else if(socketIO.isEmpty){
            logger.debugErr("Tried to send a socket msg to remote node $remoteNodeId while the socket is not initialized")
        } else if(!socketIO.get().connected()){
            logger.debugErr("Tried to send a socket msg to remote node $remoteNodeId while the socket is not connected")
        } else {
            logger.debugInfo("sending $type to remote node $remoteNodeId")
            socketIO.get().emit(type, parser.toJson(msg), Ack { args ->
                val response = if(args.size != 1 || args[0] !is String) {
                    SocketResponses.ERROR
                } else {
                    val resp = args[0] as String
                    if (SocketResponses.ALL.contains(resp)) {
                        resp
                    } else {
                        SocketResponses.ERROR
                    }
                }
                onBrokerResponse(response)
            })
        }
    }

    /**
     * Sends an ICE candidate exchange message to the remote node through the Broker.
     *
     * @param type The type of the message.
     * @param msg The message to be sent.
     */
    fun sendIceCandidatesExchangeMsg(type: String, msg: ParsableIceCandidateMsg){
        sendSocketMsg(type, msg){ response ->
            if(!SocketResponses.isOk(response)){
                EventLoop.postEvent(
                    concreteNodeEventParameters.onP2PIceCandidatesExchangeMsgBrokerNegativeResponseReceived()
                )
            }
        }
    }

    /**
     * Creates a new WebRTC peer connection.
     * Associates the peer connection with the appropriate event handlers that are defined in the
     * [InitiatorNode] or in the [ResponderNode].
     *
     * @param rtcConfiguration The configuration of the WebRTC peer connection.
     * @return The new WebRTC peer connection.
     */
    private fun createPeer(rtcConfiguration: RTCConfiguration): RTCPeerConnection {
        return PeerConnectionFactory().createPeerConnection(
            rtcConfiguration,
            object : PeerConnectionObserver {

                override fun onIceCandidate(candidate: RTCIceCandidate?) {
                    candidate?.let {
                        val msg = ParsableIceCandidateMsg()
                        msg.to = remoteNodeId
                        msg.from = localNodeId
                        msg.sessionId = sessionId
                        msg.candidate = IceCandidateAdapter.adaptConcrete(it)
                        EventLoop.postEvent(concreteNodeEventParameters.onIceCandidateReadyToBeSent(msg))
                    }
                }

                override fun onConnectionChange(state: RTCPeerConnectionState?) {
                    state?.let { EventLoop.postEvent(concreteNodeEventParameters.onConnectionStateChange(state)) }
                }

                override fun onDataChannel(channel: RTCDataChannel?) {
                    channel?.registerObserver(newDataChannelRemotelyCreatedObserver(channel))
                }

            }
        )
    }

    /**
     * Handles the reception of a new P2P message part.
     * If the message is a single-part message, it is handled immediately.
     * If the message is a multipart message, it is stored in the [incomingMultipartP2PMsgs] map until all parts are received.
     */
    fun depositNewMsgPart(msgPart: PeerMsgPart){
        if(msgPart.total <= 0){
            logger.regularErr("received P2P message part with invalid total from node $remoteNodeId")
        } else if(msgPart.total == 1){
            logger.debugInfo("received single-part P2P message from node $remoteNodeId (msg id: ${msgPart.msgId})")
            handleCompleteMsg(PeerMsg(msgPart.msgId, msgPart.channel, msgPart.payload))
        } else {
            logger.debugInfo("received part ${msgPart.part}/${msgPart.total} of P2P multipart message from node $remoteNodeId (msg id: ${msgPart.msgId})")
            handleMultipartMsg(msgPart)
        }
    }

    /**
     * Handles the reception of a multipart P2P message.
     * If it is the first part of the message, a new [IncomingMultipartP2PMsg] is created; there, the subsequent parts are deposited.
     * Once all parts are received, the message is reassembled and handled.
     */
    private fun handleMultipartMsg(peerMsgPart: PeerMsgPart){
        val existingIncomingMultipartMsg = incomingMultipartP2PMsgs[peerMsgPart.msgId]
        if(existingIncomingMultipartMsg == null){
            if(peerMsgPart.part == 0){
                logger.debugInfo("received first part (part: 0) of P2P message (tot: ${peerMsgPart.total}) from node $remoteNodeId (msg id: ${peerMsgPart.msgId}) on channel ${peerMsgPart.channel}, creating new IncomingMultipartP2PMsg")
                incomingMultipartP2PMsgs[peerMsgPart.msgId] = IncomingMultipartP2PMsg(
                    peerMsgPart, settings.multipartP2PMessageTimeoutMillis
                ){
                    incomingMultipartP2PMsgs.remove(peerMsgPart.msgId)
                    logger.debugErr("timeout on receiving P2P message from CrolangNode $remoteNodeId with msg id ${peerMsgPart.msgId}")
                }
            } else {
                logger.debugErr("received part ${peerMsgPart.part} of P2P message from node $remoteNodeId (msg id: ${peerMsgPart.msgId}) but it is not the first part, discarding msg")
            }
        } else if(existingIncomingMultipartMsg.isPartToDeposit(peerMsgPart)) {
            logger.debugInfo("depositing part ${peerMsgPart.part} (tot: ${peerMsgPart.total}) of P2P message from node $remoteNodeId (msg id: ${peerMsgPart.msgId}) into existing IncomingMultipartP2PMsg")
            existingIncomingMultipartMsg.depositNewPart(peerMsgPart).ifPresent {
                if(it.isError || it.msg.isEmpty){
                    logger.debugErr("error on merging P2P message parts from CrolangNode $remoteNodeId with msg id ${peerMsgPart.msgId}")
                } else {
                    logger.regularInfo("successfully merged all ${peerMsgPart.total} P2P message parts from node $remoteNodeId with msg id ${peerMsgPart.msgId}")
                    handleCompleteMsg(it.msg.get())
                }
                incomingMultipartP2PMsgs.remove(peerMsgPart.msgId)
            }
        } else {
            existingIncomingMultipartMsg.cancelTimer()
            incomingMultipartP2PMsgs.remove(peerMsgPart.msgId)
            logger.debugErr("received out of order part ${peerMsgPart.part} of P2P message from node $remoteNodeId (msg id: ${peerMsgPart.msgId}), discarding msg")
        }
    }

    /**
     * Handles the reception of a complete P2P message.
     * The message is logged and then passed to the appropriate callback defined by the user.
     */
    private fun handleCompleteMsg(peerMsg: PeerMsg){
        logger.regularInfo("received P2P message from node $remoteNodeId on channel ${peerMsg.channel}: ${peerMsg.payload}")
        val channelCallback = userDefinedCallbacks.onNewMsg[peerMsg.channel]
        if(channelCallback != null){
            executeCallbackOnExecutor {
                channelCallback(this.crolangNode, peerMsg.payload)
            }
        } else {
            logger.regularErr("received P2P message on unknown channel ${peerMsg.channel} from node $remoteNodeId")
        }
    }

    /**
     * Sends a P2P message to the remote node.
     * The message is split into parts, each of which is sent separately.
     * The message will be recomposed on the remote node.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun sendP2PMsg(channel: String, msg: String): Boolean {
        if(state != NodeState.CONNECTED || dataChannel.isEmpty || !dataChannel.get().state.equals(RTCDataChannelState.OPEN)){
            logger.regularErr("attempted to send message to CrolangNode $remoteNodeId that is not connected")
            return false
        }
        logger.regularInfo("sending P2P message to CrolangNode $remoteNodeId: $msg")
        val peerMsg = PeerMsg(nextP2PMsgSentId++, channel, msg)
        val parts: List<PeerMsgPartParsable>  = peerMsg.splitIntoParts(DEFAULT_PAYLOAD_SIZE_BYTES)
        logger.debugInfo("split P2P message to $remoteNodeId (msg id: ${peerMsg.msgId}) into ${parts.size} parts")
        val dataChannelInstance = dataChannel.get()
        if(parts.size == 1){
            while(dataChannelInstance.bufferedAmount > 0) {
                Thread.sleep(1)
            }
            dataChannel.get().send(RTCDataChannelBuffer(
                ByteBuffer.wrap(cborParser.encodeToByteArray(PeerMsgPartParsable.serializer(), parts[0])),
                true
            ))
            logger.debugInfo("sent single part P2P message to $remoteNodeId (msg id: ${peerMsg.msgId})")
        } else {
            parts.forEach {
                val buffer = RTCDataChannelBuffer(
                    ByteBuffer.wrap(cborParser.encodeToByteArray(PeerMsgPartParsable.serializer(), it)),
                    true
                )
                while(dataChannelInstance.bufferedAmount > MAX_BUFFERED_AMOUNT) {
                    Thread.sleep(1)
                }
                logger.debugInfo("sending part ${it.part}/${it.total} of P2P message to $remoteNodeId (msg id: ${peerMsg.msgId})")
                dataChannelInstance.send(buffer)
                //Thread.sleep(1)
            }
            logger.debugInfo("sent all P2P message parts to $remoteNodeId (msg id: ${peerMsg.msgId})")
        }
        return true
    }
}
