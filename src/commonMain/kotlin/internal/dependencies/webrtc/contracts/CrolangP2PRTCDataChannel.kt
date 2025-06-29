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

import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelObserver
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelState

/**
 * Abstract interface for WebRTC data channels.
 * 
 * Data channels provide a bidirectional communication channel between peers
 * for sending arbitrary binary data outside of the media streams.
 */
abstract class CrolangP2PRTCDataChannel {

    /**
     * Returns the current state of the data channel.
     * 
     * @return The current data channel state
     */
    abstract fun state(): CrolangP2PRTCDataChannelState

    /**
     * Registers an observer to receive data channel events.
     * 
     * @param observer The observer to register for state changes and messages
     */
    abstract fun registerObserver(observer: CrolangP2PRTCDataChannelObserver)

    /**
     * Sends binary data through the data channel to the remote peer.
     * 
     * @param data The binary data to send
     */
    abstract fun send(data: ByteArray)

    /**
     * Returns the number of bytes currently buffered for transmission.
     * 
     * @return The number of buffered bytes
     */
    abstract fun bufferedAmount(): Long

    /**
     * Closes the data channel and releases associated resources.
     */
    abstract fun close()

}
