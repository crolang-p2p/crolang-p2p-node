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

import com.google.gson.annotations.SerializedName
import dev.onvoid.webrtc.RTCBundlePolicy
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceServer
import dev.onvoid.webrtc.RTCIceTransportPolicy
import dev.onvoid.webrtc.RTCRtcpMuxPolicy
import internal.events.data.abstractions.ParsableMsg
import internal.utils.SharedStore
import java.util.*
import kotlin.collections.ArrayList

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
internal class ParsableRTCConfigurationMsg : ParsableMsg<RTCConfigurationMsg>() {

    @SerializedName("iceServers") var iceServers: ArrayList<ParsableRTCIceServerMsg> = arrayListOf()
    @SerializedName("iceTransportPolicy") var iceTransportPolicy: String? = null
    @SerializedName("bundlePolicy") var bundlePolicy: String? = null
    @SerializedName("rtcpMuxPolicy") var rtcpMuxPolicy: String? = null
    @SerializedName("iceCandidatePoolSize") var iceCandidatePoolSize: Int? = null

    /**
     * Converts the parsed, agnostic RTC configuration message into a concrete `RTCConfigurationMsg`.
     *
     * @return An `Optional` containing the concrete `RTCConfigurationMsg` if valid, or an empty `Optional` if the conversion failed.
     *
     */
    override fun toChecked(): Optional<RTCConfigurationMsg> {
        if(iceServers.isEmpty()){
            return Optional.empty()
        }
        return Optional.of(RTCConfigurationMsg(
            iceServers.map { it.toChecked() }.toCollection(arrayListOf()),
            Optional.ofNullable(iceTransportPolicy),
            Optional.ofNullable(bundlePolicy),
            Optional.ofNullable(rtcpMuxPolicy),
            Optional.ofNullable(iceCandidatePoolSize)
        ))
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
internal class ParsableRTCIceServerMsg {

    @SerializedName("urls") var urls: ArrayList<String> = arrayListOf()
    @SerializedName("username") var username: String? = null
    @SerializedName("password") var password: String? = null

    fun toChecked(): RTCIceServerMsg {
        return RTCIceServerMsg(urls, Optional.ofNullable(username), Optional.ofNullable(password))
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
    private val iceTransportPolicy: Optional<String>,
    private val bundlePolicy: Optional<String>,
    private val rtcpMuxPolicy: Optional<String>,
    private val iceCandidatePoolSize: Optional<Int> //iceCandidatePoolSize is not used in the kotlin library
){

    /**
     * Converts the agnostic RTC configuration message to a concrete `RTCConfiguration` object.
     *
     * @return The concrete `RTCConfiguration` object.
     */
    fun toConcreteRTCConfiguration(): RTCConfiguration {
        val rtcConfiguration = RTCConfiguration()
        rtcConfiguration.iceServers = iceServers.map { it.toConcreteRTCIceServer() }.toCollection(arrayListOf())
        iceTransportPolicy.ifPresent {
            when(it){
                "all" -> rtcConfiguration.iceTransportPolicy = RTCIceTransportPolicy.ALL
                "relay" -> rtcConfiguration.iceTransportPolicy = RTCIceTransportPolicy.RELAY
                "nohost" -> rtcConfiguration.iceTransportPolicy = RTCIceTransportPolicy.NO_HOST
                "none" -> rtcConfiguration.iceTransportPolicy = RTCIceTransportPolicy.NONE
                else -> SharedStore.logger.regularErr("Broker sent RTC configuration with unknown ice transport policy: $it")
            }
        }
        bundlePolicy.ifPresent {
            when(it){
                "balanced" -> rtcConfiguration.bundlePolicy = RTCBundlePolicy.BALANCED
                "max-compat" -> rtcConfiguration.bundlePolicy = RTCBundlePolicy.MAX_COMPAT
                "max-bundle" -> rtcConfiguration.bundlePolicy = RTCBundlePolicy.MAX_BUNDLE
                else -> SharedStore.logger.regularErr("Broker sent RTC configuration with unknown bundle policy: $it")
            }
        }
        rtcpMuxPolicy.ifPresent {
            when(it){
                "require" -> rtcConfiguration.rtcpMuxPolicy = RTCRtcpMuxPolicy.REQUIRE
                "negotiate" -> rtcConfiguration.rtcpMuxPolicy = RTCRtcpMuxPolicy.NEGOTIATE
                else -> SharedStore.logger.regularErr("Broker sent RTC configuration with unknown rtcp mux policy: $it")
            }
        }
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
    private val username: Optional<String>,
    private val password: Optional<String>
) {

    /**
     * Converts the agnostic ICE server message to a concrete `RTCIceServer` object.
     *
     * @return The concrete `RTCIceServer` object.
     */
    fun toConcreteRTCIceServer(): RTCIceServer {
        val rtcIceServer = RTCIceServer()
        rtcIceServer.urls = urls
        username.ifPresent { rtcIceServer.username = it }
        password.ifPresent { rtcIceServer.password = it }
        return rtcIceServer
    }

}
