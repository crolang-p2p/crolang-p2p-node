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

package internal.dependencies.event_loop

/**
 * Abstract base class for platform-specific event loop implementations.
 * 
 * The event loop is responsible for processing events asynchronously in a sequential manner.
 * This ensures thread-safe execution of operations within the CroLang P2P library.
 */
abstract class EventLoop {
    /**
     * Posts an event to be processed asynchronously by the event loop.
     * Events are typically processed in FIFO (First In, First Out) order.
     *
     * @param event The event to be processed
     */
    abstract fun postEvent(event: Event)
}

/**
 * Represents an event that can be processed asynchronously within the event loop.
 * Each event must implement the `process` method to define its execution logic.
 */
interface Event {

    /**
     * Defines the logic to be executed when the event is processed.
     */
    fun process()

}
