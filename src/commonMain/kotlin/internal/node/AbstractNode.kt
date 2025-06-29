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

import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelObserver
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import internal.dependencies.webrtc.contracts.CrolangP2PRTCPeerConnection
import internal.events.data.IceCandidateMsg
import internal.events.data.IncomingMultipartP2PMsg
import internal.events.data.ParsableIceCandidateMsg
import internal.events.data.PeerMsg
import internal.events.data.PeerMsgPart
import internal.events.data.PeerMsgPartParsable
import internal.events.data.abstractions.P2PMsgTypes
import internal.events.data.abstractions.SocketResponses
import internal.events.data.adapters.IceCandidateAdapter
import internal.utils.SharedStore
import internal.utils.SharedStore.cborParser
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.parser
import internal.utils.SharedStore.settings
import internal.utils.SharedStore.socket
import internal.utils.TimeoutTimer
import kotlinx.serialization.ExperimentalSerializationApi
import org.crolangP2P.BasicCrolangNodeCallbacks
import org.crolangP2P.CrolangNode

private const val DEFAULT_PAYLOAD_SIZE_BYTES: Int = 15000
private const val MAX_BUFFERED_AMOUNT = 512 * 1024 // 512 KB

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
 * @property remoteVersion The version of the remote node.
 * @property remotePlatform The platform of the remote node.
 * @property state The current state of the node (e.g., CREATED, CONNECTED).
 * @property peer The WebRTC peer connection object.
 * @property dataChannel The nullable data channel for communication.
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
    rtcConfiguration: CrolangP2PRTCConfiguration,
    val remoteNodeId: String,
    val sessionId: String,
    private val userDefinedCallbacks: BasicCrolangNodeCallbacks,
    val newDataChannelRemotelyCreatedObserver: (channel: CrolangP2PRTCDataChannel) -> CrolangP2PRTCDataChannelObserver,
    private val concreteNodeEventParameters: ConcreteNodeEventParameters
) {

    var remotePlatform: String = ""
    var remoteVersion: String = ""
    var crolangNode: CrolangNode = CrolangNode.create(this)
    var state: NodeState = NodeState.CREATED
    val peer: CrolangP2PRTCPeerConnection = createPeer(rtcConfiguration)
    var dataChannel: CrolangP2PRTCDataChannel? = null
    val connectionTimeoutTimer = TimeoutTimer(settings.p2pConnectionTimeoutMillis){
        SharedStore.dependencies!!.eventLoop.postEvent(concreteNodeEventParameters.onConnectionAttemptTimeout())
    }
    private var nextP2PMsgSentId = 0
    private val incomingMultipartP2PMsgs: MutableMap<Int, IncomingMultipartP2PMsg> = mutableMapOf()
    val suspendedOutgoingTeamDetailsMsgs: MutableList<ParsableIceCandidateMsg> = mutableListOf()
    val suspendedIncomingTeamDetailsMsgs: MutableList<IceCandidateMsg> = mutableListOf()

    fun setRemoteInfo(remotePlatform: String, remoteVersion: String){
        this.remotePlatform = remotePlatform
        this.remoteVersion = remoteVersion
        crolangNode = CrolangNode.create(this)
    }

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
            dataChannel?.let {
                if(it.state() == CrolangP2PRTCDataChannelState.CONNECTING || it.state() == CrolangP2PRTCDataChannelState.OPEN){
                    it.close()
                }
            }
            if(peer.connectionState() == CrolangP2PRTCPeerConnectionState.CONNECTED || peer.connectionState() == CrolangP2PRTCPeerConnectionState.CONNECTING){
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
    inline fun <reified T> sendSocketMsg(type: String, msg: T, crossinline onBrokerResponse: (response: String) -> Any){
        if(!isNegotiating(state)){
            logger.debugErr("Tried to send a socket msg to remote node $remoteNodeId while the CrolangNode is not negotiating")
        } else if(socket == null){
            logger.debugErr("Tried to send a socket msg to remote node $remoteNodeId while the socket is not initialized")
        } else if(!socket!!.connected()){
            logger.debugErr("Tried to send a socket msg to remote node $remoteNodeId while the socket is not connected")
        } else {
            logger.debugInfo("sending $type to remote node $remoteNodeId")
            socket!!.emit(type, parser.toJson<T>(msg)) { args ->
                val response = if (args.size != 1 || args[0] !is String) {
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
            }
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
                SharedStore.dependencies!!.eventLoop.postEvent(
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
    private fun createPeer(rtcConfiguration: CrolangP2PRTCConfiguration): CrolangP2PRTCPeerConnection {
        return SharedStore.dependencies!!.crolangP2PPeerConnectionFactory.createPeerConnection(
            rtcConfiguration,
            onIceCandidate = {
                val msg = ParsableIceCandidateMsg()
                msg.platformFrom = SharedStore.dependencies!!.myPlatform
                msg.versionFrom = SharedStore.dependencies!!.myVersion
                msg.to = remoteNodeId
                msg.from = localNodeId
                msg.sessionId = sessionId
                msg.candidate = IceCandidateAdapter.adaptConcrete(it)
                SharedStore.dependencies!!.eventLoop.postEvent(concreteNodeEventParameters.onIceCandidateReadyToBeSent(msg))
            },
            onConnectionChange = { SharedStore.dependencies!!.eventLoop.postEvent(concreteNodeEventParameters.onConnectionStateChange(it)) },
            onDataChannel = { it.registerObserver(newDataChannelRemotelyCreatedObserver(it)) }
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
            handleCompleteMsg(PeerMsg(msgPart.msgType, msgPart.msgId, msgPart.channel, msgPart.payload))
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
            existingIncomingMultipartMsg.depositNewPart(peerMsgPart)?.let {
                if(it.isError || it.msg == null){
                    logger.debugErr("error on merging P2P message parts from CrolangNode $remoteNodeId with msg id ${peerMsgPart.msgId}")
                } else {
                    logger.regularInfo("successfully merged all ${peerMsgPart.total} P2P message parts from node $remoteNodeId with msg id ${peerMsgPart.msgId}")
                    handleCompleteMsg(it.msg)
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
     * The message is logged and then passed to the appropriate callback defined by the user, if it is a p2p user msg.
     */
    private fun handleCompleteMsg(peerMsg: PeerMsg){
        if(peerMsg.msgType == P2PMsgTypes.USER_MSG){
            logger.regularInfo("received P2P message from node $remoteNodeId on channel ${peerMsg.channel}: ${peerMsg.payload}")
            val channelCallback = userDefinedCallbacks.onNewMsg[peerMsg.channel]
            if(channelCallback != null){
                executeCallbackOnExecutor {
                    channelCallback(this.crolangNode, peerMsg.payload)
                }
            } else {
                logger.regularErr("received P2P message on unknown channel ${peerMsg.channel} from node $remoteNodeId")
            }
        } else {
            logger.debugErr("received P2P message of unknown type ${peerMsg.msgType} from node $remoteNodeId, ignoring it")
        }
    }

    /**
     * Sends a P2P message to the remote node.
     * The message is split into parts, each of which is sent separately.
     * The message will be recomposed on the remote node.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun sendP2PMsg(channel: String, msg: String): Boolean {
        if(state != NodeState.CONNECTED || dataChannel == null || dataChannel!!.state() != CrolangP2PRTCDataChannelState.OPEN){
            logger.regularErr("attempted to send message to CrolangNode $remoteNodeId that is not connected")
            return false
        }
        logger.regularInfo("sending P2P message to CrolangNode $remoteNodeId: $msg")
        val peerMsg = PeerMsg(P2PMsgTypes.USER_MSG, nextP2PMsgSentId++, channel, msg)
        val parts: List<PeerMsgPartParsable>  = peerMsg.splitIntoParts(DEFAULT_PAYLOAD_SIZE_BYTES)
        logger.debugInfo("split P2P message to $remoteNodeId (msg id: ${peerMsg.msgId}) into ${parts.size} parts")
        val dataChannelInstance = dataChannel!!
        if(parts.size == 1){
            while(dataChannelInstance.bufferedAmount() > MAX_BUFFERED_AMOUNT) {
                SharedStore.dependencies!!.sleepProvider.sleep(1)
            }
            dataChannel!!.send(cborParser.encodeToByteArray(PeerMsgPartParsable.serializer(), parts[0]))
            logger.debugInfo("sent single part P2P message to $remoteNodeId (msg id: ${peerMsg.msgId})")
        } else {
            parts.forEach {
                val byteArray = cborParser.encodeToByteArray(PeerMsgPartParsable.serializer(), it)
                while(dataChannelInstance.bufferedAmount() > MAX_BUFFERED_AMOUNT) {
                    SharedStore.dependencies!!.sleepProvider.sleep(1)
                }
                logger.debugInfo("sending part ${it.part}/${it.total} of P2P message to $remoteNodeId (msg id: ${peerMsg.msgId})")
                dataChannelInstance.send(byteArray)
            }
            logger.debugInfo("sent all P2P message parts to $remoteNodeId (msg id: ${peerMsg.msgId})")
        }
        return true
    }
}
