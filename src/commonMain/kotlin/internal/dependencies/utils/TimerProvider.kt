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
 * Abstract contract for timer functionality.
 *
 * This abstraction allows different platforms to provide their own timer implementation
 * while keeping the core business logic platform-agnostic.
 */
abstract class TimerProvider {
    
    /**
     * Creates a new timer that will execute the given callback after the specified delay.
     * 
     * @param delayMs The delay in milliseconds after which the callback should be executed
     * @param onTimeout The callback function to execute when the timer expires
     * @return A CancelableTimer instance that can be used to cancel the timer
     */
    abstract fun createTimer(delayMs: Long, onTimeout: () -> Unit): CancelableTimer
}

/**
 * Abstract contract for a cancelable timer.
 * 
 * This interface provides the ability to cancel a scheduled timer operation.
 */
abstract class CancelableTimer {
    
    /**
     * Cancels the timer, preventing the scheduled callback from being executed.
     */
    abstract fun cancel()
}
