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

/**
 * Extension function for the `Timer` class that schedules a one-time event to be triggered after a specified delay.
 *
 * This function takes a delay in milliseconds (`delayMs`) and a lambda function `onTimesUp` that will be called
 * once the timer finishes its countdown.
 *
 * @param delayMs The delay (in milliseconds) after which the event should be triggered.
 * @param onTimesUp The callback function to execute when the timer reaches zero.
 */
private fun Timer.scheduleEvent(delayMs: Long, onTimesUp: () -> Unit) {
    this.schedule(
        object : TimerTask() {
            // Executes the provided lambda when the timer triggers
            override fun run() {
                onTimesUp()
            }
        },
        delayMs
    )
}

/**
 * A simple timer class that encapsulates a `Timer` and schedules a one-time event after a delay.
 *
 * This class uses the `scheduleEvent` function to schedule the event and provides the ability to cancel the timer.
 */
internal class TimeoutTimer(delayMs: Long, onTimesUp: () -> Unit) {

    private val timer = Timer()

    init {
        timer.scheduleEvent(delayMs, onTimesUp)
    }

    /**
     * Cancels the timer, preventing the scheduled event from being triggered.
     */
    fun cancel() {
        timer.cancel()
    }
}
