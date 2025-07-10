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

import org.crolangP2P.ConnectionAttempt

/**
 * Represents an ongoing connection attempt to one or more remote nodes.
 * 
 * This class provides control over active connection attempts, allowing you to:
 * - Check if the connection attempt has completed
 * - Forcefully terminate ongoing connection attempts
 * 
 * Connection attempts are returned when calling connectToSingleNode or connectToMultipleNodes.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class ConnectionAttemptJs internal constructor(private val attempt: ConnectionAttempt) {

    /**
     * Checks whether this connection attempt has completed.
     * 
     * A connection attempt is considered concluded when all targeted nodes have either
     * successfully connected or failed to connect.
     * 
     * @return true if the connection attempt has finished, false if still in progress
     */
    fun isConcluded(): Boolean {
        return attempt.isConcluded()
    }

    /**
     * Forcefully terminates this connection attempt.
     * 
     * This will immediately stop any ongoing connection negotiations and trigger
     * the appropriate failure callbacks for nodes that were still connecting.
     * Already established connections will remain active.
     */
    fun forceConclusion() {
        attempt.forceConclusion()
    }

}
