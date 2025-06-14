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

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelState
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import org.crolangP2P.CrolangP2P
import internal.events.data.IceCandidateMsg
import internal.events.data.ParsableConnectionRefusalMsg
import internal.events.data.ParsableIceCandidateMsg
import internal.events.data.ParsableIncomingConnectionsNotAllowedMsg
import internal.events.data.ParsableSessionDescriptionMsg
import internal.events.data.PeerMsgPart
import internal.events.data.SessionDescriptionMsg
import internal.events.data.abstractions.SocketMsgType.Companion.CONNECTION_ACCEPTANCE
import internal.events.data.abstractions.SocketMsgType.Companion.CONNECTION_ATTEMPT
import internal.events.data.abstractions.SocketMsgType.Companion.CONNECTION_REFUSAL
import internal.events.data.abstractions.SocketMsgType.Companion.ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER
import internal.events.data.abstractions.SocketMsgType.Companion.ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR
import internal.events.data.abstractions.SocketMsgType.Companion.INCOMING_CONNECTIONS_NOT_ALLOWED
import internal.events.data.abstractions.SocketResponses
import internal.events.data.adapters.AgnosticRTCSessionDescription
import internal.node.NodeState
import internal.node.ResponderNode
import internal.BuildConfig
import internal.utils.Event
import internal.utils.EventLoop
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.brokerPeersContainer
import internal.utils.SharedStore.incomingCrolangNodesCallbacks
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.parser
import internal.utils.SharedStore.rtcConfiguration
import internal.utils.SharedStore.socketIO
import io.socket.client.Ack
import java.util.*

/*
 * This file contains the events that can be triggered by a responder node.
 * A responder node is a CrolangNode that gets contacted by another CrolangNode, called initiator.
 *
 * I was doubtful on how to handle this for more clarity, but I've figured out that having all the possible events
 * involving the responder listed in a single file ordered by the order they are triggered is the clearest way to do it.
 *
 * Read it from top to bottom as it was the flow of the events for the responder.
 */

/**
 * This event is triggered when a connection attempt is received from an initiator node.
 * It checks if incoming connections are allowed, if the node is already talking to the initiator,
 * and if the user-defined callback for incoming connections is present and returns true.
 * If all checks pass, it starts the negotiation process.
 *
 * @param msg the session description message received from the initiator, containing data for establishing p2p connection
 */
internal class OnIncomingConnectionAttemptMsg(private val msg: SessionDescriptionMsg) : Event {

    override fun process() {
        if(!CrolangP2P.Kotlin.areIncomingConnectionsAllowed()){
            onIncomingConnectionsNotAllowed()
        } else if(alreadyTalking(msg)){
            logger.debugErr("received an $CONNECTION_ATTEMPT socket msg from ${msg.from} that we are already talking to, discarding it")
        } else if(incomingCrolangNodesCallbacks.isEmpty){
            logger.debugErr("error while retrieving incoming connections callbacks on $CONNECTION_ATTEMPT socket msg from ${msg.from}, discarding it")
        } else if(!incomingCrolangNodesCallbacks.get().onConnectionAttempt(msg.from, msg.platformFrom, msg.versionFrom)) {
            onConnectionAttemptRefusedByUserDefinedCallback()
        } else {
            onConnectionAttemptAccepted()
        }
    }

    private fun onIncomingConnectionsNotAllowed(){
        logger.debugInfo("received $CONNECTION_ATTEMPT socket msg but incoming connections are not allowed, discarding it")
        if(socketIO.isPresent && socketIO.get().connected()){
            val incomingConnectionsNotAllowedMsg = ParsableIncomingConnectionsNotAllowedMsg()
            incomingConnectionsNotAllowedMsg.platformFrom = BuildConfig.MY_PLATFORM
            incomingConnectionsNotAllowedMsg.versionFrom = BuildConfig.VERSION
            incomingConnectionsNotAllowedMsg.to = msg.from
            incomingConnectionsNotAllowedMsg.from = localNodeId
            incomingConnectionsNotAllowedMsg.sessionId = msg.sessionId
            socketIO.get().emit(INCOMING_CONNECTIONS_NOT_ALLOWED, parser.toJson(incomingConnectionsNotAllowedMsg), Ack {})
        }
    }

