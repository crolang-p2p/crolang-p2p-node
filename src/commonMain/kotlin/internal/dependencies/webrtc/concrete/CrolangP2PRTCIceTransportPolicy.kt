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
 * Enumeration of ICE transport policies for WebRTC connections.
 * 
 * ICE transport policy determines which types of ICE candidates
 * are gathered and used during connection establishment.
 */
enum class CrolangP2PRTCIceTransportPolicy {
    /** Gather all types of ICE candidates (host, srflx, relay) */
    ALL,
    
    /** Only gather relay candidates (TURN) for enhanced privacy */
    RELAY,
    
    /** Gather all candidates except host candidates */
    NO_HOST,
    
    /** Do not gather any ICE candidates */
    NONE
}
