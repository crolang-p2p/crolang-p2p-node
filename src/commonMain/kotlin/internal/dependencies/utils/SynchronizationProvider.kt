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

import kotlinx.coroutines.*

/**
 * Common synchronization provider using Kotlin Multiplatform coroutines.
 * 
 * This object provides synchronization functionality that works across all platforms
 * (JVM, JS, Native) using coroutines instead of platform-specific implementations.
 */
object SynchronizationProvider {

    /**
     * Creates a new countdown latch with the specified initial count.
     * 
     * @param count The initial count of the latch
     * @return A CountdownLatch instance
     */
    fun createCountdownLatch(count: Int): CountdownLatch {
        return CountdownLatch(count)
    }

    /**
     * Executes the given block with synchronization on the specified object.
     * 
     * In coroutine-based implementation, this is essentially a no-op since
     * coroutines provide structured concurrency and most operations are
     * naturally synchronized within a single coroutine context.
     * 
     * @param obj The object to synchronize on (ignored in coroutine implementation)
     * @param block The block of code to execute synchronously
     * @return The result of the block execution
     */
    fun <T> synchronized(@Suppress("UNUSED_PARAMETER") obj: Any, block: () -> T): T {
        return block()
    }
}

/**
 * Coroutine-based countdown latch implementation using Kotlin Multiplatform coroutines.
 * 
 * This implementation uses CompletableDeferred to provide cross-platform synchronization
 * that works naturally with coroutines and event loops on all platforms.
 */
class CountdownLatch(private var count: Int) {
    
    private var isCompleted = false
    private var completionDeferred: CompletableDeferred<Unit>? = null
    
    init {
        if (count <= 0) {
            isCompleted = true
        } else {
            completionDeferred = CompletableDeferred()
        }
    }

    /**
     * Causes the current coroutine to wait until the latch has counted down to zero.
     * 
     * This is a suspending function that properly integrates with coroutines
     * and event loops on all platforms.
     */
    suspend fun await() {
        if (!isCompleted) {
            completionDeferred?.await()
        }
    }

    /**
     * Decrements the count of the latch, releasing all waiting coroutines if the count reaches zero.
     */
    fun countDown() {
        if (count > 0) {
            count--
            if (count == 0) {
                isCompleted = true
                completionDeferred?.complete(Unit)
                completionDeferred = null
            }
        }
    }
}
