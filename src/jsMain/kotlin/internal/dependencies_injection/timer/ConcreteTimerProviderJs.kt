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

package internal.dependencies_injection.timer

import internal.clearTimeout
import internal.dependencies.utils.TimerProvider
import internal.dependencies.utils.CancelableTimer
import internal.setTimeout

/**
 * JavaScript/Node.js implementation of TimerProvider using setTimeout/clearTimeout.
 * 
 * This implementation uses the native JavaScript timer functions to provide
 * timer functionality compatible with both browser and Node.js environments.
 */
internal class ConcreteTimerProviderJs : TimerProvider() {
    
    /**
     * Creates a new cancelable timer with the specified delay and callback.
     * 
     * This method creates a timer using JavaScript's setTimeout function that will
     * execute the provided callback after the specified delay. The returned timer
     * can be canceled before it fires.
     * 
     * @param delayMs Delay in milliseconds before the timer fires
     * @param onTimeout Callback function to execute when the timer fires
     * @return A cancelable timer instance
     */
    override fun createTimer(delayMs: Int, onTimeout: () -> Unit): CancelableTimer {
        return ConcreteJsCancelableTimer(delayMs, onTimeout)
    }
}

/**
 * JavaScript/Node.js implementation of CancelableTimer.
 * 
 * This implementation wraps the native setTimeout/clearTimeout functions
 * to provide a cancelable timer interface.
 */
internal class ConcreteJsCancelableTimer(delayMs: Int, onTimeout: () -> Unit) : CancelableTimer() {
    
    private var timerId: Int? = null
    private var isCancelled = false
    
    init {
        // JavaScript setTimeout accepts delay as Int (milliseconds)
        val delay = if (delayMs > Int.MAX_VALUE) Int.MAX_VALUE else delayMs
        
        timerId = setTimeout({
            if (!isCancelled) {
                onTimeout()
            }
        }, delay)
    }
    
    /**
     * Cancels the timer if it hasn't already fired or been canceled.
     * 
     * This method uses JavaScript's clearTimeout to cancel the underlying timer.
     * After cancellation, the timer callback will not be executed. Multiple calls
     * to cancel() are safe and have no effect after the first call.
     */
    override fun cancel() {
        if (!isCancelled && timerId != null) {
            clearTimeout(timerId!!)
            isCancelled = true
            timerId = null
        }
    }
}
