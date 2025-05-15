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
import dev.onvoid.webrtc.RTCDataChannelState
import dev.onvoid.webrtc.RTCOfferOptions
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import internal.events.data.ConnectionRefusalMsg
import internal.events.data.IceCandidateMsg
import internal.events.data.IncomingConnectionsNotAllowedMsg
import internal.events.data.ParsableIceCandidateMsg
import internal.events.data.ParsableSessionDescriptionMsg
import internal.events.data.PeerMsgPart
import internal.events.data.SessionDescriptionMsg
import internal.events.data.abstractions.DirectMsg
import internal.events.data.abstractions.SocketMsgType.Companion.CONNECTION_ACCEPTANCE
import internal.events.data.abstractions.SocketMsgType.Companion.CONNECTION_ATTEMPT
import internal.events.data.abstractions.SocketMsgType.Companion.ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER
import internal.events.data.abstractions.SocketMsgType.Companion.ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR
import internal.events.data.adapters.AgnosticRTCSessionDescription
import org.crolangP2P.P2PConnectionFailedReason
import internal.node.InitiatorNode
import internal.node.NodeState
import internal.utils.Event
import internal.utils.EventLoop
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.brokerPeersContainer
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger

/*
 * This class contains all the events that can be triggered by an initiator node.
 * An initiator node is a CrolangNode that is trying to connect to another CrolangNode, called responder.
 *
 * I was doubtful on how to handle this for more clarity, but I've figured out that having all the possible events
 * involving the initiator listed in a single file ordered by the order they are triggered is the clearest way to do it.
 *
 * Read it from top to bottom as it was the flow of the events for the initiator.
 */

/**
 * Abstract class representing an event related to an initiator node in a peer-to-peer connection attempt.
 *
 * This abstract class defines the basic behavior for all events that involve an initiator node.
 *
 * @param remoteNodeId The ID of the remote node with which the initiator node is attempting to establish a connection.
 */
internal abstract class InitiatorNodeAbstractEvent(protected val remoteNodeId: String) : Event {

    /**
     * Processes the event by looking up the initiator Node in the collection of remote peers.
     * If the node is found, the action defined in [onNodeFound] is executed. Otherwise, an error is logged.
     */
    override fun process() {
        val node = brokerPeersContainer.initiatorNodes[remoteNodeId]
        if (node != null) {
            onNodeFound(node)
        } else {
            logger.debugErr("local InitiatorNode $remoteNodeId not found on event ${this::class.simpleName}, ignoring")
        }
    }

    /**
     * Defines the action to take when the initiator node is found.
     * This method must be implemented by subclasses to provide specific handling for the event.
     *
     * @param node The initiator node found in the remote peer container.
     */
    abstract fun onNodeFound(node: InitiatorNode)

    /**
     * Handles errors during the connection negotiation between the initiator and responder nodes.
     *
     * @param node The initiator node involved in the failed connection attempt.
     * @param msg A message describing the error.
     * @param failureErr An optional error message describing the failure cause.
     */
    protected fun connectionNegotiationError(node: InitiatorNode, msg: String, failureErr: String? = null) {
        if (failureErr == null) {
            logger.debugErr("$msg $remoteNodeId")
        } else {
            logger.debugErr("$msg $remoteNodeId: $failureErr")
        }
        node.failedConnectionPeers[remoteNodeId] = P2PConnectionFailedReason.CONNECTION_NEGOTIATION_ERROR
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }
}

/**
 * Abstract class representing an event related to a socket message received by an initiator node in a peer-to-peer connection attempt.
 *
 * This abstract class defines the basic behavior for all events that involve an initiator node.
 *
 * @param remoteNodeId The ID of the remote node with which the initiator node is attempting to establish a connection.
 * @param msg the socket message
 */
