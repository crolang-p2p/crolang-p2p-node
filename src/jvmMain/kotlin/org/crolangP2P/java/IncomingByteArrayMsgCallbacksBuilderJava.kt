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

package org.crolangP2P.java

import org.crolangP2P.CrolangNode
import org.crolangP2P.IncomingByteArrayMsgCallbacks
import org.crolangP2P.java.byte_array_msg.OnMsgCorruption
import org.crolangP2P.java.byte_array_msg.OnNewCompleteMsgReceived
import org.crolangP2P.java.byte_array_msg.OnNewMsgPartReceived

class IncomingByteArrayMsgCallbacksBuilderJava private constructor() {

    private var onNewMsgPartReceived: OnNewMsgPartReceived = NoOpOnNewMsgPartReceived()
    private var onNewCompleteMsgReceived: OnNewCompleteMsgReceived = NoOpOnNewCompleteMsgReceived()
    private var onMsgCorruption: OnMsgCorruption = NoOpOnMsgCorruption()

    @JvmInline
    private value class NoOpOnNewMsgPartReceived(val unused: Any? = null) : OnNewMsgPartReceived {
        override fun onNewMsgPartReceived(node: CrolangNode, msgId: Int, part: Int, total: Int) {
            // does nothing
        }
    }

    @JvmInline
    value class NoOpOnNewCompleteMsgReceived(val unused: Any? = null) : OnNewCompleteMsgReceived {
        override fun onNewCompleteMsgReceived(node: CrolangNode, msgId: Int, msg: ByteArray) {
            // does nothing
        }
    }

    @JvmInline
    value class NoOpOnMsgCorruption(val unused: Any? = null) : OnMsgCorruption {
        override fun onMsgCorruption(node: CrolangNode, msgId: Int) {
            // does nothing
        }
    }

    /**
     * Sets the callback for when a new part of a byte array message is received.
     * @param handler the handler to set
     * @return this builder
     */
    fun onNewMsgPartReceived(handler: OnNewMsgPartReceived) = apply {
        this.onNewMsgPartReceived = handler
    }

    /**
     * Sets the callback for when a complete byte array message is received.
     * @param handler the handler to set
     * @return this builder
     */
    fun onNewCompleteMsgReceived(handler: OnNewCompleteMsgReceived) = apply {
        this.onNewCompleteMsgReceived = handler
    }

    /**
     * Sets the callback for when a byte array message is corrupted.
     * @param handler the handler to set
     * @return this builder
     */
    fun onMsgCorruption(handler: OnMsgCorruption) = apply {
        this.onMsgCorruption = handler
    }

    fun build(): IncomingByteArrayMsgCallbacks {
        return IncomingByteArrayMsgCallbacks(
            onNewMsgPartReceived = { node, msgId, part, total ->
                onNewMsgPartReceived.onNewMsgPartReceived(node, msgId, part, total)

            },
            onNewCompleteMsgReceived = { node, msgId, msg ->
                onNewCompleteMsgReceived.onNewCompleteMsgReceived(node, msgId, msg)
            },
            onMsgCorruption = { node, msgId ->
                onMsgCorruption.onMsgCorruption(node, msgId)
            }
        )
    }

    companion object {

        @JvmStatic
        fun createNew(): IncomingByteArrayMsgCallbacksBuilderJava = IncomingByteArrayMsgCallbacksBuilderJava()
    }

}
