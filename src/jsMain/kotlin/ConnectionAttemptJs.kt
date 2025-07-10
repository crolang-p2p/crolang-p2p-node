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

@OptIn(ExperimentalJsExport::class)
@JsExport
class ConnectionAttemptJs internal constructor(private val attempt: ConnectionAttempt) {

    fun isConcluded(): Boolean {
        return attempt.isConcluded()
    }

    fun forceConclusion() {
        attempt.forceConclusion()
    }

}
