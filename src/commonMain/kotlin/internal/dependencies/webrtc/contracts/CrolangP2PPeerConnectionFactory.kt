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

package internal.dependencies.webrtc.contracts

import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration

/**
 * Abstract factory for creating WebRTC peer connections.
 * 
 * This factory provides a platform-independent interface for creating
 * peer connections with appropriate event handlers and configuration.
 */
abstract class CrolangP2PPeerConnectionFactory {

    /**
     * Creates a new WebRTC peer connection with the specified configuration and event handlers.
     * 
     * @param rtcConfiguration Configuration parameters for the peer connection
     * @param onIceCandidate Callback invoked when new ICE candidates are discovered
     * @param onConnectionChange Callback invoked when the connection state changes
     * @param onDataChannel Callback invoked when a new data channel is created
     * @return A configured peer connection instance
     */
    abstract fun createPeerConnection(
        rtcConfiguration: CrolangP2PRTCConfiguration,
        onIceCandidate: (CrolangP2PIceCandidate) -> Unit,
        onConnectionChange: (state: CrolangP2PRTCPeerConnectionState) -> Unit,
        onDataChannel: (CrolangP2PRTCDataChannel) -> Unit
    ): CrolangP2PRTCPeerConnection

}
