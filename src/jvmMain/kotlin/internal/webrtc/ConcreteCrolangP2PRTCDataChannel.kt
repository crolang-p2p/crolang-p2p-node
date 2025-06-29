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

package internal.webrtc

import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelBuffer
import dev.onvoid.webrtc.RTCDataChannelObserver
import dev.onvoid.webrtc.RTCDataChannelState
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelObserver
import internal.dependencies.webrtc.concrete.CrolangP2PRTCDataChannelState
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import java.nio.ByteBuffer

internal class ConcreteCrolangP2PRTCDataChannel(private val channel: RTCDataChannel) : CrolangP2PRTCDataChannel() {

    override fun bufferedAmount(): Long {
        return channel.bufferedAmount
    }

    override fun close() {
        channel.close()
    }

    override fun registerObserver(observer: CrolangP2PRTCDataChannelObserver) {
        channel.registerObserver(object : RTCDataChannelObserver {
            override fun onBufferedAmountChange(previousAmount: Long) {}

            override fun onStateChange() { observer.onStateChange() }

            override fun onMessage(buffer: RTCDataChannelBuffer?) {
                buffer?.data?.let {
                    val byteArray = ByteArray(buffer.data.remaining())
                    buffer.data.get(byteArray)
                    observer.onMessage(byteArray)
                }
            }

        })
    }

    override fun send(data: ByteArray) {
        channel.send(RTCDataChannelBuffer(ByteBuffer.wrap(data), true))
    }

    override fun state(): CrolangP2PRTCDataChannelState {
        val state = channel.state
        return if(state == null) {
            CrolangP2PRTCDataChannelState.CLOSED
        } else {
            when (state) {
                RTCDataChannelState.CONNECTING -> CrolangP2PRTCDataChannelState.CONNECTING
                RTCDataChannelState.OPEN -> CrolangP2PRTCDataChannelState.OPEN
                RTCDataChannelState.CLOSING -> CrolangP2PRTCDataChannelState.CLOSING
                RTCDataChannelState.CLOSED -> CrolangP2PRTCDataChannelState.CLOSED
            }
        }
    }
}