    private fun alreadyTalking(msg: SessionDescriptionMsg): Boolean {
        return brokerPeersContainer.initiatorNodes.contains(msg.from) ||
                brokerPeersContainer.responderNodes.contains(msg.from)
    }

    private fun onConnectionAttemptRefusedByUserDefinedCallback(){
        logger.debugInfo("received $CONNECTION_ATTEMPT socket msg from ${msg.from}, refused by onConnectionAttempt callback, sending $CONNECTION_REFUSAL")
        if(socketIO.isPresent && socketIO.get().connected()){
            val connectionRefusalMsg = ParsableConnectionRefusalMsg()
            connectionRefusalMsg.platformFrom = BuildConfig.MY_PLATFORM
            connectionRefusalMsg.versionFrom = BuildConfig.VERSION
            connectionRefusalMsg.to = msg.from
            connectionRefusalMsg.from = localNodeId
            connectionRefusalMsg.sessionId = msg.sessionId
            socketIO.get().emit(CONNECTION_REFUSAL, parser.toJson(connectionRefusalMsg), Ack {})
        }
    }

    private fun onConnectionAttemptAccepted(){
        logger.debugInfo("received $CONNECTION_ATTEMPT socket msg from ${msg.from}, starting negotiating")
        rtcConfiguration.ifPresentOrElse(
            { rtcConfiguration ->
                val remoteNodeId = msg.from
                val offer = msg.sessionDescription
                val newNode = ResponderNode(
                    rtcConfiguration, remoteNodeId, msg.sessionId, incomingCrolangNodesCallbacks.get()
                )
                newNode.setRemoteInfo(msg.platformFrom, msg.versionFrom)
                brokerPeersContainer.responderNodes[remoteNodeId] = newNode
                logger.debugInfo("local ResponderNode $remoteNodeId created, setting remote description")
                newNode.state = NodeState.DESCRIPTIONS_EXCHANGE
                newNode.peer.setRemoteDescription(offer, object : SetSessionDescriptionObserver {

                    override fun onSuccess() {
                        EventLoop.postEvent(OnRemoteDescriptionSetSuccessfullyResponderNode(remoteNodeId))
                    }

                    override fun onFailure(failureErr: String?) {
                        EventLoop.postEvent(OnRemoteDescriptionSetFailureResponderNode(remoteNodeId, failureErr))
                    }

                })
            },
            { logger.debugErr("error while retrieving RTC configuration on $CONNECTION_ATTEMPT socket msg from ${msg.from}, discarding it") }
        )
    }

}

/**
 * Abstract class representing an event related to a responder node in a peer-to-peer connection attempt.
 *
 * This abstract class defines the basic behavior for all events that involve a responder node.
 *
 * @param remoteNodeId The ID of the remote initiator Node that started the connection attempt.
 */
internal abstract class ResponderNodeAbstractEvent(protected val remoteNodeId: String) : Event {

    /**
     * Processes the event by looking up the responder Node in the collection of remote peers.
     * If the node is found, the action defined in [onNodeFound] is executed. Otherwise, an error is logged.
     */
    override fun process(){
        val node = brokerPeersContainer.responderNodes[remoteNodeId]
        if(node != null){
            onNodeFound(node)
        } else {
            logger.debugErr("local NegotiatingResponderNode $remoteNodeId not found on event ${this::class.simpleName}, ignoring")
        }
    }

    /**
     * Defines the action to take when the responder node is found.
     * This method must be implemented by subclasses to provide specific handling for the event.
     *
     * @param node The responder node found in the remote peer container.
     */
    abstract fun onNodeFound(node: ResponderNode)

    protected fun appendFailureString(failureErr: String?, msg: String): String{
        if(failureErr == null){
            return msg
        }
        return "$msg: $failureErr"
    }

}

