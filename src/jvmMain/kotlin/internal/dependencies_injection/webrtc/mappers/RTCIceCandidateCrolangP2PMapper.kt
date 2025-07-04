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

package internal.dependencies_injection.webrtc.mappers

import dev.onvoid.webrtc.RTCIceCandidate
import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate

internal object RTCIceCandidateCrolangP2PMapper {

    fun mapToCrolangP2P(iceCandidate: RTCIceCandidate): CrolangP2PIceCandidate {
        return if(iceCandidate.serverUrl == null || iceCandidate.serverUrl.isEmpty()) {
            CrolangP2PIceCandidate(
                sdp = iceCandidate.sdp,
                sdpMid = iceCandidate.sdpMid,
                sdpMLineIndex = iceCandidate.sdpMLineIndex
            )
        } else {
            CrolangP2PIceCandidate(
                sdp = iceCandidate.sdp,
                sdpMid = iceCandidate.sdpMid,
                sdpMLineIndex = iceCandidate.sdpMLineIndex,
                serverUrl = iceCandidate.serverUrl
            )
        }
    }

    fun mapToOnVoid(iceCandidate: CrolangP2PIceCandidate): RTCIceCandidate {
        return if(iceCandidate.serverUrl == null || iceCandidate.serverUrl.isEmpty()) {
            RTCIceCandidate(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp)
        } else {
            RTCIceCandidate(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp, iceCandidate.serverUrl)
        }
    }
}
