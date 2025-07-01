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

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.IceServer
import com.shepeliev.webrtckmp.PeerConnectionState
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import com.shepeliev.webrtckmp.DataChannelState
import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCIceServer
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSdpType
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription

/**
 * Mapper utility for converting between webrtc-kmp types and crolang-p2p-node types.
 */
internal object WebRTCKmpToCrolangP2PMapper {

    /**
     * Converts CrolangP2PRTCConfiguration to webrtc-kmp RtcConfiguration
     */
    fun CrolangP2PRTCConfiguration.toWebRTCKmpConfiguration(): RtcConfiguration {
        return RtcConfiguration(
            iceServers = this.iceServers.map { it.toWebRTCKmpIceServer() }
        )
    }

    /**
     * Converts CrolangP2PRTCIceServer to webrtc-kmp IceServer
     */
    private fun CrolangP2PRTCIceServer.toWebRTCKmpIceServer(): IceServer {
        return IceServer(
            urls = this.urls,
            username = this.username ?: "",
            password = this.password ?: ""
        )
    }

    /**
     * Converts webrtc-kmp IceCandidate to CrolangP2PIceCandidate
     */
    fun IceCandidate.toCrolangP2PIceCandidate(): CrolangP2PIceCandidate {
        return CrolangP2PIceCandidate(
            sdp = this.candidate,
            sdpMid = this.sdpMid,
            sdpMLineIndex = this.sdpMLineIndex
        )
    }

    /**
     * Converts CrolangP2PIceCandidate to webrtc-kmp IceCandidate
     */
    fun CrolangP2PIceCandidate.toWebRTCKmpIceCandidate(): IceCandidate {
        return IceCandidate(
            sdpMid = this.sdpMid,
            sdpMLineIndex = this.sdpMLineIndex,
            candidate = this.sdp
        )
    }

    /**
     * Converts webrtc-kmp PeerConnectionState to CrolangP2PRTCPeerConnectionState
     */
    fun PeerConnectionState.toCrolangP2PConnectionState(): CrolangP2PRTCPeerConnectionState {
        return when (this) {
            PeerConnectionState.New -> CrolangP2PRTCPeerConnectionState.NEW
            PeerConnectionState.Connecting -> CrolangP2PRTCPeerConnectionState.CONNECTING
            PeerConnectionState.Connected -> CrolangP2PRTCPeerConnectionState.CONNECTED
            PeerConnectionState.Disconnected -> CrolangP2PRTCPeerConnectionState.DISCONNECTED
            PeerConnectionState.Failed -> CrolangP2PRTCPeerConnectionState.FAILED
            PeerConnectionState.Closed -> CrolangP2PRTCPeerConnectionState.CLOSED
        }
    }

    /**
     * Converts webrtc-kmp DataChannelState to CrolangP2PRTCDataChannelState
     */
    fun DataChannelState.toCrolangP2PDataChannelState(): CrolangP2PRTCDataChannelState {
        return when (this) {
            DataChannelState.Connecting -> CrolangP2PRTCDataChannelState.CONNECTING
            DataChannelState.Open -> CrolangP2PRTCDataChannelState.OPEN
            DataChannelState.Closing -> CrolangP2PRTCDataChannelState.CLOSING
            DataChannelState.Closed -> CrolangP2PRTCDataChannelState.CLOSED
        }
    }

    /**
     * Converts webrtc-kmp SessionDescription to CrolangP2PRTCSessionDescription
     */
    fun SessionDescription.toCrolangP2PSessionDescription(): CrolangP2PRTCSessionDescription {
        val sdpType = when (this.type) {
            SessionDescriptionType.Offer -> CrolangP2PRTCSdpType.OFFER
            SessionDescriptionType.Answer -> CrolangP2PRTCSdpType.ANSWER
            SessionDescriptionType.Pranswer -> CrolangP2PRTCSdpType.PR_ANSWER
            SessionDescriptionType.Rollback -> CrolangP2PRTCSdpType.ROLLBACK
        }
        return CrolangP2PRTCSessionDescription(sdpType, this.sdp)
    }

    /**
     * Converts CrolangP2PRTCSessionDescription to webrtc-kmp SessionDescription
     */
    fun CrolangP2PRTCSessionDescription.toWebRTCKmpSessionDescription(): SessionDescription {
        val sdpType = when (this.sdpType) {
            CrolangP2PRTCSdpType.OFFER -> SessionDescriptionType.Offer
            CrolangP2PRTCSdpType.ANSWER -> SessionDescriptionType.Answer
            CrolangP2PRTCSdpType.PR_ANSWER -> SessionDescriptionType.Pranswer
            CrolangP2PRTCSdpType.ROLLBACK -> SessionDescriptionType.Rollback
        }
        return SessionDescription(sdpType, this.sdp ?: "")
    }
}
