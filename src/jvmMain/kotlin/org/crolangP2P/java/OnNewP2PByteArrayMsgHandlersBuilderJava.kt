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

import org.crolangP2P.IncomingByteArrayMsgCallbacks

class OnNewP2PByteArrayMsgHandlersBuilderJava private constructor(){

    private val handlers = mutableMapOf<String, IncomingByteArrayMsgCallbacks>()

    fun add(channel: String, handler: IncomingByteArrayMsgCallbacks): OnNewP2PByteArrayMsgHandlersBuilderJava {
        handlers[channel] = handler
        return this
    }

    fun build(): Map<String, IncomingByteArrayMsgCallbacks> = handlers

    companion object {
        @JvmStatic
        fun createNew(): OnNewP2PByteArrayMsgHandlersBuilderJava = OnNewP2PByteArrayMsgHandlersBuilderJava()
    }
}
