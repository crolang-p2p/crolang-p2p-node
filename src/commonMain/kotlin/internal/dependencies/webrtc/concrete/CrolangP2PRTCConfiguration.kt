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

package internal.dependencies.webrtc.concrete

/**
 * Configuration class for WebRTC peer connections in the CroLang P2P protocol.
 * 
 * This class encapsulates all the necessary configuration parameters required
 * to establish and maintain WebRTC peer connections.
 */
class CrolangP2PRTCConfiguration {
    /**
     * List of ICE servers used for NAT traversal and connection establishment.
     * ICE servers include STUN and TURN servers.
     */
    var iceServers: List<CrolangP2PRTCIceServer> = emptyList()
    
    /**
     * Policy for ICE transport, determining which types of candidates are gathered.
     */
    var iceTransportPolicy: CrolangP2PRTCIceTransportPolicy = CrolangP2PRTCIceTransportPolicy.ALL
    
    /**
     * Policy for bundling media streams to reduce the number of transport connections.
     */
    var bundlePolicy: CrolangP2PRTCBundlePolicy = CrolangP2PRTCBundlePolicy.BALANCED
    
    /**
     * Policy for RTP/RTCP multiplexing to use a single port for both RTP and RTCP traffic.
     */
    var rtcpMuxPolicy: CrolangP2PRTCRtcpMuxPolicy = CrolangP2PRTCRtcpMuxPolicy.REQUIRE
}
