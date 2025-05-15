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

package internal.utils

import internal.utils.SharedStore.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/**
 * Represents an event that can be processed asynchronously within the event loop.
 * Each event must implement the `process` method to define its execution logic.
 */
internal interface Event {

    /**
     * Defines the logic to be executed when the event is processed.
     */
    fun process()
}

/**
 * A coroutine-based event loop that continuously processes incoming events.
 *
 * This object maintains an unbounded channel (`eventChannel`) where events can be posted.
 * Events are processed sequentially in a coroutine running on `Dispatchers.Default`.
 *
 * The event loop starts automatically upon initialization and runs indefinitely.
 */
internal object EventLoop {
    // Unbounded channel used to queue events for processing
    private val eventChannel = Channel<Event>(Channel.UNLIMITED)

    // Coroutine scope for handling event processing
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Starts the event processing coroutine when the object is initialized
        scope.launch {
            processEvents()
        }
    }

    /**
     * Continuously processes events from the channel.
     * Each event's `process` method is executed in sequence.
     * Any exceptions are caught and logged without stopping the loop.
     */
    private suspend fun processEvents() {
        for (event in eventChannel) {
            try {
                event.process()
            } catch (e: Exception) {
                logger.debugErr("Error while processing event: ${e.message}")
            }
        }
    }

    /**
     * Posts an event to the event queue for asynchronous processing.
     *
     * @param event The event to be processed.
     */
    fun postEvent(event: Event) {
        scope.launch {
            eventChannel.send(event)
        }
    }
}
