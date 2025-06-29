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

import java.util.*

/**
 * Concrete implementation of TimerProvider for JVM platform.
 * 
 * This implementation uses java.util.Timer and TimerTask to provide timer functionality.
 */
class ConcreteTimerProvider : TimerProvider() {
    
    override fun createTimer(delayMs: Long, onTimeout: () -> Unit): CancelableTimer {
        return ConcreteJvmCancelableTimer(delayMs, onTimeout)
    }
}

/**
 * Concrete implementation of CancelableTimer for JVM platform.
 * 
 * This implementation wraps java.util.Timer and TimerTask.
 */
class ConcreteJvmCancelableTimer(delayMs: Long, onTimeout: () -> Unit) : CancelableTimer() {
    
    private val timer = Timer()
    
    init {
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    onTimeout()
                }
            },
            delayMs
        )
    }
    
    override fun cancel() {
        timer.cancel()
    }
}
