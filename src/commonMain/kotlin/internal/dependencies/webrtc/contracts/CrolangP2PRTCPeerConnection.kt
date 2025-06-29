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

import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCSessionDescription
import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate

/**
 * Abstract interface for WebRTC peer connections.
 * 
 * Peer connections manage the overall communication session between two peers,
 * including negotiation, ICE candidate exchange, and data channel management.
 */
abstract class CrolangP2PRTCPeerConnection {

    /**
     * Creates a new data channel for this peer connection.
     * 
     * @return A new data channel instance
     */
    abstract fun createDataChannel(): CrolangP2PRTCDataChannel

    /**
     * Adds an ICE candidate to the peer connection for connectivity establishment.
     * 
     * @param iceCandidate The ICE candidate to add
     */
    abstract fun addIceCandidate(iceCandidate: CrolangP2PIceCandidate)

    /**
     * Creates an offer for establishing a connection with the remote peer.
     * 
     * @param onSuccess Callback invoked with the created offer
     * @param onFailure Callback invoked if offer creation fails
     */
    abstract fun createOffer(
        onSuccess: (description: CrolangP2PRTCSessionDescription) -> Unit,
        onFailure: (error: String?) -> Unit
    )

    /**
     * Creates an answer to a received offer from the remote peer.
     * 
     * @param onSuccess Callback invoked with the created answer
     * @param onFailure Callback invoked if answer creation fails
     */
    abstract fun createAnswer(
        onSuccess: (description: CrolangP2PRTCSessionDescription) -> Unit,
        onFailure: (error: String?) -> Unit
    )

    /**
     * Sets the local session description for this peer connection.
     * 
     * @param description The local session description to set
     * @param onSuccess Callback invoked when the operation succeeds
     * @param onFailure Callback invoked if the operation fails
     */
    abstract fun setLocalDescription(
        description: CrolangP2PRTCSessionDescription,
        onSuccess: () -> Unit,
        onFailure: (error: String?) -> Unit
    )

    /**
     * Sets the remote session description received from the remote peer.
     * 
     * @param description The remote session description to set
     * @param onSuccess Callback invoked when the operation succeeds
     * @param onFailure Callback invoked if the operation fails
     */
    abstract fun setRemoteDescription(
        description: CrolangP2PRTCSessionDescription,
        onSuccess: () -> Unit,
        onFailure: (error: String?) -> Unit
    )

    /**
     * Returns the current connection state of this peer connection.
     * 
     * @return The current connection state
     */
    abstract fun connectionState(): CrolangP2PRTCPeerConnectionState

    /**
     * Closes the peer connection and releases all associated resources.
     */
    abstract fun close()

}