internal abstract class InitiatorNodeOnSocketMsgAbstractEvent(
    remoteNodeId: String, val msg: DirectMsg
): InitiatorNodeAbstractEvent(remoteNodeId){

    /**
     * Processes the event by looking up the initiator node in the collection of remote peers.
     * If the node is found and the message's sessionId coincides, the action defined in [onNodeFound] is executed.
     * Otherwise, an error is logged.
     */
    override fun process() {
        val node = brokerPeersContainer.initiatorNodes[remoteNodeId]
        if (node == null) {
            logger.debugErr("local InitiatorNode $remoteNodeId not found on event ${this::class.simpleName}, ignoring")
        } else if(msg.sessionId != node.sessionId){
            logger.debugErr("sessionId ${msg.sessionId} does not match with local InitiatorNode $remoteNodeId sessionId ${node.sessionId}, ignoring")
        } else {
            onNodeFound(node)
        }
    }

}

/**
 * Class representing an event triggered when a connection attempt to a remote node times out.
 *
 * This class handles the timeout scenario during the connection negotiation process. If the initiator node
 * does not successfully establish a connection within the expected time frame, this event is triggered to
 * handle the timeout and close the connection attempt.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 */
internal class OnConnectionAttemptTimeoutInitiatorNode(
    remoteNodeId: String
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        // If the connection attempt was during the descriptions exchange or ICE candidates exchange
        if (node.state == NodeState.DESCRIPTIONS_EXCHANGE || node.state == NodeState.ICE_CANDIDATES_EXCHANGE) {
            node.failedConnectionPeers[remoteNodeId] = P2PConnectionFailedReason.CONNECTION_TIMEOUT
            logger.debugErr("connection attempt to remote ResponderNode $remoteNodeId timed out")
            node.forceClose(NodeState.TIMEOUT)
            node.countdownMissingNodes()
        } else if (node.state != NodeState.CONNECTED) {
            // This case should not be possible as timeout should only occur during negotiation
            logger.debugErr(
                "connection to remote ResponderNode $remoteNodeId timed out while state is ${node.state}, removing it"
            )
            // Ensure the node is removed from the container, since forceClose won't do that if the state is 'closed'
            brokerPeersContainer.initiatorNodes.remove(remoteNodeId)
        }
    }
}

/**
 * Class representing an event triggered when a negative response is received from the Broker during
 * the nodes connection negotiation process.
 *
 * This event occurs when an error is encountered while sending a specific message (identified by the
 * `msgType`) to a remote responder node through the Broker. It handles the failure by logging
 * the error, marking the connection attempt as failed, and closing the connection.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is attempting to connect to.
 * @param msgType The type of the message that was attempted to be sent when the failure occurred.
 */
internal class OnSocketMsgBrokerNegativeResponseInitiatorNode(
    remoteNodeId: String, private val msgType: String
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        connectionNegotiationError(
            node, "error while sending $msgType msg to remote ${remoteNodeId}, closing connection attempt"
        )
    }
}

/**
 * Class representing an event triggered when the initiator node is ready to create an offer
 * during the connection negotiation process.
 *
 * This event occurs when the initiator node has successfully passed the initial stages of the connection
 * negotiation and is now prepared to create an offer for the connection. The event triggers the creation
 * of the offer, and once the offer is created, it will be processed further.
 *
 * The initiator node is the one that starts the connection process, and this event signals that it is
 * ready to create a local description to send to the responder node.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is attempting to connect to.
 */
internal class OnInitiatorNodeReadyToCreateOffer(remoteNodeId: String) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.debugInfo("creating local description for local InitiatorNode $remoteNodeId")

        node.state = NodeState.DESCRIPTIONS_EXCHANGE

        node.peer.createOffer(
            RTCOfferOptions(),
            object : CreateSessionDescriptionObserver {

                override fun onSuccess(description: RTCSessionDescription?) {
                    EventLoop.postEvent(OnOfferCreatedSuccessfullyInitiatorNode(remoteNodeId, description))
                }

                override fun onFailure(failureErr: String?) {
                    EventLoop.postEvent(OnOfferCreatedFailureInitiatorNode(remoteNodeId, failureErr))
                }
            }
        )
    }
}

