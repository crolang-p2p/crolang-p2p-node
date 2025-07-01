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

@file:JsModule("timers")
@file:JsNonModule

package internal.utils

/**
 * External declarations for Node.js timer functions.
 * These provide access to Node.js setImmediate and other timer APIs.
 */

/**
 * Schedules callback to be executed on the next iteration of the Node.js event loop.
 * This is equivalent to Node.js setImmediate() function.
 */
external fun setImmediate(callback: () -> Unit): dynamic

/**
 * Schedules callback to be executed after delay milliseconds.
 * This is equivalent to Node.js setTimeout() function.
 */
external fun setTimeout(callback: () -> Unit, delay: Int): dynamic

/**
 * Cancels a timer that was previously created with setTimeout().
 */
external fun clearTimeout(timeoutId: dynamic)

/**
 * Cancels a timer that was previously created with setImmediate().
 */
external fun clearImmediate(immediateId: dynamic)
