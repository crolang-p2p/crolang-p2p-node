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

package internal.events.data

import internal.dependencies.webrtc.concrete.CrolangP2PRTCBundlePolicy
import internal.dependencies.webrtc.concrete.CrolangP2PRTCConfiguration
import internal.dependencies.webrtc.concrete.CrolangP2PRTCIceServer
import internal.dependencies.webrtc.concrete.CrolangP2PRTCIceTransportPolicy
import internal.dependencies.webrtc.concrete.CrolangP2PRTCRtcpMuxPolicy
import internal.events.data.abstractions.ParsableMsg
import internal.utils.SharedStore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a message containing RTC configuration data, capable of being parsed into a concrete RTC configuration message.
 *
 * This class parses the JSON payload of an RTC configuration message and converts it into a concrete `RTCConfigurationMsg` object
 * containing the necessary RTC configuration data.
 *
 * @property iceServers The list of ICE servers used for the RTC connection.
 * @property iceTransportPolicy The ICE transport policy for the RTC connection.
 * @property bundlePolicy The bundle policy for the RTC connection.
 * @property rtcpMuxPolicy The RTCP mux policy for the RTC connection.
 * @property iceCandidatePoolSize The size of the ICE candidate pool for the RTC connection.
 */
@Serializable
internal class ParsableRTCConfigurationMsg : ParsableMsg<RTCConfigurationMsg>() {

    @SerialName("iceServers") var iceServers: ArrayList<ParsableRTCIceServerMsg> = arrayListOf()
    @SerialName("iceTransportPolicy") var iceTransportPolicy: String? = null
    @SerialName("bundlePolicy") var bundlePolicy: String? = null
    @SerialName("rtcpMuxPolicy") var rtcpMuxPolicy: String? = null
    @SerialName("iceCandidatePoolSize") var iceCandidatePoolSize: Int? = null

    /**
     * Converts the parsed, agnostic RTC configuration message into a concrete `RTCConfigurationMsg`.
     *
     * @return An `nullable` containing the concrete `RTCConfigurationMsg` if valid, null if the conversion failed.
     *
     */
    override fun toChecked(): RTCConfigurationMsg? {
        if(iceServers.isEmpty()){
            return null
        }
        return RTCConfigurationMsg(
            iceServers.map { it.toChecked() }.toCollection(arrayListOf()),
            iceTransportPolicy,
            bundlePolicy,
            rtcpMuxPolicy,
            iceCandidatePoolSize
        )
    }

}

/**
 * Represents a message containing ICE server data, capable of being parsed into a concrete ICE server message.
 *
 * This class parses the JSON payload of an ICE server message and converts it into a concrete `RTCIceServerMsg` object
 *
 * @property urls The list of URLs for the ICE server.
 * @property username The username for the ICE server.
 * @property password The password for the ICE server.
 */
@Serializable
internal class ParsableRTCIceServerMsg {

    @SerialName("urls") var urls: ArrayList<String> = arrayListOf()
    @SerialName("username") var username: String? = null
    @SerialName("password") var password: String? = null

    fun toChecked(): RTCIceServerMsg {
        return RTCIceServerMsg(urls, username, password)
    }

}

/**
 * Represents a concrete message containing RTC configuration data.
 *
 * @property iceServers The list of ICE servers used for the RTC connection.
 * @property iceTransportPolicy The ICE transport policy for the RTC connection.
 * @property bundlePolicy The bundle policy for the RTC connection.
 * @property rtcpMuxPolicy The RTCP mux policy for the RTC connection.
 * @property iceCandidatePoolSize The size of the ICE candidate pool for the RTC connection.
 */
internal class RTCConfigurationMsg(
    private val iceServers: ArrayList<RTCIceServerMsg>,
    private val iceTransportPolicy: String?,
    private val bundlePolicy: String?,
    private val rtcpMuxPolicy: String?,
    private val iceCandidatePoolSize: Int?
){

    /**
     * Converts the agnostic RTC configuration message to a concrete `RTCConfiguration` object.
     *
     * @return The concrete `RTCConfiguration` object.
     */
    fun toConcreteRTCConfiguration(): CrolangP2PRTCConfiguration {
        val rtcConfiguration = CrolangP2PRTCConfiguration()
        rtcConfiguration.iceServers = iceServers.map { it.toConcreteRTCIceServer() }.toCollection(arrayListOf())
        iceTransportPolicy?.let { when(it){
            "all" -> rtcConfiguration.iceTransportPolicy = CrolangP2PRTCIceTransportPolicy.ALL
            "relay" -> rtcConfiguration.iceTransportPolicy = CrolangP2PRTCIceTransportPolicy.RELAY
            "nohost" -> rtcConfiguration.iceTransportPolicy = CrolangP2PRTCIceTransportPolicy.NO_HOST
            "none" -> rtcConfiguration.iceTransportPolicy = CrolangP2PRTCIceTransportPolicy.NONE
            else -> SharedStore.logger.regularErr("Broker sent RTC configuration with unknown ice transport policy: $it")
        }}
        bundlePolicy?.let { when(it){
            "balanced" -> rtcConfiguration.bundlePolicy = CrolangP2PRTCBundlePolicy.BALANCED
            "max-compat" -> rtcConfiguration.bundlePolicy = CrolangP2PRTCBundlePolicy.MAX_COMPAT
            "max-bundle" -> rtcConfiguration.bundlePolicy = CrolangP2PRTCBundlePolicy.MAX_BUNDLE
            else -> SharedStore.logger.regularErr("Broker sent RTC configuration with unknown bundle policy: $it")
        }}
        rtcpMuxPolicy?.let { when(it){
            "require" -> rtcConfiguration.rtcpMuxPolicy = CrolangP2PRTCRtcpMuxPolicy.REQUIRE
            "negotiate" -> rtcConfiguration.rtcpMuxPolicy = CrolangP2PRTCRtcpMuxPolicy.NEGOTIATE
            else -> SharedStore.logger.regularErr("Broker sent RTC configuration with unknown rtcp mux policy: $it")
        }}
        return rtcConfiguration
    }

}

/**
 * Represents a concrete message containing ICE server data.
 *
 * @property urls The list of URLs for the ICE server.
 * @property username The username for the ICE server.
 * @property password The password for the ICE server.
 */
internal class RTCIceServerMsg(
    private val urls: ArrayList<String>,
    private val username: String?,
    private val password: String?
) {

    /**
     * Converts the agnostic ICE server message to a concrete `RTCIceServer` object.
     *
     * @return The concrete `RTCIceServer` object.
     */
    fun toConcreteRTCIceServer(): CrolangP2PRTCIceServer {
        val rtcIceServer = CrolangP2PRTCIceServer()
        rtcIceServer.urls = urls
        username?.let { rtcIceServer.username = it }
        password?.let { rtcIceServer.password = it }
        return rtcIceServer
    }

}
