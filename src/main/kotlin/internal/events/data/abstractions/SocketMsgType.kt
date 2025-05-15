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

package internal.events.data.abstractions

/**
 * This class contains the types of messages that can be exchanged between a [CrolangNode] and the broker (socket server).
 * Each message type serves a specific purpose in the connection and configuration process between nodes.
 */
internal class SocketMsgType {
    companion object {

        /**
         * Message type sent when the [CrolangNode] successfully connects to the broker.
         * This message also includes the RTC configuration needed to connect to STUN/TURN servers
         * for establishing a connection with other nodes.
         */
        const val AUTHENTICATED = "AUTHENTICATED"

        /**
         * Message type sent by a [CrolangNode] to the broker to check whether specific nodes
         * are currently connected to the Broker. The message includes a list of node IDs,
         * and the response provides their current connection status.
         */
        const val ARE_NODES_CONNECTED_TO_BROKER = "ARE_NODES_CONNECTED_TO_BROKER"

        /**
         * Message type sent from a [CrolangNode] initiator to a [CrolangNode] receiver
         * to start the connection process. This message includes the offer for the WebRTC negotiation.
         */
        const val CONNECTION_ATTEMPT = "CONNECTION_ATTEMPT"

        /**
         * Message type sent from a [CrolangNode] receiver to the [CrolangNode] initiator
         * to accept the connection. This message includes the answer for the WebRTC negotiation.
         */
        const val CONNECTION_ACCEPTANCE = "CONNECTION_ACCEPTANCE"

        /**
         * Message type sent by a [CrolangNode] receiver when it refuses an incoming connection
         * attempt from another node.
         */
        const val CONNECTION_REFUSAL = "CONNECTION_REFUSAL"

        /**
         * Message type sent by the broker to inform a [CrolangNode] that the targeted node
         * does not accept incoming connections.
         */
        const val INCOMING_CONNECTIONS_NOT_ALLOWED = "INCOMING_CONNECTIONS_NOT_ALLOWED"

        /**
         * Message type used by the [CrolangNode] initiator to send ICE candidates
         * to the [CrolangNode] responder. This is part of the exchange needed to establish the P2P connection.
         */
        const val ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER = "ICE_CANDIDATES_EXCHANGE_INITIATOR_TO_RESPONDER"

        /**
         * Message type used by the [CrolangNode] responder to send ICE candidates
         * to the [CrolangNode] initiator. This is part of the exchange needed to establish the P2P connection.
         */
        const val ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR = "ICE_CANDIDATES_EXCHANGE_RESPONDER_TO_INITIATOR"

    }
}
