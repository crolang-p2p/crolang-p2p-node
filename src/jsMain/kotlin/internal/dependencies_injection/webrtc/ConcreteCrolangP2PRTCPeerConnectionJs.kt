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
                onDataChannel(ConcreteCrolangP2PRTCDataChannelJs(dataChannel, scope))
            }
            .launchIn(scope)
    }

    /**
     * Creates a new data channel for sending messages to the remote peer.
     * 
     * This method creates an ordered data channel with the label "crolang-messages"
     * that can be used for reliable message transmission.
     * 
     * @return A new [CrolangP2PRTCDataChannel] for sending/receiving data
     */
    override fun createDataChannel(): CrolangP2PRTCDataChannel {
        val webrtcKmpDataChannel = webrtcKmpPeerConnection.createDataChannel(
            label = "crolang-messages",
            ordered = true
        )
        return ConcreteCrolangP2PRTCDataChannelJs(webrtcKmpDataChannel!!, scope)
    }

    /**
     * Adds an ICE candidate to the peer connection.
     * 
     * ICE candidates contain connectivity information that helps establish
     * a direct connection between peers.
     * 
     * @param iceCandidate The ICE candidate to add
     */
    override fun addIceCandidate(iceCandidate: CrolangP2PIceCandidate) {
        scope.launch {
            with(WebRTCKmpToCrolangP2PMapper) {
                webrtcKmpPeerConnection.addIceCandidate(iceCandidate.toWebRTCKmpIceCandidate())
            }
        }
    }

    /**
     * Creates an SDP offer for initiating a WebRTC connection.
     * 
     * The offer contains information about the capabilities and preferences
     * of the local peer. This is typically the first step in WebRTC negotiation.
     * 
     * @param onSuccess Callback invoked with the created offer description
     * @param onFailure Callback invoked if offer creation fails
     */
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

    /**
     * Creates an SDP answer in response to a received offer.
     * 
     * The answer contains information about the local peer's capabilities
     * and confirms the parameters for the WebRTC connection.
     * 
     * @param onSuccess Callback invoked with the created answer description
     * @param onFailure Callback invoked if answer creation fails
     */
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

    /**
     * Sets the local session description for this peer connection.
     * 
     * This method configures the local peer's capabilities and preferences
     * as described in the session description (offer or answer).
     * 
     * @param description The session description to set as the local description
     * @param onSuccess Callback invoked when the operation completes successfully
     * @param onFailure Callback invoked if the operation fails
     */
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

    /**
     * Sets the remote session description for this peer connection.
     * 
     * This method configures the remote peer's capabilities and preferences
     * as described in the received session description (offer or answer).
     * 
     * @param description The session description received from the remote peer
     * @param onSuccess Callback invoked when the operation completes successfully
     * @param onFailure Callback invoked if the operation fails
     */
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

    /**
     * Gets the current connection state of the peer connection.
     * 
     * @return The current [CrolangP2PRTCPeerConnectionState] indicating the
     *         connection status (new, connecting, connected, disconnected, etc.)
     */
    override fun connectionState(): CrolangP2PRTCPeerConnectionState {
        return webrtcKmpPeerConnection.connectionState.toCrolangP2PConnectionState()
    }

    /**
     * Closes the peer connection and releases all associated resources.
     * 
     * This method cancels all coroutines and conditionally closes the native
     * WebRTC connection. In Node.js environments, we avoid calling close()
     * to prevent FATAL ERRORs during native object finalization.
     */
    override fun close() {
        try {
            // Cancel the scope to stop all Flow collection
            scope.cancel()
        } catch (e: Exception) {
            println("Error canceling scope: ${e.message}")
        }
        
        if (!isRunningInNodeJs()) {
            // We're in a browser - safe to call close() (on Node.js this would cause FATAL ERROR, automatically handled by garbage collection)
            try {
                webrtcKmpPeerConnection.close()
            } catch (e: Exception) {
                println("Browser cleanup error: ${e.message}")
            }
        }
    }
    
    /**
     * Detects if the code is running in Node.js environment
     */
    private fun isRunningInNodeJs(): Boolean {
        return try {
            // Simple approach: check if we can access Node.js process object
            val processExists = js("typeof process !== 'undefined'")
            val windowExists = js("typeof window !== 'undefined'") 
            
            // Node.js has process but no window
            processExists == true && windowExists == false
        } catch (e: Exception) {
            // If detection fails, assume Node.js to be safe (avoid close())
            true
        }
    }
}
