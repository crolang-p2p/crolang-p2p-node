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

import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * A utility class that manages synchronization for asynchronous events using CountDownLatch.
 * This class ensures that a specified number of steps (or events) are completed before proceeding.
 *
 * @param description A descriptive label for debugging purposes.
 */
internal class AwaitAsyncEventGuard(private val description: String) {

    // Holds an optional CountDownLatch to manage event synchronization.
    private var connectionLatch: Optional<CountDownLatch> = Optional.empty()

    /**
     * Checks if a countdown is currently in progress.
     * This method returns true if the countdown latch is present and has not yet reached zero.
     *
     * @return true if a countdown is in progress, false otherwise.
     */
    fun isCountdownInProgress(): Boolean {
        return connectionLatch.isPresent
    }

    /**
     * Starts a new countdown with a specified number of steps.
     * This method initializes a new CountDownLatch with `n` counts.
     *
     * @param n The number of steps before the latch reaches zero. Default is 1.
     * @throws IllegalStateException if a countdown is already in progress.
     */
    @Synchronized
    fun startNewCountdown(n: Int = 1) {
        connectionLatch.ifPresentOrElse(
            { throw IllegalStateException("Cannot startNewCountdown, countdown already started for '$description'") },
            { connectionLatch = Optional.of(CountDownLatch(n)) }
        )
    }

    /**
     * Decreases the countdown by one step.
     * This method decrements the CountDownLatch count, allowing progress when the count reaches zero.
     *
     * @throws IllegalStateException if no countdown has been started.
     */
    @Synchronized
    fun stepDown() {
        connectionLatch.ifPresentOrElse(
            { if (it.count > 0) it.countDown() },
            { throw IllegalStateException("Cannot not stepDown, countdown not started for '$description'") }
        )
    }

    /**
     * Blocks the current thread until the countdown reaches zero.
     * After awaiting completion, the countdown latch is reset to allow reuse.
     *
     * @throws IllegalStateException if no countdown has been started.
     */
    fun await() {
        connectionLatch.ifPresentOrElse(
            {
                it.await()
                connectionLatch = Optional.empty()
            },
            { throw IllegalStateException("Cannot await, countdown not started for '$description'") }
        )
    }
}
