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

import internal.events.data.ParsableIceCandidateMsg
import internal.dependencies.event_loop.Event
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState

/**
 * Represents the parameters for customizing the concrete behaviour of [InitiatorNode] and [ResponderNode].
 *
 * This class contains various event handlers and containers used to manage
 * WebRTC node connections, such as handling ICE candidates, connection state changes,
 * and broker responses.
 *
 * @property onConnectionAttemptTimeout A function that returns an event triggered
 * when a connection attempt times out.
 * @property onIceCandidateReadyToBeSent A function that takes a `ParsableIceCandidateMsg`
 * and returns an event triggered when an ICE candidate is ready to be sent.
 * @property onConnectionStateChange A function that takes an `RTCPeerConnectionState`
 * and returns an event triggered when the connection state changes.
 * @property onP2PIceCandidatesExchangeMsgBrokerNegativeResponseReceived A function that
 * returns an event triggered when a negative response is received from the Broker
 * during a P2P ICE candidates exchange.
 * @property nodesContainer A mutable map that holds the nodes, where the key is a
 * `String` representing the node identifier, and the value is an `AbstractNode`.
 * @property onNegotiationClosure A function triggered when a negotiation is closed.
 * @property onConnectedClosure A function triggered when the connection is closed after it was successfully established.
 */
internal class ConcreteNodeEventParameters(
    val onConnectionAttemptTimeout: () -> Event,
    val onIceCandidateReadyToBeSent: (ParsableIceCandidateMsg) -> Event,
    val onConnectionStateChange: (CrolangP2PRTCPeerConnectionState) -> Event,
    val onP2PIceCandidatesExchangeMsgBrokerNegativeResponseReceived: () -> Event,
    val nodesContainer: MutableMap<String, out AbstractNode>,
    val onNegotiationClosure: () -> Unit,
    val onConnectedClosure: () -> Unit
)
