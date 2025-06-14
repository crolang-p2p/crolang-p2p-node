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
import java.util.*

/**
 * This message is used to inform the initiator Node that their connection attempt was rejected by the responder.
 * This is received when the connection is refused by the user-defined onConnectionAttempt callback.
 *
 * @param platformFrom The platform from which the message was sent.
 * @param versionFrom The version of the platform from which the message was sent.
 * @param from The ID of the sender (CrolangNode responder).
 * @param to The ID of the receiver (CrolangNode initiator).
 * @param sessionId The ID of the session.
 */
internal class ConnectionRefusalMsg(
    platformFrom: String,
    versionFrom: String,
    from: String,
    to: String,
    sessionId: String
): DirectMsg(platformFrom, versionFrom, from, to, sessionId)

/**
 * This class is used to parse the JSON payload of a connection refusal message and convert it into a concrete
 * `ConnectionRefusalMsg` object.
 *
 * @property platformFrom The platform from which the message was sent.
 * @property versionFrom The version of the platform from which the message was sent.
 * @property from The ID of the sender (CrolangNode responder).
 * @property to The ID of the receiver (CrolangNode initiator).
 * @property sessionId The ID of the session.
 */
internal class ParsableConnectionRefusalMsg: ParsableDirectMsg<ConnectionRefusalMsg>() {

    override fun toChecked(): Optional<ConnectionRefusalMsg> {
        return if(platformFrom == null || versionFrom == null || from == null || to == null || sessionId == null) {
            Optional.empty()
        } else {
            Optional.of(ConnectionRefusalMsg(platformFrom!!, versionFrom!!, from!!, to!!, sessionId!!))
        }
    }

}