/**
 * This event is triggered when a connection attempt reaches timeout.
 *
 * @param remoteNodeId The ID of the remote initiator Node that started the connection attempt.
 */
internal class OnConnectionAttemptTimeoutResponderNode(
    remoteNodeId: String
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        if(node.state == NodeState.DESCRIPTIONS_EXCHANGE || node.state == NodeState.ICE_CANDIDATES_EXCHANGE){
            logger.debugErr("connection attempt to remote NegotiatingInitiatorNode $remoteNodeId timed out")
            node.forceClose(NodeState.TIMEOUT)
        } else if(node.state != NodeState.CONNECTED){
            // this case should not be possible since timeout can only occur while negotiating
            logger.debugErr(
                "connection attempt to remote NegotiatingInitiatorNode $remoteNodeId timed out but state is ${node.state}, discarding it"
            )
            // ensuring that the node is removed from the container, since forceClose won't do that having the state as closed
            brokerPeersContainer.responderNodes.remove(remoteNodeId)
        }
    }

}

/**
 * Class representing an event triggered when a negative response is received from the Broker during
 * the nodes connection negotiation process.
 *
 * This event occurs when an error is encountered while sending a specific message (identified by the
 * `msgType`) to a remote initiator node through the Broker. It handles the failure by logging
 * the error, marking the connection attempt as failed, and closing the connection.
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is attempting to connect to.
 * @param msgType The type of the message that was attempted to be sent when the failure occurred.
 */
internal class OnSocketMsgBrokerNegativeResponseResponderNode(
    remoteNodeId: String, private val msgType: String
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugErr("error while sending $msgType msg to remote ${remoteNodeId}, closing connection attempt")
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }

}

/**
 * This event is triggered when the remote description is set successfully on the local responder node.
 * THe remote description set attempt is performed when receiving a CONNECTION_ATTEMPT.
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is attempting to connect to.
 */
internal class OnRemoteDescriptionSetSuccessfullyResponderNode(
    remoteNodeId: String
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugInfo("remote description set on local ResponderNode $remoteNodeId, creating answer")
        node.peer.createAnswer(RTCAnswerOptions(), object : CreateSessionDescriptionObserver {

            override fun onSuccess(description: RTCSessionDescription?) {
                EventLoop.postEvent(OnAnswerCreationSuccessResponderNode(remoteNodeId, description))
            }

            override fun onFailure(failureErr: String?) {
                EventLoop.postEvent(OnAnswerCreationFailureResponderNode(remoteNodeId, failureErr))
            }

        })
    }

}

/**
 * Represents an event triggered when the responder node fails to set a remote session description
 * during the connection negotiation process.
 *
 * This event occurs when the responder node attempts to set the remote session description received from the
 * initiator node but fails to do so. The failure can be due to various reasons, and this event is responsible for
 * handling the error, logging the failure, and marking the connection attempt as unsuccessful.
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is trying to connect to.
 * @param failureErr A string containing the error message describing the failure, or `null` if no error message is provided.
 */
internal class OnRemoteDescriptionSetFailureResponderNode(
    remoteNodeId: String, private val failureErr: String?
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugErr(appendFailureString(
            failureErr, "error setting remote description on local ResponderNode $remoteNodeId"
        ))
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }

}

/**
 * This event is triggered after the remote initiator's offer is set successfully locally and an offer is created successfully.
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is attempting to connect to.
 * @param description The RTCSessionDescription object representing the answer created by the responder node.
 */
internal class OnAnswerCreationSuccessResponderNode(
    remoteNodeId: String, private val description: RTCSessionDescription?
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        if(description == null){
            logger.debugErr("error setting remote description on local ResponderNode $remoteNodeId (remote description is null)")
            node.forceClose(NodeState.NEGOTIATION_ERROR)
        } else {
            logger.debugInfo("answer created on local ResponderNode $remoteNodeId, setting local description")
            node.peer.setLocalDescription(description, object : SetSessionDescriptionObserver {

                override fun onSuccess() {
                    EventLoop.postEvent(OnLocalDescriptionSetSuccessResponderNode(remoteNodeId, description))
                }

                override fun onFailure(failureErr: String?) {
                    EventLoop.postEvent(OnLocalDescriptionSetFailureResponderNode(remoteNodeId, failureErr))
                }

            })
        }
    }

}

