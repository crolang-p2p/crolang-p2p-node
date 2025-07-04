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
 * Abstract observer for WebRTC data channel events.
 * 
 * Implementations of this class can monitor data channel state changes
 * and receive incoming messages from remote peers.
 */
abstract class CrolangP2PRTCDataChannelObserver {

    /**
     * Called when the data channel state changes.
     * This includes transitions between CONNECTING, OPEN, CLOSING, and CLOSED states.
     */
    abstract fun onStateChange()
    
    /**
     * Called when a message is received through the data channel.
     * 
     * @param message The binary message received from the remote peer
     */
    abstract fun onMessage(message: ByteArray)

}
