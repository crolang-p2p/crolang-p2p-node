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

import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import internal.events.data.abstractions.DirectMsg
import internal.events.data.abstractions.ParsableMsg
import internal.events.data.adapters.IceCandidateAdapter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a message containing an ICE candidate sent between nodes.
 *
 * This message is used to exchange ICE candidates between peers. It holds the `from` and `to` fields to identify
 * the sender and receiver of the message, as well as the `candidate` field which contains the actual ICE candidate data.
 *
 * @param platformFrom The platform from which the message was sent.
 * @param versionFrom The version of the platform from which the message was sent.
 * @param from The ID of the sender (CrolangNode initiator).
 * @param to The ID of the receiver (CrolangNode responder).
 * @param sessionId The ID of the session.
 * @param candidate The ICE candidate to be exchanged.
 */
internal class IceCandidateMsg(
    platformFrom: String,
    versionFrom: String,
    from: String,
    to: String,
    sessionId: String,
    val candidate: CrolangP2PIceCandidate
): DirectMsg(platformFrom, versionFrom, from, to, sessionId)

/**
 * Represents a message containing an ICE candidate in an agnostic format, capable of being parsed into a concrete ICE candidate message.
 *
 * This class parses the JSON payload of an ICE candidate message and converts it into a concrete `IceCandidateMsg` object
 * containing the necessary ICE candidate data. The conversion involves transforming the data from an agnostic representation
 * (independent of WebRTC libraries) into a concrete `RTCIceCandidate` object that can be used by the system.
 *
 * @property candidate The ICE candidate data in an agnostic format.
 */
@Serializable
internal class ParsableIceCandidateMsg: ParsableMsg<IceCandidateMsg>() {

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

    /**
     * The ICE candidate data in an agnostic format.
     * This field is populated during parsing and must be converted to a concrete type before being used.
     */
    @SerialName("candidate") var candidate: IceCandidateAdapter? = null

    /**
     * Converts the parsed, agnostic ICE candidate message into a concrete `IceCandidateMsg`.
     *
     * This method validates the candidate data and transforms it from the agnostic format (represented by `IceCandidateAdapter`)
     * to the concrete `RTCIceCandidate` format, which is used by the system. If the data is valid, it returns the
     * `IceCandidateMsg` object, otherwise, it returns null.
     *
     * **Steps performed by this method:**
     * 1. It checks if the necessary fields (`candidate`, `from`, and `to`) are not null.
     * 2. If valid, it converts the `IceCandidateAdapter` to a concrete `RTCIceCandidate`.
     * 3. Returns an the `IceCandidateMsg` if the conversion is successful
     * 4. Returns null if any data is invalid or if the conversion fails.
     *
     * @return The concrete `IceCandidateMsg` if valid, or null if the conversion failed.
     */
    override fun toChecked(): IceCandidateMsg? {
        if(platformFrom == null || versionFrom == null || candidate == null || from == null || to == null || sessionId == null) {
            return null
        }
        val concreteCandidate = candidate!!.toConcrete() ?: return null
        return IceCandidateMsg(platformFrom!!, versionFrom !!, from!!, to!!, sessionId!!, concreteCandidate)
    }
}
