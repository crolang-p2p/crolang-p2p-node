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

import dev.onvoid.webrtc.RTCSdpType
import dev.onvoid.webrtc.RTCSessionDescription
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSdpType
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription

internal object RTCSessionDescriptionCrolangP2PMapper {

    fun mapToCrolangP2P(description: RTCSessionDescription): CrolangP2PRTCSessionDescription {
        return CrolangP2PRTCSessionDescription(
            when (description.sdpType) {
                RTCSdpType.OFFER -> CrolangP2PRTCSdpType.OFFER
                RTCSdpType.ANSWER -> CrolangP2PRTCSdpType.ANSWER
                RTCSdpType.ROLLBACK -> CrolangP2PRTCSdpType.ROLLBACK
                RTCSdpType.PR_ANSWER -> CrolangP2PRTCSdpType.PR_ANSWER
                else -> throw IllegalArgumentException("Unknown SDP type: ${description.sdpType}")
            },
            description.sdp
        )
    }

    fun mapToOnVoid(description: CrolangP2PRTCSessionDescription): RTCSessionDescription {
        return RTCSessionDescription(
            when (description.sdpType) {
                CrolangP2PRTCSdpType.OFFER -> RTCSdpType.OFFER
                CrolangP2PRTCSdpType.ANSWER -> RTCSdpType.ANSWER
                CrolangP2PRTCSdpType.ROLLBACK -> RTCSdpType.ROLLBACK
                CrolangP2PRTCSdpType.PR_ANSWER -> RTCSdpType.PR_ANSWER
            },
            description.sdp
        )
    }
}
