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

package internal.executor

import internal.dependencies.utils.ExecutorProvider
import internal.setImmediate

/**
 * JavaScript/Node.js implementation of ExecutorProvider using Node.js event loop.
 * 
 * This implementation leverages the Node.js event loop through setImmediate() to execute
 * tasks asynchronously. In JavaScript's single-threaded environment, this provides
 * non-blocking execution by scheduling tasks for the next iteration of the event loop.
 * 
 * Unlike the JVM implementation which uses a thread pool, this implementation relies
 * on Node.js's event-driven architecture where all operations are scheduled on the
 * same thread but executed asynchronously.
 */
internal class ConcreteExecutorProviderJs : ExecutorProvider {
    
    override fun executeAsync(task: () -> Unit) {
        try {
            // Schedule the task to be executed on the next tick of the Node.js event loop
            // This is equivalent to the JVM's executor.submit() but adapted for JavaScript's
            // single-threaded, event-driven model
            setImmediate {
                try {
                    task()
                } catch (e: Throwable) {
                    // Silently handle exceptions to match JVM behavior
                    // where the SharedStore catches exceptions and logs a generic error message
                }
            }
        } catch (e: Exception) {
            // Handle exceptions during task scheduling
            // This matches the JVM behavior where exceptions in executor.submit() are caught
        }
    }
}
