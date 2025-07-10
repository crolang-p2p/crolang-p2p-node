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

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ConnectionToNodeFailedReasonJs {
    LOCAL_NODE_NOT_CONNECTED_TO_BROKER,
    TRIED_TO_CONNECT_TO_SELF,
    ALREADY_CONNECTED_TO_REMOTE_NODE,
    CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY,
    CONNECTION_TIMEOUT,
    REMOTE_NODE_NOT_CONNECTED_TO_BROKER,
    CONNECTION_NEGOTIATION_ERROR,
    CONNECTION_REFUSED_BY_REMOTE_NODE,
    CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE,
    UNAUTHORIZED_CONNECTION,
    DISABLED;

    internal object Converter{
        fun fromConnectionToNodeFailedReasonException(
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
