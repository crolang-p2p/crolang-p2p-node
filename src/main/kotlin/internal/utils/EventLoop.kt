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
import kotlinx.coroutines.channels.onFailure

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
    private val eventChannel = Channel<Event>(capacity = Channel.UNLIMITED)

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            processEvents()
        }
    }

    private suspend fun processEvents() {
        for (event in eventChannel) {
            try {
                event.process()
            } catch (e: Exception) {
                logger.debugErr("Error while processing event: ${e.message}")
            }
        }
    }

    fun postEvent(event: Event) {
        eventChannel.trySend(event).onFailure {
            logger.debugErr("Failed to enqueue event")
        }
    }
}

