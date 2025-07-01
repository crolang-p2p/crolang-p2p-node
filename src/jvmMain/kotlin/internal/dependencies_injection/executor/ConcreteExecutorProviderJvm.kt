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

package internal.dependencies_injection.executor

import internal.dependencies.utils.ExecutorProvider
import java.util.concurrent.Executors

/**
 * JVM-specific implementation of ExecutorProvider using java.util.concurrent.Executors.
 * This implementation uses a cached thread pool for executing tasks asynchronously.
 */
class ConcreteExecutorProviderJvm : ExecutorProvider {
    
    /**
     * Executor service for managing threads in the library.
     * This uses a cached thread pool which creates new threads as needed,
     * but will reuse previously constructed threads when they are available.
     */
    private val executor = Executors.newCachedThreadPool()
    
    override fun executeAsync(task: () -> Unit) {
        try {
            executor.submit {
                task()
            }
        } catch (e: Exception) {
            // Silently handle exceptions - this matches the existing behavior
            // where the SharedStore catches exceptions and logs a generic error message
        }
    }
}
