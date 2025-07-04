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

package internal.webrtc

import internal.dependencies.webrtc.concrete.CrolangP2PIceCandidate
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCPeerConnectionState
import internal.dependencies.webrtc.contracts.CrolangP2PPeerConnectionFactory
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import internal.dependencies.webrtc.contracts.CrolangP2PRTCPeerConnection

internal class ConcreteCrolangP2PPeerConnectionFactoryJvm : CrolangP2PPeerConnectionFactory() {

    override fun createPeerConnection(
        rtcConfiguration: CrolangP2PRTCConfiguration,
        onIceCandidate: (CrolangP2PIceCandidate) -> Unit,
        onConnectionChange: (state: CrolangP2PRTCPeerConnectionState) -> Unit,
        onDataChannel: (CrolangP2PRTCDataChannel) -> Unit
    ): CrolangP2PRTCPeerConnection {
        return ConcreteCrolangP2PRTCPeerConnectionJvm(rtcConfiguration, onIceCandidate, onConnectionChange, onDataChannel)
    }

}
