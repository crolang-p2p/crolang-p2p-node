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

package internal.events.data

import internal.events.data.abstractions.DirectMsg
import internal.events.data.abstractions.ParsableDirectMsg
import java.util.Optional

/**
 * This message is used to inform the initiator Node that incoming connections are not allowed on the responder.
 * This is received when the responder did not call CrolangP2P.allowIncomingConnections().
 *
 * @param from The ID of the sender (CrolangNode responder).
 * @param to The ID of the receiver (CrolangNode initiator).
 * @param sessionId The ID of the session.
 */
internal class IncomingConnectionsNotAllowedMsg(
    from: String,
    to: String,
    sessionId: String
): DirectMsg(from, to, sessionId)

/**
 * This class is used to parse the JSON payload of an incoming connections not allowed message and convert it into a concrete
 * `IncomingConnectionsNotAllowedMsg` object.
 *
 * @property from The ID of the sender (CrolangNode responder).
 * @property to The ID of the receiver (CrolangNode initiator).
 * @property sessionId The ID of the session.
 */
internal class ParsableIncomingConnectionsNotAllowedMsg: ParsableDirectMsg<IncomingConnectionsNotAllowedMsg>() {

    override fun toChecked(): Optional<IncomingConnectionsNotAllowedMsg> {
        return if(from == null || to == null || sessionId == null) {
            Optional.empty()
        } else {
            Optional.of(IncomingConnectionsNotAllowedMsg(from!!, to!!, sessionId!!))
        }
    }

}