/**
 * This event is triggered when the responder node fails to set the local session description
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is attempting to connect to.
 * @param failureErr A string containing the error message describing the failure, or `null` if no error message is provided.
 */
internal class OnAnswerCreationFailureResponderNode(
    remoteNodeId: String, private val failureErr: String?
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugErr(appendFailureString(
            failureErr, "error while creating answer on local ResponderNode $remoteNodeId"
        ))
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }

}

/**
 * This event is triggered when the local session description is set successfully on the responder node.
 * This happens after an answer is created.
 *
 * The description (answer) is sent to the initiator via a socket message called CONNECTION_ACCEPTANCE.
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is attempting to connect to.
 * @param description The RTCSessionDescription object representing the answer created by the responder node.
 */
internal class OnLocalDescriptionSetSuccessResponderNode(
    remoteNodeId: String, private val description: RTCSessionDescription
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugInfo("local description set successfully on local ResponderNode $remoteNodeId, converting answer to agnostic representation")
        node.state = NodeState.ICE_CANDIDATES_EXCHANGE
        AgnosticRTCSessionDescription.adaptConcrete(description).ifPresentOrElse(
            {
                logger.debugInfo("answer converted to agnostic representation on local ResponderNode $remoteNodeId, sending $CONNECTION_ACCEPTANCE socket msg")
                val msg = ParsableSessionDescriptionMsg()
                msg.platformFrom = BuildConfig.MY_PLATFORM
                msg.versionFrom = BuildConfig.VERSION
                msg.to = remoteNodeId
                msg.from = localNodeId
                msg.sessionId = node.sessionId
                msg.sessionDescription = it
                node.sendSocketMsg(CONNECTION_ACCEPTANCE, msg) { response ->
                    if(!SocketResponses.isOk(response)){
                        EventLoop.postEvent(
                            OnSocketMsgBrokerNegativeResponseResponderNode(remoteNodeId, CONNECTION_ACCEPTANCE)
                        )
                    }
                }
            },
            {
                logger.debugErr("error while converting answer to agnostic representation on local ResponderNode $remoteNodeId")
                node.forceClose(NodeState.NEGOTIATION_ERROR)
            }
        )
    }

}

/**
 * This event is triggered when the responder node fails to set the local session description.
 *
 * @param remoteNodeId The ID of the remote initiator node that the responder node is attempting to connect to.
 * @param failureErr A string containing the error message describing the failure, or `null` if no error message is provided.
 */
internal class OnLocalDescriptionSetFailureResponderNode(
    remoteNodeId: String, private val failureErr: String?
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugErr(appendFailureString(
            failureErr, "error while setting local description on local ResponderNode $remoteNodeId"
        ))
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }

}

/**
 * This event is triggered when the responder node has generated a new ice candidate to send to the initiator.
 *
 * If the responder node is in the correct state to send the ICE candidate, the event triggers the sending of the ICE candidate message.
 * If the responder node is not in the correct state, the message is discarted.
 */
internal class OnIceCandidateReadyToBeSentResponderNode(
    remoteNodeId: String, private val msg: ParsableIceCandidateMsg
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        if(node.state == NodeState.DESCRIPTIONS_EXCHANGE || node.state == NodeState.ICE_CANDIDATES_EXCHANGE){
            logger.debugInfo("new $ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR socket msg ready to be sent to remote $remoteNodeId, sending it")
            node.sendIceCandidatesExchangeMsg(ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR, msg)
        } else {
            logger.debugInfo("new $ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR socket msg ready to be sent to remote $remoteNodeId, but state is ${node.state}, discarding it")
        }
    }

}

/**
 * This event is triggered when the responder node receives an ICE candidate message from the initiator node.
 *
 * @param msg The message containing the ICE candidate data.
 */
