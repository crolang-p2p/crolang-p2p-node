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

package internal.webrtc.mappers

import dev.onvoid.webrtc.RTCPeerConnectionState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState

internal object RTCPeerConnectionStateCrolangP2PMapper {

    fun mapToCrolangP2P(state: RTCPeerConnectionState): CrolangP2PRTCPeerConnectionState {
        return when (state) {
            RTCPeerConnectionState.NEW -> CrolangP2PRTCPeerConnectionState.NEW
            RTCPeerConnectionState.CONNECTING -> CrolangP2PRTCPeerConnectionState.CONNECTING
            RTCPeerConnectionState.CONNECTED -> CrolangP2PRTCPeerConnectionState.CONNECTED
            RTCPeerConnectionState.DISCONNECTED -> CrolangP2PRTCPeerConnectionState.DISCONNECTED
            RTCPeerConnectionState.FAILED -> CrolangP2PRTCPeerConnectionState.FAILED
            RTCPeerConnectionState.CLOSED -> CrolangP2PRTCPeerConnectionState.CLOSED
        }
    }

    fun mapToOnVoid(state: CrolangP2PRTCPeerConnectionState): RTCPeerConnectionState {
        return when (state) {
            CrolangP2PRTCPeerConnectionState.NEW -> RTCPeerConnectionState.NEW
            CrolangP2PRTCPeerConnectionState.CONNECTING -> RTCPeerConnectionState.CONNECTING
            CrolangP2PRTCPeerConnectionState.CONNECTED -> RTCPeerConnectionState.CONNECTED
            CrolangP2PRTCPeerConnectionState.DISCONNECTED -> RTCPeerConnectionState.DISCONNECTED
            CrolangP2PRTCPeerConnectionState.FAILED -> RTCPeerConnectionState.FAILED
            CrolangP2PRTCPeerConnectionState.CLOSED -> RTCPeerConnectionState.CLOSED
        }
    }

}