/**
 * Represents an event triggered when the initiator node successfully creates a local session description (offer)
 * during the connection negotiation process.
 *
 * This event is triggered after the initiator node successfully creates the offer to establish a peer-to-peer connection.
 * The offer is then set as the local description, and if this is successful, it proceeds to the next stage of the connection negotiation.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 * @param description The local session description (offer) created by the initiator node.
 */
internal class OnOfferCreatedSuccessfullyInitiatorNode(
    remoteNodeId: String, private val description: RTCSessionDescription?
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        if (description == null) {
            connectionNegotiationError(node, "local description is null after creation for local InitiatorNode")
        } else {
            logger.debugInfo("local description created for local InitiatorNode $remoteNodeId, setting it")
            node.peer.setLocalDescription(description, object : SetSessionDescriptionObserver {

                override fun onSuccess() {
                    EventLoop.postEvent(OnOfferSetSuccessfullyInitiatorNode(remoteNodeId, description))
                }

                override fun onFailure(failureErr: String?) {
                    EventLoop.postEvent(OnOfferSetFailureInitiatorNode(remoteNodeId, failureErr))
                }
            })
        }
    }
}

/**
 * Represents an event triggered when the initiator node fails to create a local session description (offer)
 * during the connection negotiation process.
 *
 * This event is triggered after the initiator node attempts to create an offer to establish a peer-to-peer connection,
 * but the creation fails. The failure can be due to various reasons, and this event is responsible for handling the error,
 * logging the failure, and marking the connection attempt as unsuccessful.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 * @param failureErr A string containing the error message describing the failure, or `null` if no error message is provided.
 */
internal class OnOfferCreatedFailureInitiatorNode(
    remoteNodeId: String, private val failureErr: String?
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        connectionNegotiationError(node, "error on creating local description for local InitiatorNode", failureErr)
    }
}

/**
 * Represents an event triggered when the initiator node successfully sets a local session description (offer)
 * during the connection negotiation process.
 *
 * This event occurs when the initiator node has successfully set its local session description, which is used to
 * initiate the connection with the remote responder node. The local description is then converted into an agnostic format
 * and sent to the remote responder node to proceed with the connection.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is attempting to connect to.
 * @param description The local session description (offer) that was successfully set by the initiator node.
 */
internal class OnOfferSetSuccessfullyInitiatorNode(
    remoteNodeId: String, private val description: RTCSessionDescription
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.debugInfo(
            "local description set for local InitiatorNode $remoteNodeId, converting it to agnostic format"
        )
        AgnosticRTCSessionDescription.adaptConcrete(description).ifPresentOrElse(
            { agnosticDescription ->
                logger.debugInfo("sending converted local description to remote ResponderNode $remoteNodeId")
                val msg = ParsableSessionDescriptionMsg()
                msg.sessionDescription = agnosticDescription
                msg.from = localNodeId
                msg.to = remoteNodeId
                msg.sessionId = node.sessionId
                node.sendSocketMsg(CONNECTION_ATTEMPT, msg) { ack: Boolean ->
                    if(!ack){
                        EventLoop.postEvent(OnConnectionAttemptNotDeliveredInitiatorNode(remoteNodeId))
                    }
                }
            },
            {
                connectionNegotiationError(
                    node, "error on converting local description to an agnostic offer for remote ResponderNode"
                )
            }
        )
    }

}

/**
 * Represents an event triggered when the initiator node fails to deliver a connection attempt message to the remote responder node.
 *
 * This event occurs when the initiator node attempts to send a connection attempt message to the remote responder node,
 * but the message is not delivered to the remote node, meaning that the remote node is not connected to the Broker.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 */
internal class OnConnectionAttemptNotDeliveredInitiatorNode(
    remoteNodeId: String
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.regularErr("$remoteNodeId is not connected to Broker")
        node.failedConnectionPeers[remoteNodeId] = P2PConnectionFailedReason.REMOTE_NODE_NOT_CONNECTED_TO_BROKER
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }
}

