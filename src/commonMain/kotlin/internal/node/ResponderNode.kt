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

import internal.events.OnConnectionAttemptTimeoutResponderNode
import internal.events.OnDataChannelStateChangeResponderNode
import internal.events.OnIceCandidateReadyToBeSentResponderNode
import internal.events.OnIncomingP2PMsgPartResponderNode
import internal.events.OnP2PConnectionStateChangeResponderNode
import internal.events.OnSocketMsgBrokerNegativeResponseResponderNode
import internal.events.data.abstractions.SocketMsgType.Companion.ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR
import internal.node.DataChannelUtility.onP2PMessageByteArray
import internal.utils.SharedStore.brokerPeersContainer
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.incomingCrolangNodesCallbacks
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelObserver
import internal.utils.SharedStore
import org.crolangP2P.IncomingCrolangNodesCallbacks
import org.crolangP2P.P2PConnectionFailedReason

/**
 * Represents a Responder node in a peer-to-peer WebRTC connection setup.
 *
 * A Responder node is a [CrolangNode] responsible for responding to the connection initiated by another [CrolangNode],
 * which will act as the Initiator. This class handles the connection, ICE candidate exchange, data channel setup,
 * and event-driven callbacks related to connection state changes, timeouts, and incoming messages.
 *
 * Two custom behaviours are provided to the common AbstractNode class:
 * - [newDataChannelRemotelyCreatedObserver]: An observer for the data channel created by another node
 * - [concreteNodeEventParameters]: Parameters for customizing the concrete behaviour of the ResponderNode,
 * providing mainly the events handlers
 *
 * @property incomingConnectionCallbacks A set of user-defined callbacks for handling Responder-related events such as
 * a new message available, on disconnection, etc...
 */
internal class ResponderNode(
    rtcConfiguration: CrolangP2PRTCConfiguration,
    remoteNodeId: String,
    sessionId: String,
    val incomingConnectionCallbacks: IncomingCrolangNodesCallbacks,
) : AbstractNode(
    rtcConfiguration,
    remoteNodeId,
    sessionId,
    incomingConnectionCallbacks,
    newDataChannelRemotelyCreatedObserver = {
        object : CrolangP2PRTCDataChannelObserver() {

            override fun onStateChange() {
                SharedStore.dependencies!!.eventLoop.postEvent(OnDataChannelStateChangeResponderNode(remoteNodeId, it, it.state()))
            }

            override fun onMessage(message: ByteArray) {
                onP2PMessageByteArray(remoteNodeId, message){ remoteNodeId, checkedMsgPart ->
                    OnIncomingP2PMsgPartResponderNode(remoteNodeId, checkedMsgPart)
                }
            }

        }
    },
    ConcreteNodeEventParameters(
        onConnectionAttemptTimeout = { OnConnectionAttemptTimeoutResponderNode(remoteNodeId) },
        onIceCandidateReadyToBeSent = { OnIceCandidateReadyToBeSentResponderNode(remoteNodeId, it) },
        onConnectionStateChange = { OnP2PConnectionStateChangeResponderNode(remoteNodeId, it) },
        onP2PIceCandidatesExchangeMsgBrokerNegativeResponseReceived = { OnSocketMsgBrokerNegativeResponseResponderNode(
            remoteNodeId, ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR
        ) },
        nodesContainer = brokerPeersContainer.responderNodes,
        onNegotiationClosure = {
            executeCallbackOnExecutor {
                incomingCrolangNodesCallbacks!!
                    .onConnectionFailed(
                        remoteNodeId,
                        P2PConnectionFailedReason.CONNECTION_NEGOTIATION_ERROR.toConnectionToNodeFailedReasonException()
                    )
            }
        },
        onConnectedClosure = {
            executeCallbackOnExecutor { incomingConnectionCallbacks.onDisconnection(remoteNodeId) }
        }
    )
)
