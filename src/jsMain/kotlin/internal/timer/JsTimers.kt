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

package internal.timer

/**
 * External declarations for JavaScript/Node.js timer functions.
 * 
 * These functions provide access to the native JavaScript timer API
 * for timeout and interval management.
 */

/**
 * Sets a timer which executes a function after the timer expires.
 * 
 * @param callback The function to be executed after the timer expires
 * @param delay The time, in milliseconds, the timer should wait before executing the function
 * @return A numeric ID which can be used to cancel the timer
 */
external fun setTimeout(callback: () -> Unit, delay: Int): Int

/**
 * Cancels a timeout previously established by calling setTimeout().
 * 
 * @param timeoutId The identifier of the timeout to cancel
 */
external fun clearTimeout(timeoutId: Int)
