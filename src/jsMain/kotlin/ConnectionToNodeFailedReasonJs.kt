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

import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException

/**
 * Enumeration of possible reasons why a P2P connection attempt failed.
 * 
 * This enum provides detailed information about connection failures, allowing you to:
 * - Implement appropriate error handling strategies
 * - Provide meaningful feedback to users
 * - Determine if a retry might succeed
 * 
 * Connection failures can occur due to network issues, authentication problems,
 * or policy restrictions on either the local or remote node.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ConnectionToNodeFailedReasonJs {
    /** The local node is not connected to the broker */
    LOCAL_NODE_NOT_CONNECTED_TO_BROKER,
    
    /** Attempted to connect to the same node (self-connection not allowed) */
    TRIED_TO_CONNECT_TO_SELF,
    
    /** Already have an active connection to the target node */
    ALREADY_CONNECTED_TO_REMOTE_NODE,
    
    /** User forcefully terminated the connection attempt */
    CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY,
    
    /** Connection attempt timed out */
    CONNECTION_TIMEOUT,
    
    /** The target node is not connected to the Broker */
    REMOTE_NODE_NOT_CONNECTED_TO_BROKER,
    
    /** WebRTC negotiation or technical error occurred */
    CONNECTION_NEGOTIATION_ERROR,
    
    /** The remote node rejected the connection attempt */
    CONNECTION_REFUSED_BY_REMOTE_NODE,
    
    /** The remote node has disabled incoming connections */
    CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE,
    
    /** Connection was rejected due to authorization failure by the Broker*/
    UNAUTHORIZED_CONNECTION,
    
    /** Connection feature is disabled */
    DISABLED;

    /**
     * Internal converter for mapping platform-specific exception types to JavaScript enum values.
     * 
     * This object provides conversion utilities between the multiplatform exception system
     * and the JavaScript-specific enum representation.
     */
    internal object Converter{
        /**
         * Converts a platform-specific connection failure exception to the JavaScript enum.
         * 
         * @param exception The platform-specific exception to convert
         * @return The corresponding JavaScript enum value
         */
        internal fun fromConnectionToNodeFailedReasonException(
            exception: ConnectionToNodeFailedReasonException
        ): ConnectionToNodeFailedReasonJs {
            return when (exception) {
                is ConnectionToNodeFailedReasonException.LocalNodeNotConnectedToBroker -> LOCAL_NODE_NOT_CONNECTED_TO_BROKER
                is ConnectionToNodeFailedReasonException.TriedToConnectToSelf -> TRIED_TO_CONNECT_TO_SELF
                is ConnectionToNodeFailedReasonException.AlreadyConnectedToRemoteNode -> ALREADY_CONNECTED_TO_REMOTE_NODE
                is ConnectionToNodeFailedReasonException.ConnectionAttemptClosedByUserForcefully -> CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY
                is ConnectionToNodeFailedReasonException.ConnectionTimeout -> CONNECTION_TIMEOUT
                is ConnectionToNodeFailedReasonException.RemoteNodeNotConnectedToBroker -> REMOTE_NODE_NOT_CONNECTED_TO_BROKER
                is ConnectionToNodeFailedReasonException.ConnectionNegotiationError -> CONNECTION_NEGOTIATION_ERROR
                is ConnectionToNodeFailedReasonException.ConnectionRefusedByRemoteNode -> CONNECTION_REFUSED_BY_REMOTE_NODE
                is ConnectionToNodeFailedReasonException.ConnectionsNotAllowedOnRemoteNode -> CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE
                is ConnectionToNodeFailedReasonException.UnauthorizedConnection -> UNAUTHORIZED_CONNECTION
                is ConnectionToNodeFailedReasonException.Disabled -> DISABLED
            }
        }
    }

}