/**
 * Represents an event triggered when the initiator node receives a connection refusal message from the remote responder node.
 *
 * This event occurs when the initiator node attempts to establish a connection with a remote responder node,
 * but the responder node refuses the connection attempt. The event handles the refusal by logging the error,
 * marking the connection attempt as failed, and closing the connection.
 *
 * @param msg The connection refusal message received from the remote responder node.
 */
internal class OnConnectionAttemptRefusedInitiatorNode(
    msg: ConnectionRefusalMsg
) : InitiatorNodeOnSocketMsgAbstractEvent(msg.from, msg) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.regularErr("$remoteNodeId refused connection attempt")
        node.failedConnectionPeers[remoteNodeId] = P2PConnectionFailedReason.CONNECTION_REFUSED_BY_REMOTE_NODE
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }
}

/**
 * Represents an event triggered when the initiator node receives a message indicating that incoming connections
 * are not allowed on the remote responder node.
 *
 * This event occurs when the initiator node attempts to establish a connection with a remote responder node,
 * but the responder node indicates that incoming connections are not allowed. The event handles this situation
 * by logging the error, marking the connection attempt as failed, and closing the connection.
 *
 * @param msg The message indicating that incoming connections are not allowed on the remote responder node.
 */
internal class OnIncomingConnectionsNotAllowedInitiatorNode(
    msg: IncomingConnectionsNotAllowedMsg
) : InitiatorNodeOnSocketMsgAbstractEvent(msg.from, msg) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.regularErr("connections not allowed on remote Node $remoteNodeId")
        node.failedConnectionPeers[remoteNodeId] = P2PConnectionFailedReason.CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE
        node.forceClose(NodeState.NEGOTIATION_ERROR)
    }
}

/**
 * Represents an event triggered when the initiator node fails to set a local session description (offer)
 * during the connection negotiation process.
 *
 * This event occurs when the initiator node attempts to set its local session description but fails to do so.
 * The failure can be due to various reasons, and this event is responsible for handling the error, logging the failure,
 * and marking the connection attempt as unsuccessful.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 * @param failureErr A string containing the error message describing the failure, or `null` if no error message is provided.
 */
internal class OnOfferSetFailureInitiatorNode(
    remoteNodeId: String, private val failureErr: String?
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        connectionNegotiationError(node, "error on setting local description on local InitiatorNode", failureErr)
    }

}

/**
 * Represents an event triggered when the initiator node receives an incoming connection acceptance message
 * from the remote responder node.
 *
 * This event occurs when the initiator node receives a connection acceptance message from the remote responder node.
 * The message contains the session description sent by the responder node, which is then set as the remote description
 * on the initiator node. If the remote description is set successfully, the connection negotiation process proceeds.
 *
 * @param message The incoming connection acceptance message containing the session description from the responder node.
 */
internal class OnIncomingP2PConnectionAcceptanceMsg(
    private val message: SessionDescriptionMsg
) : InitiatorNodeOnSocketMsgAbstractEvent(message.from, message) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.debugInfo(
            "Received $CONNECTION_ACCEPTANCE socket msg from remote ResponderNode " +
                    "${msg.from}, setting remote description on its corresponding local InitiatorNode"
        )
        node.peer.setRemoteDescription(
            message.sessionDescription, object : SetSessionDescriptionObserver {

                override fun onSuccess() {
                    EventLoop.postEvent(OnRemoteDescriptionSetSuccessfullyInitiatorNode(message.from))
                }

                override fun onFailure(s: String?) {
                    EventLoop.postEvent(OnRemoteDescriptionSetFailureInitiatorNode(message.from, s))
                }

            }
        )
    }

}

/**
 * Represents an event triggered when the initiator node successfully sets a remote session description
 * during the connection negotiation process.
 *
 * This event occurs when the initiator node successfully sets the remote session description received from the
 * responder node. The remote description is then processed further, and if successful, the connection negotiation
 * process proceeds to the next stage of exchanging ICE candidates.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 */
