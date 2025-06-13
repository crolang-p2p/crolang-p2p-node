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

package org.crolangP2P

/**
 * Builder for compact creation of handler maps for WebSocket onNewMsg, usable from Java.
 * <p>
 * Allows adding handlers for different channels via the {@code add} method.
 */
class OnNewSocketMsgHandlersBuilder private constructor() {

    private val handlers = mutableMapOf<String, Function2<String, String, Unit>>()

    /**
     * Adds a handler for a channel, accepting a Java lambda (BiConsumer) with no return value.
     * @param channel the channel name
     * @param handler BiConsumer<String, String> (socketId, msg) -> void
     * @return this builder
     */
    fun add(channel: String, handler: java.util.function.BiConsumer<String, String>): OnNewSocketMsgHandlersBuilder {
        handlers[channel] = object : Function2<String, String, Unit> {
            override fun invoke(socketId: String, msg: String) {
                handler.accept(socketId, msg)
            }
        }
        return this
    }

    /**
     * Returns the final channel -> handler map to use in onNewMsg.
     */
    fun build(): Map<String, Function2<String, String, Unit>> = handlers

    companion object {
        /**
         * Factory method to obtain a new builder, usable from Java.
         */
        @JvmStatic
        fun createNew(): OnNewSocketMsgHandlersBuilder = OnNewSocketMsgHandlersBuilder()
    }

}
