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

package internal.dependencies_injection.webrtc.mappers

import dev.onvoid.webrtc.RTCBundlePolicy
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceServer
import dev.onvoid.webrtc.RTCIceTransportPolicy
import dev.onvoid.webrtc.RTCRtcpMuxPolicy
import internal.dependencies.webrtc.concrete.CrolangP2PRTCBundlePolicy
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCIceTransportPolicy
import internal.dependencies.webrtc.concrete.CrolangP2PRTCRtcpMuxPolicy

internal object RTCConfigurationCrolangP2PMapper {

    fun mapToOnVoid(rtcConfiguration: CrolangP2PRTCConfiguration): RTCConfiguration {
        return RTCConfiguration().apply {
            rtcConfiguration.bundlePolicy.let {
                bundlePolicy = when (it) {
                    CrolangP2PRTCBundlePolicy.BALANCED -> RTCBundlePolicy.BALANCED
                    CrolangP2PRTCBundlePolicy.MAX_BUNDLE -> RTCBundlePolicy.MAX_BUNDLE
                    CrolangP2PRTCBundlePolicy.MAX_COMPAT -> RTCBundlePolicy.MAX_COMPAT
                }
            }
            rtcConfiguration.iceTransportPolicy.let {
                iceTransportPolicy = when (it) {
                    CrolangP2PRTCIceTransportPolicy.ALL -> RTCIceTransportPolicy.ALL
                    CrolangP2PRTCIceTransportPolicy.RELAY -> RTCIceTransportPolicy.RELAY
                    CrolangP2PRTCIceTransportPolicy.NO_HOST -> RTCIceTransportPolicy.NO_HOST
                    CrolangP2PRTCIceTransportPolicy.NONE -> RTCIceTransportPolicy.NONE
                }
            }
            rtcConfiguration.rtcpMuxPolicy.let {
                rtcpMuxPolicy = when (it) {
                    CrolangP2PRTCRtcpMuxPolicy.REQUIRE -> RTCRtcpMuxPolicy.REQUIRE
                    CrolangP2PRTCRtcpMuxPolicy.NEGOTIATE -> RTCRtcpMuxPolicy.NEGOTIATE
                }
            }
            rtcConfiguration.iceServers.let {
                iceServers = it.map { server ->
                    RTCIceServer().apply {
                        urls = server.urls
                        username = server.username
                        password = server.password
                    }
                }
            }
        }
    }
}
