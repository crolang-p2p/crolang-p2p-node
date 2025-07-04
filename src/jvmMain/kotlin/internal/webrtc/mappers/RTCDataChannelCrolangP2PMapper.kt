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

package internal.webrtc.mappers

import dev.onvoid.webrtc.RTCDataChannel
import internal.dependencies.webrtc.contracts.CrolangP2PRTCDataChannel
import internal.webrtc.ConcreteCrolangP2PRTCDataChannelJvm

internal object RTCDataChannelCrolangP2PMapper {

    fun mapToCrolangP2P(channel: RTCDataChannel): CrolangP2PRTCDataChannel {
        return ConcreteCrolangP2PRTCDataChannelJvm(channel)
    }
}
