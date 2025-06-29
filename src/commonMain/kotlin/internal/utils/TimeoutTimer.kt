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

import internal.dependencies.utils.CancelableTimer

/**
 * A simple timer class that encapsulates a platform-agnostic timer via dependency injection.
 *
 * This class uses the injected TimerProvider to create platform-specific timers while
 * keeping the core business logic platform-agnostic.
 */
internal class TimeoutTimer(delayMs: Long, onTimesUp: () -> Unit) {

    private val timer: CancelableTimer = SharedStore.dependencies!!.timerProvider.createTimer(delayMs, onTimesUp)

    /**
     * Cancels the timer, preventing the scheduled event from being triggered.
     */
    fun cancel() {
        timer.cancel()
    }
}
