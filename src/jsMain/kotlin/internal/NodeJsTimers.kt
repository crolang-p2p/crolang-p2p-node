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

package internal

import kotlin.js.js

/**
 * Pure JavaScript timer functions that work in both browser and Node.js environments.
 * These functions use the global JavaScript timer APIs that are available in all JS environments.
 */

/**
 * Schedules callback to be executed on the next iteration of the event loop.
 * Uses setTimeout with 0 delay as a cross-platform equivalent to setImmediate.
 */
fun setImmediate(callback: () -> Unit): Int {
    return js("setTimeout")(callback, 0)
}

/**
 * Schedules callback to be executed after delay milliseconds.
 * Uses the global JavaScript setTimeout function.
 */
fun setTimeout(callback: () -> Unit, delay: Int): Int {
    return js("setTimeout")(callback, delay)
}

/**
 * Cancels a timer that was previously created with setTimeout().
 * Uses the global JavaScript clearTimeout function.
 */
fun clearTimeout(timeoutId: Int) {
    js("clearTimeout")(timeoutId)
}

/**
 * Cancels a timer that was previously created with setImmediate().
 * Uses clearTimeout since setImmediate is implemented as setTimeout(0).
 */
fun clearImmediate(immediateId: Int) {
    js("clearTimeout")(immediateId)
}