internal class OnRemoteDescriptionSetSuccessfullyInitiatorNode(
    remoteNodeId: String
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.debugInfo("remote description set on local InitiatorNode $remoteNodeId")
        node.state = NodeState.ICE_CANDIDATES_EXCHANGE
        logger.debugInfo("start sending $ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER socket msgs to remote ResponderNode $remoteNodeId")
        node.suspendedOutgoingTeamDetailsMsgs.forEach{
            node.sendIceCandidatesExchangeMsg(ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER, it)
        }
        node.suspendedOutgoingTeamDetailsMsgs.clear()
        if(node.suspendedIncomingTeamDetailsMsgs.isNotEmpty()){
            logger.debugInfo("adding suspended incoming ice candidates from remote ResponderNode $remoteNodeId")
            node.suspendedIncomingTeamDetailsMsgs.forEach{
                node.peer.addIceCandidate(it.candidate)
            }
            node.suspendedIncomingTeamDetailsMsgs.clear()
        }
    }

}

/**
 * Represents an event triggered when the initiator node fails to set a remote session description
 * during the connection negotiation process.
 *
 * This event occurs when the initiator node attempts to set the remote session description received from the
 * responder node but fails to do so. The failure can be due to various reasons, and this event is responsible for
 * handling the error, logging the failure, and marking the connection attempt as unsuccessful.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 * @param failureErr A string containing the error message describing the failure, or `null` if no error message is provided.
 */
internal class OnRemoteDescriptionSetFailureInitiatorNode(
    remoteNodeId: String, private val failureErr: String?
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        connectionNegotiationError(node, "error on setting remote description on local InitiatorNode", failureErr)
    }

}

/**
 * Represents an event triggered when the initiator node is ready to send an ICE candidate to the remote responder node.
 *
 * This event occurs when the initiator node has an ICE candidate ready to be sent to the responder node.
 * If the initiator node is in the correct state to send the ICE candidate, the event triggers the sending of the ICE candidate message.
 * If the initiator node is still in the process of exchanging descriptions, the message is suspended until the descriptions are fully exchanged.
 *
 * @param remoteNodeId The ID of the remote responder node that the initiator node is trying to connect to.
 * @param msg The ICE candidate message to be sent to the responder node.
 */
internal class OnIceCandidateReadyToBeSentInitiatorNode(
    remoteNodeId: String, private val msg: ParsableIceCandidateMsg
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        when (node.state) {
            NodeState.ICE_CANDIDATES_EXCHANGE -> {
                node.sendIceCandidatesExchangeMsg(ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER, msg)
            }
            NodeState.DESCRIPTIONS_EXCHANGE -> {
                logger.debugInfo("a $ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER socket msg for remote NegotiatingResponderNode $remoteNodeId is ready to be sent, but the descriptions are still not finished to be exchanged, suspending it")
                node.suspendedOutgoingTeamDetailsMsgs.add(msg)
            }
            else -> {
                logger.debugInfo("a $ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER socket msg for remote NegotiatingResponderNode $remoteNodeId is ready to be sent, but the node is in the state ${node.state}, discarding it")
            }
        }
    }

}

/**
 * Represents an event triggered when the initiator node receives an incoming ICE candidate message from the remote responder node.
 *
 * This event occurs when the initiator node receives an ICE candidate message from the responder node.
 * If the initiator node is in the correct state to receive the ICE candidate, the event triggers the processing of the ICE candidate.
 * If the initiator node is still in the process of exchanging descriptions, the message is suspended until the descriptions are fully exchanged.
 *
 * @param message The incoming ICE candidate message from the responder node.
 */
internal class OnIncomingP2PIceCandidatesExchangeMsgInitiatorNode(
    private val message: IceCandidateMsg
) : InitiatorNodeOnSocketMsgAbstractEvent(message.from, message) {

    override fun onNodeFound(node: InitiatorNode) {
        when (node.state) {
            NodeState.ICE_CANDIDATES_EXCHANGE -> {
                logger.debugInfo("received $ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR socket msg from remote NegotiatingResponderNode $remoteNodeId, adding ice candidate to its corresponding local NegotiatingInitiatorNode")
                node.peer.addIceCandidate(message.candidate)
            }
            NodeState.DESCRIPTIONS_EXCHANGE -> {
                logger.debugInfo("received $ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR socket msg from remote NegotiatingResponderNode $remoteNodeId, suspending it until ${NodeState.ICE_CANDIDATES_EXCHANGE} is reached")
                node.suspendedIncomingTeamDetailsMsgs.add(message)
            }
            else -> {
                logger.debugInfo("received $ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR socket msg from remote NegotiatingResponderNode $remoteNodeId, state is ${node.state}, discarding it")
            }
        }
    }

}

