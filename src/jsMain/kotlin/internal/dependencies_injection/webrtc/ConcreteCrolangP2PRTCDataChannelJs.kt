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

package internal.dependencies_injection.webrtc

import com.shepeliev.webrtckmp.DataChannel
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelObserver
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelState
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import internal.dependencies_injection.webrtc.mappers.WebRTCKmpToCrolangP2PMapper.toCrolangP2PDataChannelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * JavaScript implementation of WebRTC data channel using webrtc-kmp library.
 * 
 * This class adapts the Flow-based API of webrtc-kmp DataChannel to the 
 * observer-based API expected by the crolang-p2p-node architecture.
 */
internal class ConcreteCrolangP2PRTCDataChannelJs(
    private val webrtcKmpDataChannel: DataChannel,
    private val sharedScope: CoroutineScope // Use shared scope from PeerConnection
) : CrolangP2PRTCDataChannel() {
    
    // Current observer for state changes and messages
    private var currentObserver: CrolangP2PRTCDataChannelObserver? = null
    
    init {
        setupEventAdapters()
    }
    
    /**
     * Sets up event adapters to convert Flow-based events to observer-based events.
     */
    private fun setupEventAdapters() {
        // Message events
        webrtcKmpDataChannel.onMessage
            .onEach { data ->
                currentObserver?.onMessage(data)
            }
            .launchIn(sharedScope)
        
        // State change events - we need to monitor multiple flows
        webrtcKmpDataChannel.onOpen
            .onEach { 
                currentObserver?.onStateChange() 
            }
            .launchIn(sharedScope)
        
        webrtcKmpDataChannel.onClose
            .onEach { 
                currentObserver?.onStateChange() 
            }
            .launchIn(sharedScope)
        
        webrtcKmpDataChannel.onClosing
            .onEach { 
                currentObserver?.onStateChange() 
            }
            .launchIn(sharedScope)
        
        webrtcKmpDataChannel.onError
            .onEach { error ->
                // Log error but still notify state change
                println("DataChannel error: $error")
                currentObserver?.onStateChange()
            }
            .launchIn(sharedScope)
    }

    /**
     * Gets the current state of the data channel.
     * 
     * @return The current [CrolangP2PRTCDataChannelState] indicating whether the channel
     *         is connecting, open, closing, or closed
     */
    override fun state(): CrolangP2PRTCDataChannelState {
        return webrtcKmpDataChannel.readyState.toCrolangP2PDataChannelState()
    }

    /**
     * Registers an observer to receive data channel events.
     * 
     * The observer will be notified of state changes and incoming messages
     * on this data channel.
     * 
     * @param observer The observer to register for receiving events
     */
    override fun registerObserver(observer: CrolangP2PRTCDataChannelObserver) {
        currentObserver = observer
    }

    /**
     * Sends binary data through the data channel.
     * 
     * @param data The byte array to send to the remote peer
     */
    override fun send(data: ByteArray) {
        webrtcKmpDataChannel.send(data)
    }

    /**
     * Gets the amount of data currently buffered for transmission.
     * 
     * This indicates how much data is queued to be sent but hasn't been
     * transmitted yet. Can be used to implement flow control.
     * 
     * @return The number of bytes currently buffered
     */
    override fun bufferedAmount(): Int {
        // js has problems with kotlin's Long type, so we convert to Int (calling directly .toInt() on Long does not work, we need to convert to String first)
        return webrtcKmpDataChannel.bufferedAmount.toString().toInt()
    }

    /**
     * Closes the data channel and releases all associated resources.
     * 
     * This method closes the underlying WebRTC data channel.
     * Note: The shared scope is managed by the parent PeerConnection.
     */
    override fun close() {
        // Note: We don't cancel the shared scope here as it's managed by PeerConnection
        // The scope will be cancelled when the PeerConnection is closed
        webrtcKmpDataChannel.close()
    }
}
