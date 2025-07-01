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

package internal.event_loop

import internal.dependencies.event_loop.Event
import internal.dependencies.event_loop.EventLoop
import internal.utils.setImmediate

/**
 * JavaScript/Node.js implementation of EventLoop using the Node.js event loop.
 *
 * This implementation leverages the native Node.js event loop through JavaScript's
 * setTimeout and Promise mechanisms to process events asynchronously.
 * Events are processed sequentially to ensure thread-safe execution.
 */
internal class ConcreteEventLoop : EventLoop() {

    private val eventQueue = mutableListOf<Event>()
    private var isProcessing = false

    override fun postEvent(event: Event) {
        eventQueue.add(event)
        startProcessing()
    }

    private fun startProcessing() {
        if (isProcessing) return

        isProcessing = true
        processNextEvent()
    }

    private fun processNextEvent() {
        if (eventQueue.isEmpty()) {
            isProcessing = false
            return
        }

        val event = eventQueue.removeFirstOrNull()
        if (event == null) {
            isProcessing = false
            return
        }

        try {
            event.process()
            // Schedule next event processing on the next tick of Node.js event loop
            setImmediate {
                processNextEvent()
            }
        } catch (e: Throwable) {
            println("Error while processing event: ${e.message}")
            // Continue processing even if one event fails
            setImmediate {
                processNextEvent()
            }
        }
    }
}
