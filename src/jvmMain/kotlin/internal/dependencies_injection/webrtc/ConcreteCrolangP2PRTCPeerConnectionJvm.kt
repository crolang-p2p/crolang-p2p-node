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

package internal.dependencies_injection.webrtc

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelInit
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCOfferOptions
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCPriorityType
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import internal.dependencies.webrtc.contracts.CrolangP2PRTCPeerConnection
import internal.dependencies_injection.webrtc.mappers.RTCConfigurationCrolangP2PMapper
import internal.dependencies_injection.webrtc.mappers.RTCDataChannelCrolangP2PMapper
import internal.dependencies_injection.webrtc.mappers.RTCIceCandidateCrolangP2PMapper
import internal.dependencies_injection.webrtc.mappers.RTCPeerConnectionStateCrolangP2PMapper
import internal.dependencies_injection.webrtc.mappers.RTCSessionDescriptionCrolangP2PMapper

internal class ConcreteCrolangP2PRTCPeerConnectionJvm(
    rtcConfiguration: CrolangP2PRTCConfiguration,
    onIceCandidate: (CrolangP2PIceCandidate) -> Unit,
    onConnectionChange: (state: CrolangP2PRTCPeerConnectionState) -> Unit,
    onDataChannel: (CrolangP2PRTCDataChannel) -> Unit
) : CrolangP2PRTCPeerConnection() {

    private val onvoidPeerConnection: RTCPeerConnection = PeerConnectionFactory().createPeerConnection(
        RTCConfigurationCrolangP2PMapper.mapToOnVoid(rtcConfiguration),
        object : PeerConnectionObserver {

            override fun onIceCandidate(candidate: RTCIceCandidate?) {
                candidate?.let { onIceCandidate(RTCIceCandidateCrolangP2PMapper.mapToCrolangP2P(it)) }
            }

            override fun onConnectionChange(state: RTCPeerConnectionState?) {
                state?.let { onConnectionChange(RTCPeerConnectionStateCrolangP2PMapper.mapToCrolangP2P(it)) }
            }

            override fun onDataChannel(channel: RTCDataChannel?) {
                channel?.let { onDataChannel(RTCDataChannelCrolangP2PMapper.mapToCrolangP2P(it)) }
            }

        }
    )

    override fun addIceCandidate(iceCandidate: CrolangP2PIceCandidate) {
        onvoidPeerConnection.addIceCandidate(RTCIceCandidateCrolangP2PMapper.mapToOnVoid(iceCandidate))
    }

    override fun close() {
        onvoidPeerConnection.close()
    }

    override fun connectionState(): CrolangP2PRTCPeerConnectionState {
        return RTCPeerConnectionStateCrolangP2PMapper.mapToCrolangP2P(onvoidPeerConnection.connectionState)
    }

    override fun createAnswer(
        onSuccess: (description: CrolangP2PRTCSessionDescription) -> Unit, onFailure: (error: String?) -> Unit
    ) {
        onvoidPeerConnection.createAnswer(RTCAnswerOptions(), object : CreateSessionDescriptionObserver {
            override fun onSuccess(description: RTCSessionDescription?) {
                description?.let { onSuccess(RTCSessionDescriptionCrolangP2PMapper.mapToCrolangP2P(it)) }
            }

            override fun onFailure(failureErr: String?) { onFailure(failureErr) }
        })
    }

    override fun createDataChannel(): CrolangP2PRTCDataChannel {
        val conf = RTCDataChannelInit()
        conf.ordered = true
        conf.maxRetransmits = -1
        conf.priority = RTCPriorityType.HIGH
        return ConcreteCrolangP2PRTCDataChannelJvm(onvoidPeerConnection.createDataChannel("", conf))
    }

    override fun createOffer(
        onSuccess: (description: CrolangP2PRTCSessionDescription) -> Unit, onFailure: (error: String?) -> Unit
    ) {
        onvoidPeerConnection.createOffer(
            RTCOfferOptions(),
            object : CreateSessionDescriptionObserver {
                override fun onSuccess(description: RTCSessionDescription?) {
                    description?.let { onSuccess(RTCSessionDescriptionCrolangP2PMapper.mapToCrolangP2P(it)) }
                }

                override fun onFailure(failureErr: String?) { onFailure(failureErr) }
            }
        )
    }

    override fun setLocalDescription(
        description: CrolangP2PRTCSessionDescription, onSuccess: () -> Unit, onFailure: (error: String?) -> Unit
    ) {
        onvoidPeerConnection.setLocalDescription(
            RTCSessionDescriptionCrolangP2PMapper.mapToOnVoid(description),
            object : SetSessionDescriptionObserver {
                override fun onSuccess() { onSuccess()}

                override fun onFailure(failureErr: String?) { onFailure(failureErr) }
            }
        )
    }

    override fun setRemoteDescription(
        description: CrolangP2PRTCSessionDescription, onSuccess: () -> Unit, onFailure: (error: String?) -> Unit
    ) {
        onvoidPeerConnection.setRemoteDescription(
            RTCSessionDescriptionCrolangP2PMapper.mapToOnVoid(description),
            object : SetSessionDescriptionObserver {
                override fun onSuccess() { onSuccess() }

                override fun onFailure(failureErr: String?) { onFailure(failureErr) }
            }
        )
    }

}
