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
 * Configuration for an ICE server used in WebRTC connections.
 * 
 * ICE servers include STUN and TURN servers that help establish
 * peer-to-peer connections through NAT traversal.
 */
class CrolangP2PRTCIceServer {
    /** List of server URLs (e.g., "stun:stun.example.com:3478", "turn:turn.example.com:3478") */
    var urls: List<String> = emptyList()
    
    /** Username for authentication with TURN servers (optional) */
    var username: String? = null
    
    /** Password for authentication with TURN servers (optional) */
    var password: String? = null
}
