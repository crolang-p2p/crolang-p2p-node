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

import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onDataChannel
import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import internal.dependencies.webrtc.contracts.CrolangP2PRTCPeerConnection
import internal.dependencies_injection.webrtc.mappers.WebRTCKmpToCrolangP2PMapper
import internal.dependencies_injection.webrtc.mappers.WebRTCKmpToCrolangP2PMapper.toCrolangP2PConnectionState
import internal.dependencies_injection.webrtc.mappers.WebRTCKmpToCrolangP2PMapper.toCrolangP2PSessionDescription
import internal.dependencies_injection.webrtc.mappers.WebRTCKmpToCrolangP2PMapper.toWebRTCKmpSessionDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * JavaScript implementation of WebRTC peer connection using webrtc-kmp library.
 * 
 * This class adapts the Flow-based API of webrtc-kmp to the callback-based API
 * expected by the crolang-p2p-node architecture.
 */
internal class ConcreteCrolangP2PRTCPeerConnectionJs(
    rtcConfiguration: CrolangP2PRTCConfiguration,
    private val onIceCandidate: (CrolangP2PIceCandidate) -> Unit,
    private val onConnectionChange: (state: CrolangP2PRTCPeerConnectionState) -> Unit,
    private val onDataChannel: (CrolangP2PRTCDataChannel) -> Unit
) : CrolangP2PRTCPeerConnection() {

    // webrtc-kmp PeerConnection (Flow-based API)
    private val webrtcKmpPeerConnection = PeerConnection(
        WebRTCKmpToCrolangP2PMapper.run { rtcConfiguration.toWebRTCKmpConfiguration() }
    )
    
    // Coroutine scope for managing Flow collectors
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        // Adapt Flow-based events to callback-based events
        setupEventAdapters()
    }
    
    /**
     * Sets up event adapters to convert Flow-based events to callback-based events.
     */
    private fun setupEventAdapters() {
        // ICE candidate events
        webrtcKmpPeerConnection.onIceCandidate
            .onEach { candidate ->
                with(WebRTCKmpToCrolangP2PMapper) {
                    onIceCandidate(candidate.toCrolangP2PIceCandidate())
                }
            }
            .launchIn(scope)
        
        // Connection state change events
        webrtcKmpPeerConnection.onConnectionStateChange
            .onEach { state ->
                with(WebRTCKmpToCrolangP2PMapper) {
                    onConnectionChange(state.toCrolangP2PConnectionState())
                }
            }
            .launchIn(scope)
        
        // Data channel events
        webrtcKmpPeerConnection.onDataChannel
            .onEach { dataChannel ->
                onDataChannel(ConcreteCrolangP2PRTCDataChannelJs(dataChannel))
            }
            .launchIn(scope)
    }

    override fun createDataChannel(): CrolangP2PRTCDataChannel {
        val webrtcKmpDataChannel = webrtcKmpPeerConnection.createDataChannel(
            label = "crolang-messages",
            ordered = true
        )
        return ConcreteCrolangP2PRTCDataChannelJs(webrtcKmpDataChannel!!)
    }

    override fun addIceCandidate(iceCandidate: CrolangP2PIceCandidate) {
        scope.launch {
            with(WebRTCKmpToCrolangP2PMapper) {
                webrtcKmpPeerConnection.addIceCandidate(iceCandidate.toWebRTCKmpIceCandidate())
            }
        }
    }

    override fun createOffer(
        onSuccess: (description: CrolangP2PRTCSessionDescription) -> Unit,
        onFailure: (error: String?) -> Unit
    ) {
        scope.launch {
            try {
                val offer = webrtcKmpPeerConnection.createOffer(
                    OfferAnswerOptions(
                        offerToReceiveAudio = false,
                        offerToReceiveVideo = false
                    )
                )
                with(WebRTCKmpToCrolangP2PMapper) {
                    onSuccess(offer.toCrolangP2PSessionDescription())
                }
            } catch (e: Exception) {
                onFailure(e.message)
            }
        }
    }

    override fun createAnswer(
        onSuccess: (description: CrolangP2PRTCSessionDescription) -> Unit,
        onFailure: (error: String?) -> Unit
    ) {
        scope.launch {
            try {
                val answer = webrtcKmpPeerConnection.createAnswer(
                    OfferAnswerOptions()
                )
                onSuccess(answer.toCrolangP2PSessionDescription())
            } catch (e: Exception) {
                onFailure(e.message)
            }
        }
    }

    override fun setLocalDescription(
        description: CrolangP2PRTCSessionDescription,
        onSuccess: () -> Unit,
        onFailure: (error: String?) -> Unit
    ) {
        scope.launch {
            try {
                webrtcKmpPeerConnection.setLocalDescription(
                    description.toWebRTCKmpSessionDescription()
                )
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message)
            }
        }
    }

    override fun setRemoteDescription(
        description: CrolangP2PRTCSessionDescription,
        onSuccess: () -> Unit,
        onFailure: (error: String?) -> Unit
    ) {
        scope.launch {
            try {
                webrtcKmpPeerConnection.setRemoteDescription(
                    description.toWebRTCKmpSessionDescription()
                )
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message)
            }
        }
    }

    override fun connectionState(): CrolangP2PRTCPeerConnectionState {
        return webrtcKmpPeerConnection.connectionState.toCrolangP2PConnectionState()
    }

    override fun close() {
        scope.cancel() // Cancel all coroutines
        webrtcKmpPeerConnection.close()
    }
}
