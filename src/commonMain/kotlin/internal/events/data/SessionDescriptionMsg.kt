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

import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription
import internal.events.data.abstractions.DirectMsg
import internal.events.data.abstractions.ParsableMsg
import internal.events.data.adapters.AgnosticRTCSessionDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The [SessionDescriptionMsg] class represents a message containing a WebRTC session description,
 * which includes the specific content required to establish a P2P connection between peers.
 * This message is used to facilitate WebRTC session setup across different programming languages by using agnostic data structures.
 *
 * @property platformFrom Indicates the platform from which the message was sent.
 * @property versionFrom Indicates the version of the platform from which the message was sent.
 * @property from Indicates the sender of the message.
 * @property to Indicates the recipient of the message.
 * @property sessionId Identifies the session associated with this message.
 * @property sessionDescription The WebRTC session description, including details such as the offer or response
 * of the WebRTC session, which is agnostic to the programming language to enable interoperability.
 */
internal class SessionDescriptionMsg(
    platformFrom: String,
    versionFrom: String,
    from: String,
    to: String,
    sessionId: String,
    val sessionDescription: CrolangP2PRTCSessionDescription
): DirectMsg(platformFrom, versionFrom, from, to, sessionId)

/**
 * The [ParsableSessionDescriptionMsg] class extends [ParsableMsg] and provides functionality for deserializing
 * a session description message. It is used to convert the received agnostic data into a concrete form
 * (i.e., [SessionDescriptionMsg]), ensuring that the necessary data can be understood and processed by different peers
 * running in different programming environments.
 *
 * @property sessionDescription The session description, but in a more agnostic form to facilitate deserialization
 * and ensure compatibility across various programming languages.
 */
@Serializable
internal open class ParsableSessionDescriptionMsg: ParsableMsg<SessionDescriptionMsg>() {

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

    @SerialName("sessionDescription") var sessionDescription: AgnosticRTCSessionDescription? = null

    /**
     * Converts the deserialized message into a concrete [SessionDescriptionMsg] object.
     * This method returns a nullable to handle the case where the necessary data is not present
     * or the session description cannot be correctly converted into a form that can be understood by the peer.
     *
     * @return An nullable containing the session description message, if the data is complete and valid.
     *         If necessary data is missing or the session description is invalid, null is returned.
     */
    override fun toChecked(): SessionDescriptionMsg? {
        if(platformFrom == null || versionFrom == null || from == null || to == null || sessionDescription == null || sessionId == null){
            return null
        }

        val rtcSessionDescription = sessionDescription!!.toConcrete()
        return if(rtcSessionDescription == null) {
            null
        } else {
            SessionDescriptionMsg(platformFrom!!, versionFrom!!, from!!, to!!, sessionId!!, rtcSessionDescription)
        }
    }

}
