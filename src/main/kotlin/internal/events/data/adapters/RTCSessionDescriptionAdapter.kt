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

import com.google.gson.annotations.SerializedName
import dev.onvoid.webrtc.RTCSdpType
import dev.onvoid.webrtc.RTCSessionDescription
import java.util.Optional

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
internal class AgnosticRTCSessionDescription {

    /**
     * The type of session description (either 'offer' or 'answer').
     */
    @SerializedName("type") var type: String? = null

    /**
     * The SDP (Session Description Protocol) string, which contains the session's media information.
     */
    @SerializedName("sdp") var sdp: String? = null

    /**
     * Converts the agnostic RTC session description to a concrete RTCSessionDescription object.
     * This is used when processing the data in a platform-specific way, using the actual WebRTC library.
     *
     * @return An Optional containing a concrete RTCSessionDescription if the conversion is successful, otherwise empty.
     */
    fun toConcrete(): Optional<RTCSessionDescription> {
        if (sdp == null) {
            return Optional.empty()
        }
        return when (type) {
            AGNOSTIC_OFFER -> Optional.of(RTCSessionDescription(RTCSdpType.OFFER, sdp))
            AGNOSTIC_ANSWER -> Optional.of(RTCSessionDescription(RTCSdpType.ANSWER, sdp))
            else -> Optional.empty()
        }
    }

    companion object {

        /**
         * Converts a concrete RTCSessionDescription into an agnostic version.
         * This method is used to convert a WebRTC library-specific session description to a format
         * that is independent of the specific library, allowing for cross-platform communication.
         *
         * @param rtcSessionDescription The concrete RTCSessionDescription to convert.
         * @return An Optional containing the agnostic RTCSessionDescription if the conversion is successful, otherwise empty.
         */
        fun adaptConcrete(
            rtcSessionDescription: RTCSessionDescription
        ): Optional<AgnosticRTCSessionDescription> {
            val agnostic = AgnosticRTCSessionDescription()
            agnostic.sdp = rtcSessionDescription.sdp
            when (rtcSessionDescription.sdpType) {
                RTCSdpType.OFFER -> agnostic.type = AGNOSTIC_OFFER
                RTCSdpType.ANSWER -> agnostic.type = AGNOSTIC_ANSWER
                else -> return Optional.empty()
            }
            return Optional.of(agnostic)
        }
    }
}