internal class OnIncomingP2PIceCandidatesExchangeMsgResponderNode(
    private val msg: IceCandidateMsg
) : ResponderNodeAbstractEvent(msg.from) {

    override fun onNodeFound(node: ResponderNode) {
        if(node.sessionId != msg.sessionId){
            logger.debugErr("received $ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER socket msg from remote InitiatorNode ${msg.from} but the session id is different, discarding it")
        } else if(node.state == NodeState.ICE_CANDIDATES_EXCHANGE){
            logger.debugInfo("received $ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER socket msg from remote InitiatorNode ${msg.from}, adding ice candidate to its corresponding local ResponderNode")
            node.peer.addIceCandidate(msg.candidate)
        } else {
            logger.debugInfo("received $ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER socket msg from remote InitiatorNode ${msg.from} but the node has state ${node.state}, discarding it")
        }

    }
}

/**
 * Represents an event triggered when the responder node RTCPeerConnection's state changes.
 *
 * This event only checks for disconnection and failed, since the completion of successful connection is demanded to
 * the [OnDataChannelStateChangeResponderNode] event.
 * If the connection state changes to disconnected or failed, the responder node is forced to close the connection.
 *
 * @param remoteNodeId The ID of the remote initiator node.
 * @param state The new state of the RTCPeerConnection.
 */
internal class OnP2PConnectionStateChangeResponderNode(
    remoteNodeId: String, private val state: RTCPeerConnectionState
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugInfo("peer connection to responder node $remoteNodeId state changed: $state")
        if(state == RTCPeerConnectionState.DISCONNECTED || state == RTCPeerConnectionState.FAILED){
            node.forceClose(NodeState.DISCONNECTED)
        }
    }

}

/**
 * Represents an event triggered when the responder node's data channel state changes.
 *
 * This event occurs when the data channel state changes for the responder node. If the data channel state changes to
 * 'open' and the node is still negotiating, the connection is considered successful, and the responder node is marked as connected,
 * calling the user-defined callback.
 * If the data channel state changes to 'closed' while the node is negotiating, the connection negotiation is considered failed.
 * If the data channel state changes to 'closed' while the node is connected, the connection is considered closed.
 *
 * @param remoteNodeId The ID of the remote initiator node.
 * @param state The new state of the data channel.
 */
internal class OnDataChannelStateChangeResponderNode(
    remoteNodeId: String, private val channel: RTCDataChannel, private val state: RTCDataChannelState
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        logger.debugInfo("data channel state changed to $state with remote InitiatorNode $remoteNodeId")
        if(state == RTCDataChannelState.OPEN && node.isNegotiating(node.state)){
            node.connectionTimeoutTimer.cancel()
            node.dataChannel = Optional.of(channel)
            logger.debugInfo("data channel opened with remote NegotiatingInitiatorNode $remoteNodeId")
            node.state = NodeState.CONNECTED
            executeCallbackOnExecutor { node.incomingConnectionCallbacks.onConnectionSuccess(node.crolangNode) }
        } else if(state == RTCDataChannelState.CLOSED && node.isNegotiating(node.state)){
            node.forceClose(NodeState.NEGOTIATION_ERROR)
        } else if(state == RTCDataChannelState.CLOSED && node.state == NodeState.CONNECTED){
            node.forceClose(NodeState.DISCONNECTED)
        }
    }

}

/**
 * Represents an event triggered when the responder node receives an incoming P2P message part from the remote initiator node.
 *
 * This event occurs only when the nodes are fully connected, since P2P messages cannot be exchanged before establishing
 * a connection between them.
 * The incoming message is deposited in the responder node's message queue for processing.
 *
 * @param remoteNodeId The ID of the remote initiator node that sent the message.
 * @param msg The incoming message from the initiator node.
 */
internal class OnIncomingP2PMsgPartResponderNode(
    remoteNodeId: String, private val msg: PeerMsgPart
) : ResponderNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: ResponderNode) {
        node.depositNewMsgPart(msg)
    }

}
