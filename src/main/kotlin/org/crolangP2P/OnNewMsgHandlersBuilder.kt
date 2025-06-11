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

import java.util.function.BiConsumer
import kotlin.Unit
import kotlin.jvm.functions.Function2

/**
 * Builder for compact creation of handler maps for onNewMsg, usable from both Java and Kotlin.
 * <p>
 * Allows adding handlers for different channels via the {@code add} method.
 * In Java, you can pass a void lambda using {@code BiConsumer<CrolangNode, String>} without returning Unit.
 * <pre>
 *     var handlers = OnNewMsgHandlersBuilder.createNew()
 *         .add("CHANNEL", (node, msg) -> { ... })
 *         .build();
 * </pre>
 * In Kotlin, you can also use Function2.
 */
class OnNewMsgHandlersBuilder private constructor() {
    private val handlers = mutableMapOf<String, Function2<CrolangNode, String, Unit>>()

    /**
     * Adds a handler for a channel, accepting a Kotlin lambda (Function2).
     * @param channel the channel name
     * @param handler function (CrolangNode, String) -> Unit
     * @return this builder
     */
    fun add(channel: String, handler: Function2<CrolangNode, String, Unit>): OnNewMsgHandlersBuilder {
        handlers[channel] = handler
        return this
    }

    /**
     * Adds a handler for a channel, accepting a Java lambda (BiConsumer) with no return value.
     * @param channel the channel name
     * @param handler BiConsumer<CrolangNode, String>
     * @return this builder
     */
    fun add(channel: String, handler: java.util.function.BiConsumer<CrolangNode, String>): OnNewMsgHandlersBuilder {
        handlers[channel] = object : Function2<CrolangNode, String, Unit> {
            override fun invoke(node: CrolangNode, msg: String) {
                handler.accept(node, msg)
            }
        }
        return this
    }

    /**
     * Returns the final channel -> handler map to use in onNewMsg.
     */
    fun build(): Map<String, Function2<CrolangNode, String, Unit>> = handlers

    companion object {
        /**
         * Factory method to obtain a new builder, usable from Java.
         */
        @JvmStatic
        fun createNew(): OnNewMsgHandlersBuilder = OnNewMsgHandlersBuilder()
    }
}
