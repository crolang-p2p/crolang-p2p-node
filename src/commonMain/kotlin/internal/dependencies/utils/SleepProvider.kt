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
 * Abstract contract for sleep/delay functionality.
 *
 * This abstraction allows different platforms to provide their own sleep implementation
 * while keeping the core business logic platform-agnostic.
 */
abstract class SleepProvider {
    
    /**
     * Suspends the current execution for the specified duration.
     * 
     * @param millis The duration to sleep in milliseconds
     */
    abstract fun sleep(millis: Long)
}
