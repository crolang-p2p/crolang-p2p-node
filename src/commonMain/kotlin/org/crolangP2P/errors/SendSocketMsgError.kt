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

package org.crolangP2P.errors

import internal.events.data.abstractions.SocketResponses

/**
 * This enum represents the possible reasons for failure when attempting to send a socket message between nodes.
 */
enum class SendSocketMsgError {

    /**
     * The local node is not connected to the Broker and cannot send messages via WebSocket relay.
     */
    NOT_CONNECTED_TO_BROKER,
    /**
     * When a node attempts to send a message to itself, which is not allowed.
     */
    TRIED_TO_SEND_MSG_TO_SELF,
    /**
     * The remote node is not connected to the Broker and cannot receive messages.
     */
    REMOTE_NODE_NOT_CONNECTED_TO_BROKER,
    /**
     * Unknown error occurred during the message sending process.
     */
    UNKNOWN_ERROR,
    /**
     * The sender is not authorized to contact the remote node.
     */
    UNAUTHORIZED_TO_CONTACT_REMOTE_NODE,
    /**
     * The node ID provided for the message is empty.
     */
    EMPTY_ID,
    /**
     * The channel provided for the message is empty.
     */
    EMPTY_CHANNEL,
    /**
     * The Broker has disabled WebSocket relay functionality due to configuration settings.
     */
    DISABLED;

    internal companion object {
        fun fromMessage(msg: String): SendSocketMsgError = when (msg) {
            SocketResponses.UNAUTHORIZED -> UNAUTHORIZED_TO_CONTACT_REMOTE_NODE
            SocketResponses.NOT_CONNECTED -> REMOTE_NODE_NOT_CONNECTED_TO_BROKER
            SocketResponses.DISABLED -> DISABLED
            else -> UNKNOWN_ERROR
        }
    }


}
