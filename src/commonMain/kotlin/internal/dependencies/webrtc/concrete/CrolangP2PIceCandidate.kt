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

import kotlinx.serialization.Serializable

/**
 * Represents an ICE (Interactive Connectivity Establishment) candidate for WebRTC connections.
 * 
 * ICE candidates contain network connectivity information that helps establish
 * peer-to-peer connections through NAT traversal techniques.
 *
 * @property sdp The SDP (Session Description Protocol) string containing candidate information
 * @property sdpMid The media stream identification tag
 * @property sdpMLineIndex The index of the media description in the SDP
 * @property serverUrl Optional URL of the ICE server that provided this candidate
 */
@Serializable
class CrolangP2PIceCandidate(
    val sdp: String,
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val serverUrl: String? = null
)
