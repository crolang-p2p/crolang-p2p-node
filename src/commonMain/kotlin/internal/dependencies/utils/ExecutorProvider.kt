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

package internal.dependencies.utils

/**
 * Platform-agnostic abstraction for executing tasks asynchronously in the background.
 * This interface provides a way to execute callbacks asynchronously without relying on 
 * platform-specific threading mechanisms like java.util.concurrent.Executors.
 * 
 * This abstraction allows the core library to be platform-independent while still 
 * supporting asynchronous task execution that is essential for callback handling 
 * and non-blocking operations.
 */
interface ExecutorProvider {
    
    /**
     * Executes the given task asynchronously in the background.
     * The task should be executed as soon as possible without blocking the current thread.
     * 
     * Implementations should handle exceptions that occur during task execution
     * gracefully, typically by logging them or providing some form of error reporting.
     * 
     * @param task The task to execute asynchronously
     */
    fun executeAsync(task: () -> Unit)
}
