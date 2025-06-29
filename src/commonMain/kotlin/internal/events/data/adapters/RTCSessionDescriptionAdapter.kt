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

package internal.events.data.adapters

import internal.dependencies.webrtc.concrete.CrolangP2PRTCSdpType
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Constant representing the 'offer' type in the agnostic RTC session description.
 */
internal const val AGNOSTIC_OFFER = "offer"

/**
 * Constant representing the 'answer' type in the agnostic RTC session description.
 */
internal const val AGNOSTIC_ANSWER = "answer"

/**
 * A class representing an agnostic version of the RTCSessionDescription.
 * This format is used to ensure compatibility between different programming languages.
 * It provides a common structure for exchanging WebRTC session descriptions (offers and answers)
 * across various platforms, independent of the libraries used in each language.
 */
@Serializable
internal class AgnosticRTCSessionDescription {

    /**
     * The type of session description (either 'offer' or 'answer').
     */
    @SerialName("type") var type: String? = null

    /**
     * The SDP (Session Description Protocol) string, which contains the session's media information.
     */
    @SerialName("sdp") var sdp: String? = null

    /**
     * Converts the agnostic RTC session description to a concrete CrolangP2PRTCSessionDescription object.
     * This is used when processing the data in a platform-specific way, using the actual WebRTC library.
     *
     * @return An nullable containing a concrete CrolangP2PRTCSessionDescription if the conversion is successful, null otherwise.
     */
    fun toConcrete(): CrolangP2PRTCSessionDescription? {
        if (sdp == null) {
            return null
        }
        return when (type) {
            AGNOSTIC_OFFER -> CrolangP2PRTCSessionDescription(CrolangP2PRTCSdpType.OFFER, sdp)
            AGNOSTIC_ANSWER -> CrolangP2PRTCSessionDescription(CrolangP2PRTCSdpType.ANSWER, sdp)
            else -> null
        }
    }

    companion object {

        /**
         * Converts a concrete CrolangP2PRTCSessionDescription into an agnostic version.
         * This method is used to convert a WebRTC library-specific session description to a format
         * that is independent of the specific library, allowing for cross-platform communication.
         *
         * @param rtcSessionDescription The concrete CrolangP2PRTCSessionDescription to convert.
         * @return A nullable containing the agnostic RTCSessionDescription if the conversion is successful, null otherwise.
         */
        fun adaptConcrete(
            rtcSessionDescription: CrolangP2PRTCSessionDescription
        ): AgnosticRTCSessionDescription? {
            val agnostic = AgnosticRTCSessionDescription()
            agnostic.sdp = rtcSessionDescription.sdp
            when (rtcSessionDescription.sdpType) {
                CrolangP2PRTCSdpType.OFFER -> agnostic.type = AGNOSTIC_OFFER
                CrolangP2PRTCSdpType.ANSWER -> agnostic.type = AGNOSTIC_ANSWER
                else -> return null
            }
            return agnostic
        }
    }
}
