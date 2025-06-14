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
import internal.utils.TimeoutTimer
import kotlinx.serialization.Serializable
import java.util.Optional
import kotlin.math.ceil
import kotlin.math.min

/**
 * PeerMsg represents a message sent between two peers.
 *
 * @property msgType The type of the message.
 * @property msgId A unique identifier for the message.
 * @property channel The channel through which the message is sent.
 * @property payload The content of the message.
 */
internal open class PeerMsg(
    val msgType: String,
    val msgId: Int,
    val channel: String,
    val payload: String
) {
    /**
     * Splits the payload of the message into smaller chunks to avoid WebRTC size limitations.
     *
     * @param payloadSizeBytes The maximum size of each chunk in bytes.
     * @return A list of PeerMsgPartParsable objects representing the message parts.
     */
    fun splitIntoParts(payloadSizeBytes: Int): List<PeerMsgPartParsable> {
        val payloadLen = payload.length
        val totalParts: Int = ceil(payloadLen.toDouble() / payloadSizeBytes).toInt()
        if(payload.isEmpty() || totalParts == 1){
            val part = PeerMsgPartParsable()
            part.msgType = msgType
            part.total = 1
            part.part = 0
            part.msgId = msgId
            part.channel = channel
            part.payload = payload
            return listOf(part)
        } else {
            return (0 until totalParts).map { i ->
                val part = PeerMsgPartParsable()
                part.msgType = msgType
                part.total = totalParts
                part.part = i
                part.msgId = msgId
                part.channel = channel

                // extracts a chunk of the payload calculating the start and end of the chunk
                val chunkStart = i * payloadSizeBytes
                part.payload = payload.substring(chunkStart, min(chunkStart + payloadSizeBytes, payloadLen))

                part
            }
        }
    }
}

/**
 * PeerMsgPart represents a part of a message. This is used when a message is split into multiple parts.
 *
 * @property msgType The type of the message.
 * @property msgId The message identifier.
 * @property channel The channel through which the message is sent.
 * @property payload The content of the message.
 * @property part The specific part of the message.
 * @property total The total number of parts the message is split into.
 */
internal class PeerMsgPart(
    msgType: String,
    msgId: Int,
    channel: String,
    payload: String,
    val part: Int,
    val total: Int
) : PeerMsg(msgType, msgId, channel, payload)

/**
 * PeerMsgPartParsable represents a part of a message that can be deserialized. This is used for parsing data
 * received from a peer.
 *
 * @property msgType The type of the message.
 * @property msgId The message identifier.
 * @property channel The channel through which the message is sent.
 * @property payload The content of the message.
 * @property part The specific part of the message.
 * @property total The total number of parts the message is split into.
 */
@Serializable
internal class PeerMsgPartParsable {
    @SerializedName("msgType") var msgType: String? = null
    @SerializedName("msgId") var msgId: Int? = null
    @SerializedName("channel") var channel: String? = null
    @SerializedName("payload") var payload: String? = null
    @SerializedName("part") var part: Int? = null
    @SerializedName("total") var total: Int? = null

    /**
     * Converts the PeerMsgPartParsable object into a PeerMsgPart object if all necessary fields are present.
     *
     * @return An Optional containing the PeerMsgPart if valid, otherwise an empty Optional.
     */
    fun toChecked(): Optional<PeerMsgPart> {
        return if (msgType == null || msgId == null || channel == null || payload == null || part == null || total == null) {
            Optional.empty()
        } else {
            Optional.of(PeerMsgPart(msgType!!, msgId!!, channel!!, payload!!, part!!, total!!))
        }
    }
}

/**
 * IncomingMultipartP2PMsg manages a message that has been split into multiple parts and received over peer-to-peer.
 *
 * @param firstMsgPart The first part of the message received.
 * @param timeoutMs The timeout in milliseconds for receiving subsequent parts.
 * @param onTimeoutCallback The function to be executed when the timeout expires.
 */
internal class IncomingMultipartP2PMsg(
    firstMsgPart: PeerMsgPart,
    timeoutMs: Long,
    onTimeoutCallback: () -> Unit
) {

    private val timeoutTimer = TimeoutTimer(timeoutMs) {
        if (gathered.size < total) {
            onTimeoutCallback()
        }
    }

    private val mstType: String = firstMsgPart.msgType
    private val msgId: Int = firstMsgPart.msgId
    private val channel: String = firstMsgPart.channel
    private val total: Int = firstMsgPart.total
    private val totalCheck: Int = firstMsgPart.total - 1
    private val gathered: ArrayList<String> = ArrayList(firstMsgPart.total)

    init {
        gathered.add(firstMsgPart.payload)
    }

    fun cancelTimer() {
        timeoutTimer.cancel()
    }

    /**
     * Checks if a part of the message has already been deposited.
     *
     * @param part The part of the message to check.
     * @return true if the part has already been deposited, otherwise false.
     */
    fun isPartToDeposit(part: PeerMsgPart): Boolean {
        return gathered.size == part.part
    }

    /**
     * Deposits a new part of the message. If all parts are received, the message is reassembled and returned.
     *
     * @param part The new part of the message to deposit.
     * @return An Optional containing the result of merging the parts (MsgPartsMergeResult),
     *         or an empty Optional if all parts are not yet received.
     */
    fun depositNewPart(part: PeerMsgPart): Optional<MsgPartsMergeResult> {
        gathered.add(part.payload)
        return if (part.part != totalCheck) {
            Optional.empty()
        } else {
            timeoutTimer.cancel()
            Optional.of(mergeParts())
        }
    }

    /**
     * Merges all received parts into a single message.
     *
     * @return The result of merging the parts, which indicates if there was an error or if the message was successfully
     * reconstructed.
     */
    private fun mergeParts(): MsgPartsMergeResult {
        return if (gathered.size == total) {
            val finalPayload = StringBuilder()
            for (part in gathered) {
                finalPayload.append(part)
            }
            MsgPartsMergeResult(false, Optional.of(PeerMsg(mstType, msgId, channel, finalPayload.toString())))
        } else {
            MsgPartsMergeResult(true, Optional.empty())
        }
    }

}

/**
 * MsgPartsMergeResult represents the result of merging the parts of a message.
 *
 * @property isError Indicates if there was an error during the merging process.
 * @property msg The reconstructed PeerMsg object, if the merging was successful.
 */
internal class MsgPartsMergeResult(val isError: Boolean, val msg: Optional<PeerMsg>)
