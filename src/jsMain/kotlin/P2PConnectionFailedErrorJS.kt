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

import org.crolangP2P.errors.P2PConnectionFailedError
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Enum class representing the reasons for connection failures to a remote Node.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class P2PConnectionFailedErrorJS {
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
     */
    CONNECTION_REFUSED_BY_REMOTE_NODE,
    /**
     * The remote Node does not allow incoming connections.
     */
    CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE,
    /**
     * The connection attempt was not authorized by the broker.
     */
    UNAUTHORIZED_CONNECTION,
    /**
     * P2P connections are disabled in the Broker configuration.
     */
    DISABLED;

    internal companion object {
        fun fromInternal(value: P2PConnectionFailedError): P2PConnectionFailedErrorJS {
            return when (value) {
                P2PConnectionFailedError.LOCAL_NODE_NOT_CONNECTED_TO_BROKER -> LOCAL_NODE_NOT_CONNECTED_TO_BROKER
                P2PConnectionFailedError.TRIED_TO_CONNECT_TO_SELF -> TRIED_TO_CONNECT_TO_SELF
                P2PConnectionFailedError.ALREADY_CONNECTED_TO_REMOTE_NODE -> ALREADY_CONNECTED_TO_REMOTE_NODE
                P2PConnectionFailedError.CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY -> CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY
                P2PConnectionFailedError.CONNECTION_TIMEOUT -> CONNECTION_TIMEOUT
                P2PConnectionFailedError.REMOTE_NODE_NOT_CONNECTED_TO_BROKER -> REMOTE_NODE_NOT_CONNECTED_TO_BROKER
                P2PConnectionFailedError.CONNECTION_NEGOTIATION_ERROR -> CONNECTION_NEGOTIATION_ERROR
                P2PConnectionFailedError.CONNECTION_REFUSED_BY_REMOTE_NODE -> CONNECTION_REFUSED_BY_REMOTE_NODE
                P2PConnectionFailedError.CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE -> CONNECTIONS_NOT_ALLOWED_ON_REMOTE_NODE
                P2PConnectionFailedError.UNAUTHORIZED_CONNECTION -> UNAUTHORIZED_CONNECTION
                P2PConnectionFailedError.DISABLED -> DISABLED
            }
        }
    }
}
