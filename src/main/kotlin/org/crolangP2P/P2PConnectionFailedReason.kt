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

package org.crolangP2P

import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException

/**
 * Enum class representing the reasons for connection failures to a remote Node.
 */
enum class P2PConnectionFailedReason {
    /**
     * The local Node is not connected to the Broker and therefore cannot connect to another Node.
     */
    LOCAL_NODE_NOT_CONNECTED_TO_BROKER,
    /**
     * The local Node tries to connect to itself, referring its own id.
     */
    TRIED_TO_CONNECT_TO_SELF,
    /**
     * The local Node is already connected to the remote Node.
     */
    ALREADY_CONNECTED_TO_REMOTE_NODE,
    /**
     * The connection attempt to the remote Node is closed by the user forcefully via the forceConclusion method.
     *
     * @see org.crolangP2P.ConnectionAttempt
     */
    CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY,
    /**
     * The connection attempt to the remote Node times out.
     *
     * @see org.crolangP2P.CrolangSettings
     */
    CONNECTION_TIMEOUT,
    /**
     * The remote Node is not connected to the Broker and therefore cannot connect to another Node.
     */
    REMOTE_NODE_NOT_CONNECTED_TO_BROKER,
    /**
     * The connection negotiation with the remote Node fails.
     */
    CONNECTION_NEGOTIATION_ERROR,
    /**
     * The connection to the remote Node is refused via the onConnectionAttempt callback.
     *
     * @see IncomingCrolangNodesCallbacks
     */
    CONNECTION_REFUSED_BY_REMOTE_NODE,
    /**
     * The remote Node does not allow incoming connections.
     */
    CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE;

    /**
     * Converts the current instance of [P2PConnectionFailedReason] to a [ConnectionToNodeFailedReasonException].
     *
     * @return The corresponding [ConnectionToNodeFailedReasonException] based on the current instance.
     */
    internal fun toConnectionToNodeFailedReasonException(): ConnectionToNodeFailedReasonException {
        return when (this) {
            LOCAL_NODE_NOT_CONNECTED_TO_BROKER -> ConnectionToNodeFailedReasonException.LocalNodeNotConnectedToBroker
            TRIED_TO_CONNECT_TO_SELF -> ConnectionToNodeFailedReasonException.TriedToConnectToSelf
            ALREADY_CONNECTED_TO_REMOTE_NODE -> ConnectionToNodeFailedReasonException.AlreadyConnectedToRemoteNode
            CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY -> ConnectionToNodeFailedReasonException.ConnectionAttemptClosedByUserForcefully
            CONNECTION_TIMEOUT -> ConnectionToNodeFailedReasonException.ConnectionTimeout
            REMOTE_NODE_NOT_CONNECTED_TO_BROKER -> ConnectionToNodeFailedReasonException.RemoteNodeNotConnectedToBroker
            CONNECTION_NEGOTIATION_ERROR -> ConnectionToNodeFailedReasonException.ConnectionNegotiationError
            CONNECTION_REFUSED_BY_REMOTE_NODE -> ConnectionToNodeFailedReasonException.ConnectionRefusedByRemoteNode
            CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE -> ConnectionToNodeFailedReasonException.ConnectionsNotAllowedOnRemoteNode
        }
    }
}
