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
import internal.events.data.abstractions.ParsableMsg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 */
@Serializable
internal class ParsableConnectionRefusalMsg: ParsableMsg<ConnectionRefusalMsg>() {

    /**
     * The platform from which the message was sent.
     */
    @SerialName("platformFrom") var platformFrom: String? = null

    /**
     * The version of the platform from which the message was sent.
     */
    @SerialName("versionFrom") var versionFrom: String? = null

    /**
     * The ID of the sender.
     */
    @SerialName("from") var from: String? = null

    /**
     * The ID of the recipient.
     */
    @SerialName("to") var to: String? = null

    /**
     * The ID of the session associated with the message.
     */
    @SerialName("sessionId") var sessionId: String? = null

    override fun toChecked(): ConnectionRefusalMsg? {
        return if(platformFrom == null || versionFrom == null || from == null || to == null || sessionId == null) {
            null
        } else {
            ConnectionRefusalMsg(platformFrom!!, versionFrom!!, from!!, to!!, sessionId!!)
        }
    }

}