/**
 * Represents an event triggered when the initiator node RTCPeerConnection's state changes.
 *
 * This event only checks for disconnection and failed, since the completion of successful connection is demanded to
 * the [OnDataChannelStateChangeInitiatorNode] event.
 * If the connection state changes to disconnected or failed, the initiator node is forced to close the connection.
 *
 * @param remoteNodeId The ID of the remote responder node.
 * @param state The new state of the RTCPeerConnection.
 */
internal class OnP2PConnectionStateChangeInitiatorNode(
    remoteNodeId: String, private val state: RTCPeerConnectionState
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.debugInfo("peer connection to initiator node $remoteNodeId state changed: $state")
        if(state == RTCPeerConnectionState.DISCONNECTED || state == RTCPeerConnectionState.FAILED){
            if(node.state == NodeState.DESCRIPTIONS_EXCHANGE || node.state == NodeState.ICE_CANDIDATES_EXCHANGE){
                connectionNegotiationError(
                    node, "peer connection to initiator node $remoteNodeId state changed to $state"
                )
            } else if(node.state == NodeState.CONNECTED){
                node.forceClose(NodeState.DISCONNECTED)
            }
        }
    }

}

/**
 * Represents an event triggered when the initiator node's data channel state changes.
 *
 * This event occurs when the data channel state changes for the initiator node. If the data channel state changes to
 * 'open' and the node is still negotiating, the connection is considered successful, and the initiator node is marked as connected.
 * If the data channel state changes to 'closed' while the node is negotiating, the connection negotiation is considered failed.
 * If the data channel state changes to 'closed' while the node is connected, the connection is considered closed.
 *
 * @param remoteNodeId The ID of the remote responder node.
 * @param state The new state of the data channel.
 */
internal class OnDataChannelStateChangeInitiatorNode(
    remoteNodeId: String, private val state: RTCDataChannelState
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        logger.debugInfo("data channel state changed to $state for node $remoteNodeId")
        if(state == RTCDataChannelState.OPEN && node.isNegotiating(node.state)){
            logger.debugInfo("data channel opened for InitiatorNode $remoteNodeId")
            node.state = NodeState.CONNECTED
            node.connectionTimeoutTimer.cancel()
            executeCallbackOnExecutor {
                node.asyncCrolangNodeCallbacks.onConnectionSuccess(node.crolangNode)
            }
            node.countdownMissingNodes()
        } else if(state == RTCDataChannelState.CLOSED && node.isNegotiating(node.state)){
            connectionNegotiationError(node, "data channel closed while negotiating for InitiatorNode $remoteNodeId")
        } else if(state == RTCDataChannelState.CLOSED && node.state == NodeState.CONNECTED){
            logger.debugErr("data channel closed while connected for InitiatorNode $remoteNodeId")
            node.forceClose(NodeState.DISCONNECTED)
        }
    }

}

/**
 * Represents an event triggered when the initiator node receives an incoming P2P message part from the remote responder node.
 *
 * This event occurs only when the nodes are fully connected, since P2P messages cannot be exchanged before establishing
 * a connection between them.
 * The incoming message is deposited in the initiator node's message queue for processing.
 *
 * @param remoteNodeId The ID of the remote responder node that sent the message.
 * @param msg The incoming message from the responder node.
 */
internal class OnIncomingP2PMsgPartInitiatorNode(
    remoteNodeId: String, private val msg: PeerMsgPart
) : InitiatorNodeAbstractEvent(remoteNodeId) {

    override fun onNodeFound(node: InitiatorNode) {
        node.depositNewMsgPart(msg)
    }

}
