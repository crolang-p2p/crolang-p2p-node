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

import internal.events.data.abstractions.SocketResponses

/**
 * Exception thrown when sending a message to a remote node via the Broker using WebSocket relay fails.
 *
 * This sealed class represents the possible reasons for failure when attempting to send a socket message between nodes.
 * Each subclass corresponds to a specific error scenario, such as:
 *
 * - The local node is not connected to the Broker ([NotConnectedToBroker])
 * - The message is being sent to self ([TriedToSendMsgToSelf])
 * - The remote node is not connected to the Broker ([RemoteNodeNotConnectedToBroker])
 * - The sender is not authorized to contact the remote node ([UnauthorizedToContactRemoteNode])
 * - The message channel or node ID is empty ([EmptyChannel], [EmptyId])
 * - An unknown error occurred ([UnknownError])
 *
 * Use [fromMessage] to convert a broker response string into the corresponding exception.
 */
sealed class SendSocketMsgException(message: String) : Exception(message) {

    /**
     * Thrown when the local node is not connected to the Broker and cannot send messages via WebSocket relay.
     */
    data object NotConnectedToBroker : SendSocketMsgException("NOT_CONNECTED_TO_BROKER") {
        private fun readResolve(): Any = NotConnectedToBroker
    }

    /**
     * Thrown when a node attempts to send a message to itself, which is not allowed.
     */
    data object TriedToSendMsgToSelf : SendSocketMsgException("TRIED_TO_SEND_MSG_TO_SELF") {
        private fun readResolve(): Any = TriedToSendMsgToSelf
    }

    /**
     * Thrown when the remote node is not connected to the Broker and cannot receive messages.
     */
    data object RemoteNodeNotConnectedToBroker : SendSocketMsgException("REMOTE_NODE_NOT_CONNECTED_TO_BROKER") {
        private fun readResolve(): Any = RemoteNodeNotConnectedToBroker
    }

    /**
     * Thrown when an unknown error occurs during the message sending process.
     */
    data object UnknownError : SendSocketMsgException("UNKNOWN_ERROR") {
        private fun readResolve(): Any = UnknownError
    }

    /**
     * Thrown when the sender is not authorized to contact the remote node.
     */
    data object UnauthorizedToContactRemoteNode : SendSocketMsgException("UNAUTHORIZED_TO_CONTACT_REMOTE_NODE") {
        private fun readResolve(): Any = UnauthorizedToContactRemoteNode
    }

    /**
     * Thrown when the node ID provided for the message is empty.
     */
    data object EmptyId : SendSocketMsgException("EMPTY_ID") {
        private fun readResolve(): Any = EmptyId
    }

    /**
     * Thrown when the channel provided for the message is empty.
     */
    data object EmptyChannel : SendSocketMsgException("EMPTY_CHANNEL") {
        private fun readResolve(): Any = EmptyChannel
    }

    /**
     * Thrown when the Broker has disabled WebSocket relay functionality due to configuration settings.
     */
    data object Disabled : SendSocketMsgException("DISABLED") {
        private fun readResolve(): Any = Disabled
    }

    internal companion object {
        fun fromMessage(msg: String): SendSocketMsgException = when (msg) {
            SocketResponses.UNAUTHORIZED -> UnauthorizedToContactRemoteNode
            SocketResponses.NOT_CONNECTED -> RemoteNodeNotConnectedToBroker
            SocketResponses.DISABLED -> Disabled
            else -> UnknownError
        }
    }


}
