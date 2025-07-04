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

import internal.events.OnConnectionAttemptTimeoutInitiatorNode
import internal.events.OnDataChannelStateChangeInitiatorNode
import internal.events.OnIceCandidateReadyToBeSentInitiatorNode
import internal.events.OnIncomingP2PMsgPartInitiatorNode
import internal.events.OnP2PConnectionStateChangeInitiatorNode
import internal.events.OnSocketMsgBrokerNegativeResponseInitiatorNode
import internal.events.data.abstractions.SocketMsgType.Companion.ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER
import internal.node.DataChannelUtility.onP2PMessageByteArray
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.brokerPeersContainer
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelObserver
import internal.utils.SharedStore
import org.crolangP2P.AsyncCrolangNodeCallbacks
import org.crolangP2P.P2PConnectionFailedReason

/**
 * Represents an Initiator node in a peer-to-peer WebRTC connection setup.
 *
 * An Initiator node is a [CrolangNode] responsible for initiating the connection towards another [CrolangNode],
 * which will act as the Responder. This class handles the creation and management of the connection,
 * ICE candidate exchange, data channel setup, and event-driven callbacks related to connection state changes,
 * timeouts, and incoming messages.
 *
 * Two custom behaviours are provided to the common AbstractNode class:
 * - [newDataChannelRemotelyCreatedObserver]: An observer for the data channel created by another node
 * - [concreteNodeEventParameters]: Parameters for customizing the concrete behaviour of the InitiatorNode,
 * providing mainly the events handlers
 *
 * @property asyncCrolangNodeCallbacks A set of callbacks for handling connection-related events such as
 * successful connection, connection failure, and disconnection.
 * @property failedConnectionPeers A map storing peers that failed to establish a connection, with the reason for failure.
 * @property countdownMissingNodes A function that triggers the countdown for missing nodes in the sync connection process.
 */
internal class InitiatorNode(
    rtcConfiguration: CrolangP2PRTCConfiguration,
    remoteNodeId: String,
    sessionId: String,
    val asyncCrolangNodeCallbacks: AsyncCrolangNodeCallbacks,
    private val connectionAttemptInitiators: MutableMap<String, InitiatorNode>,
    val failedConnectionPeers: MutableMap<String, P2PConnectionFailedReason>,
    val countdownMissingNodes: () -> Unit
) : AbstractNode(
    rtcConfiguration,
    remoteNodeId,
    sessionId,
    asyncCrolangNodeCallbacks,
    newDataChannelRemotelyCreatedObserver = {
        /**
         * Since the Initiator node is the one creating the data channel, the observer for the data channel created
         * by another node is empty.
         */
        object : CrolangP2PRTCDataChannelObserver() {

            override fun onStateChange() {}

            override fun onMessage(message: ByteArray) {}

        }
    },
    concreteNodeEventParameters = ConcreteNodeEventParameters(
        onConnectionAttemptTimeout = { OnConnectionAttemptTimeoutInitiatorNode(remoteNodeId) },
        onIceCandidateReadyToBeSent = { OnIceCandidateReadyToBeSentInitiatorNode(remoteNodeId, it) },
        onConnectionStateChange = { OnP2PConnectionStateChangeInitiatorNode(remoteNodeId, it) },
        onP2PIceCandidatesExchangeMsgBrokerNegativeResponseReceived = { OnSocketMsgBrokerNegativeResponseInitiatorNode(
            remoteNodeId, ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER
        ) },
        nodesContainer = brokerPeersContainer.initiatorNodes,
        onNegotiationClosure = {
            if(failedConnectionPeers[remoteNodeId] == null){
                failedConnectionPeers[remoteNodeId] = P2PConnectionFailedReason.CONNECTION_NEGOTIATION_ERROR
            }
            connectionAttemptInitiators.remove(remoteNodeId)
            executeCallbackOnExecutor { asyncCrolangNodeCallbacks.onConnectionFailed(
                remoteNodeId, failedConnectionPeers[remoteNodeId]!!.toConnectionToNodeFailedReasonException()
            ) }
            countdownMissingNodes()
        },
        onConnectedClosure = {
            executeCallbackOnExecutor { asyncCrolangNodeCallbacks.onDisconnection(remoteNodeId) }
        }
    )
) {

    init {
        // Creates and registers a data channel for communication with the remote node, associating the Initiator events
        val createdDataChannel = peer.createDataChannel()
        createdDataChannel.registerObserver(object : CrolangP2PRTCDataChannelObserver() {

            override fun onStateChange() {
                SharedStore.dependencies!!.eventLoop.postEvent(OnDataChannelStateChangeInitiatorNode(remoteNodeId, createdDataChannel.state()))
            }

            override fun onMessage(message: ByteArray) {
                onP2PMessageByteArray(remoteNodeId, message){
                        remNodeId, checkedMsgPart -> OnIncomingP2PMsgPartInitiatorNode(remNodeId, checkedMsgPart)
                }
            }
        })
        dataChannel = createdDataChannel
    }

}
