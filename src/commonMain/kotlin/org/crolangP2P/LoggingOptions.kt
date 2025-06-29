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

package org.crolangP2P

/**
 * Options for logging in the Crolang library.
 *
 * @property enableBaseLogging If true, enables base logging for the Crolang library; base logs involve connections, messages received, etc...
 * @property enableDebugLogging If true, enables debug logging for the Crolang library; debug logging is more verbose and includes detailed information about the internal workings of the library.
 */
class LoggingOptions constructor(
    /**
     * If true, enables base logging for the Crolang library; base logs involve connections, messages received, etc...
     */
    val enableBaseLogging: Boolean = false,

    /**
     * If true, enables debug logging for the Crolang library; debug logging is more verbose and includes detailed information about the internal workings of the library.
     */
    val enableDebugLogging: Boolean = false
)
