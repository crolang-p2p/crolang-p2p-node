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
 * Enumeration of RTCP multiplexing policies for WebRTC connections.
 * 
 * RTCP mux policy determines whether RTP and RTCP traffic should
 * be multiplexed over a single port or use separate ports.
 */
enum class CrolangP2PRTCRtcpMuxPolicy {
    /** Require RTCP multiplexing - use the same port for RTP and RTCP */
    REQUIRE,
    
    /** Negotiate RTCP multiplexing - attempt to use mux but fall back if needed */
    NEGOTIATE;
}
