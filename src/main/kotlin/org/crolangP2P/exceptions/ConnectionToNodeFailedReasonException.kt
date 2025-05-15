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

package org.crolangP2P.exceptions

import org.crolangP2P.P2PConnectionFailedReason
import org.crolangP2P.IncomingCrolangNodesCallbacks

/**
 * Exception thrown when trying to connect to another Node.
 *
 * @see LocalNodeNotConnectedToBroker
 * @see TriedToConnectToSelf
 * @see AlreadyConnectedToRemoteNode
 * @see ConnectionAttemptClosedByUserForcefully
 * @see ConnectionTimeout
 * @see RemoteNodeNotConnectedToBroker
 * @see ConnectionNegotiationError
 * @see ConnectionRefusedByRemoteNode
 * @see ConnectionsNotAllowedOnRemoteNode
 */
sealed class ConnectionToNodeFailedReasonException(
    /**
     * The reason for the connection failure while trying to connect to another Node.
     */
    val reason: P2PConnectionFailedReason
) : Exception(reason.name) {

    /**
     * Exception thrown when the local Node is not connected to the Broker and therefore cannot connect to another Node.
     */
    data object LocalNodeNotConnectedToBroker : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.LOCAL_NODE_NOT_CONNECTED_TO_BROKER
    ) {
        private fun readResolve(): Any = LocalNodeNotConnectedToBroker
    }

    /**
     * Exception thrown when the local Node tries to connect to itself, referring its own id.
     */
    data object TriedToConnectToSelf : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.TRIED_TO_CONNECT_TO_SELF
    ) {
        private fun readResolve(): Any = TriedToConnectToSelf
    }

    /**
     * Exception thrown when the local Node is already connected to the remote Node.
     */
    data object AlreadyConnectedToRemoteNode : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.ALREADY_CONNECTED_TO_REMOTE_NODE
    ) {
        private fun readResolve(): Any = AlreadyConnectedToRemoteNode
    }

    /**
     * Exception thrown when the connection attempt to the remote Node is closed by the user forcefully via the
     * forceConclusion method.
     *
     * @see org.crolangP2P.ConnectionAttempt
     */
    data object ConnectionAttemptClosedByUserForcefully : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY
    ) {
        private fun readResolve(): Any = ConnectionAttemptClosedByUserForcefully
    }

    /**
     * Exception thrown when the connection attempt to the remote Node times out.
     *
     * @see org.crolangP2P.CrolangSettings
     */
    data object ConnectionTimeout : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.CONNECTION_TIMEOUT
    ) {
        private fun readResolve(): Any = ConnectionTimeout
    }

    /**
     * Exception thrown when the remote Node is not connected to the Broker and therefore cannot connect to another Node.
     */
    data object RemoteNodeNotConnectedToBroker : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.REMOTE_NODE_NOT_CONNECTED_TO_BROKER
    ) {
        private fun readResolve(): Any = RemoteNodeNotConnectedToBroker
    }

    /**
     * Exception thrown when the connection negotiation with the remote Node fails.
     */
    data object ConnectionNegotiationError : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.CONNECTION_NEGOTIATION_ERROR
    ) {
        private fun readResolve(): Any = ConnectionNegotiationError
    }

    /**
     * Exception thrown when the connection to the remote Node is refused via the onConnectionAttempt callback.
     *
     * @see IncomingCrolangNodesCallbacks
     */
    data object ConnectionRefusedByRemoteNode : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.CONNECTION_REFUSED_BY_REMOTE_NODE
    ) {
        private fun readResolve(): Any = ConnectionRefusedByRemoteNode
    }

    /**
     * Exception thrown when the remote Node does not allow incoming connections.
     */
    data object ConnectionsNotAllowedOnRemoteNode : ConnectionToNodeFailedReasonException(
        P2PConnectionFailedReason.CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE
    ) {
        private fun readResolve(): Any = ConnectionsNotAllowedOnRemoteNode
    }

}
