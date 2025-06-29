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

import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A class that represents an agnostic version of an ICE candidate.
 * This format is used to ensure compatibility across different platforms and programming languages
 * when exchanging ICE candidates in WebRTC.
 */
@Serializable
internal class IceCandidateAdapter {

    /**
     * The SDP string associated with the ICE candidate.
     */
    @SerialName("sdp") var sdp: String? = null

    /**
     * The SDP MID (Media Identification) associated with the ICE candidate.
     */
    @SerialName("sdpMid") var sdpMid: String? = null

    /**
     * The SDP MLine index associated with the ICE candidate.
     */
    @SerialName("sdpMLineIndex") var sdpMLineIndex: Int? = null

    /**
     * The URL of the server that is providing the ICE candidate.
     */
    @SerialName("serverUrl") var serverUrl: String? = null

    /**
     * Converts the agnostic ICE candidate representation to a concrete RTCIceCandidate object.
     * This method is used to convert the agnostic format to a platform-specific WebRTC representation
     * that can be processed by a WebRTC library.
     *
     * @return An nullable containing the RTCIceCandidate if conversion is successful, null otherwise.
     */
    fun toConcrete(): CrolangP2PIceCandidate? {
        if (sdp == null || sdpMid == null || sdpMLineIndex == null) {
            return null
        }
        return if(serverUrl == null){
            CrolangP2PIceCandidate(sdp!!, sdpMid!!, sdpMLineIndex!!)
        } else {
            CrolangP2PIceCandidate(sdp!!, sdpMid!!, sdpMLineIndex!!, serverUrl)
        }
    }

    companion object {

        /**
         * Converts a concrete RTCIceCandidate to an agnostic version.
         * This method is used to convert a WebRTC library-specific ICE candidate to a format
         * that is independent of the library, allowing cross-platform communication.
         *
         * @param rtcIceCandidate The concrete RTCIceCandidate to convert.
         * @return The agnostic representation of the ICE candidate.
         */
        fun adaptConcrete(rtcIceCandidate: CrolangP2PIceCandidate): IceCandidateAdapter {
            val agnostic = IceCandidateAdapter()
            agnostic.sdp = rtcIceCandidate.sdp
            agnostic.sdpMid = rtcIceCandidate.sdpMid
            agnostic.sdpMLineIndex = rtcIceCandidate.sdpMLineIndex
            agnostic.serverUrl = rtcIceCandidate.serverUrl
            return agnostic
        }
    }
}
