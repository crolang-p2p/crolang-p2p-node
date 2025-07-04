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
    
    override fun createTimer(delayMs: Long, onTimeout: () -> Unit): CancelableTimer {
        return ConcreteJsCancelableTimer(delayMs, onTimeout)
    }
}

/**
 * JavaScript/Node.js implementation of CancelableTimer.
 * 
 * This implementation wraps the native setTimeout/clearTimeout functions
 * to provide a cancelable timer interface.
 */
internal class ConcreteJsCancelableTimer(delayMs: Long, onTimeout: () -> Unit) : CancelableTimer() {
    
    private var timerId: Int? = null
    private var isCancelled = false
    
    init {
        // JavaScript setTimeout accepts delay as Int (milliseconds)
        // Convert Long to Int, capping at Int.MAX_VALUE for very large delays
        val delay = if (delayMs > Int.MAX_VALUE) Int.MAX_VALUE else delayMs.toInt()
        
        timerId = setTimeout({
            if (!isCancelled) {
                onTimeout()
            }
        }, delay)
    }
    
    override fun cancel() {
        if (!isCancelled && timerId != null) {
            clearTimeout(timerId!!)
            isCancelled = true
            timerId = null
        }
    }
}
