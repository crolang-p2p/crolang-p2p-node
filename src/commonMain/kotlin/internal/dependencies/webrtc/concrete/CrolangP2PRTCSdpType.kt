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
 * Enumeration of SDP (Session Description Protocol) types for WebRTC.
 * 
 * These types define the purpose and content of SDP messages
 * exchanged during WebRTC negotiation.
 */
enum class CrolangP2PRTCSdpType {
    /** An offer to establish a connection with specific capabilities */
    OFFER,
    
    /** A provisional answer that may be updated */
    PR_ANSWER,
    
    /** A final answer to an offer */
    ANSWER,
    
    /** A rollback to a previous negotiation state */
    ROLLBACK;
}
