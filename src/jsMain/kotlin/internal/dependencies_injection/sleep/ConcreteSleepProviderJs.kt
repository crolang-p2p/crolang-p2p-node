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

package internal.dependencies_injection.sleep

import internal.dependencies.utils.SleepProvider

/**
 * External declaration for JavaScript Date constructor.
 * Using the same approach as TimestampProvider.
 */
external class Date() {
    fun getTime(): Double
}

/**
 * JavaScript/Node.js implementation of SleepProvider.
 * 
 * Note: JavaScript is inherently single-threaded and asynchronous, so true blocking
 * sleep like JVM's Thread.sleep() is not possible or recommended. This implementation
 * uses a busy-wait approach for API compatibility, but should be used sparingly.
 * 
 * In JavaScript environments, asynchronous patterns with Promises and async/await
 * should be preferred over blocking sleep operations.
 */
internal class ConcreteSleepProvider : SleepProvider() {
    
    override fun sleep(millis: Long) {
        if (millis <= 0) return
        
        // JavaScript implementation of sleep using busy-wait with time checking
        // Get the current time in milliseconds using Date.getTime()
        val startTime = Date().getTime()
        val endTime = startTime + millis
        
        // Loop until the specified time has passed
        while (Date().getTime() < endTime) {
            // Continue looping until time elapsed
            // This creates a blocking behavior similar to Thread.sleep()
            // Note: This is a busy-wait and will consume CPU cycles, but it is called with very short durations
            // so it should not significantly impact performance in most cases.
        }
    }
}
