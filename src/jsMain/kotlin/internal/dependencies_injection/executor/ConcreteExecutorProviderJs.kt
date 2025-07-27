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

import internal.dependencies.event_loop.Event
import internal.dependencies.utils.ExecutorProvider
import internal.dependencies_injection.event_loop.ConcreteEventLoopJs
import internal.setImmediate

/**
 * JavaScript/Node.js implementation of ExecutorProvider using Node.js event loop.
 */
internal class ConcreteExecutorProviderJs(private val eventLoop: ConcreteEventLoopJs) : ExecutorProvider {
    
    /**
     * Executes a task asynchronously using the Node.js event loop.
     * 
     * @param task The function to execute asynchronously
     */
    override fun executeAsync(task: () -> Unit) {
        eventLoop.postEvent(JsUserRequestedAsyncEvent(task))
    }
}

internal class JsUserRequestedAsyncEvent(private val task: () -> Unit): Event {
    override fun process() {
        task()
    }
}
