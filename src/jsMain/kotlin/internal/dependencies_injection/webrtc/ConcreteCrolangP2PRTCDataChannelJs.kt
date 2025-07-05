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
    private val webrtcKmpDataChannel: DataChannel
) : CrolangP2PRTCDataChannel() {
    
    // Coroutine scope for managing Flow collectors
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
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
            .launchIn(scope)
        
        // State change events - we need to monitor multiple flows
        webrtcKmpDataChannel.onOpen
            .onEach { 
                currentObserver?.onStateChange() 
            }
            .launchIn(scope)
        
        webrtcKmpDataChannel.onClose
            .onEach { 
                currentObserver?.onStateChange() 
            }
            .launchIn(scope)
        
        webrtcKmpDataChannel.onClosing
            .onEach { 
                currentObserver?.onStateChange() 
            }
            .launchIn(scope)
        
        webrtcKmpDataChannel.onError
            .onEach { error ->
                // Log error but still notify state change
                console.log("DataChannel error: $error")
                currentObserver?.onStateChange()
            }
            .launchIn(scope)
    }

    override fun state(): CrolangP2PRTCDataChannelState {
        return webrtcKmpDataChannel.readyState.toCrolangP2PDataChannelState()
    }

    override fun registerObserver(observer: CrolangP2PRTCDataChannelObserver) {
        currentObserver = observer
    }

    override fun send(data: ByteArray) {
        webrtcKmpDataChannel.send(data)
    }

    override fun bufferedAmount(): Long {
        return webrtcKmpDataChannel.bufferedAmount
    }

    override fun close() {
        scope.cancel() // Cancel all coroutines
        webrtcKmpDataChannel.close()
    }
}
