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

import internal.dependencies.utils.CountdownLatch
import internal.dependencies.utils.SynchronizationProvider
import internal.utils.SharedStore

/**
 * A utility class that manages synchronization for asynchronous events using platform-agnostic CountdownLatch.
 * This class ensures that a specified number of steps (or events) are completed before proceeding.
 *
 * @param description A descriptive label for debugging purposes.
 */
internal class AwaitAsyncEventGuard(private val description: String) {

    // Holds a nullable CountdownLatch to manage event synchronization.
    private var connectionLatch: CountdownLatch? = null

    /**
     * Checks if a countdown is currently in progress.
     * This method returns true if the countdown latch is present and has not yet reached zero.
     *
     * @return true if a countdown is in progress, false otherwise.
     */
    fun isCountdownInProgress(): Boolean {
        return connectionLatch != null
    }

    /**
     * Starts a new countdown with a specified number of steps.
     * This method initializes a new CountdownLatch with `n` counts.
     *
     * @param n The number of steps before the latch reaches zero. Default is 1.
     * @throws IllegalStateException if a countdown is already in progress.
     */
    fun startNewCountdown(n: Int = 1) {
        SynchronizationProvider.synchronized(this) {
            connectionLatch?.let {
                throw IllegalStateException("Cannot startNewCountdown, countdown already started for '$description'")
            } ?: run {
                connectionLatch = SynchronizationProvider.createCountdownLatch(n)
            }
        }
    }

    /**
     * Decreases the countdown by one step.
     * This method decrements the CountdownLatch count, allowing progress when the count reaches zero.
     *
     * @throws IllegalStateException if no countdown has been started.
     */
    fun stepDown() {
        SynchronizationProvider.synchronized(this) {
            connectionLatch?.let {
                it.countDown()
            } ?: throw IllegalStateException("Cannot stepDown, countdown not started for '$description'")
        }
    }

    /**
     * Suspends the current coroutine until the countdown reaches zero.
     * After awaiting completion, the countdown latch is reset to allow reuse.
     *
     * @throws IllegalStateException if no countdown has been started.
     */
    suspend fun await() {
        connectionLatch?.let { it ->
            it.await()
            connectionLatch = null
        } ?: throw IllegalStateException("Cannot await, countdown not started for '$description'")
    }
}
