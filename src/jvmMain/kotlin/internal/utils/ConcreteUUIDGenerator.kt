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

package internal.utils

import internal.dependencies.utils.UUIDGenerator
import java.util.UUID

/**
 * JVM implementation of UUIDGenerator using java.util.UUID.
 * 
 * This implementation provides UUID generation functionality for JVM platforms
 * using the standard Java UUID.randomUUID() method.
 */
internal class ConcreteUUIDGenerator : UUIDGenerator() {
    
    /**
     * Generates a new random UUID using java.util.UUID.randomUUID().
     * 
     * @return A randomly generated UUID string in standard format
     */
    override fun generateRandomUUID(): String {
        return UUID.randomUUID().toString()
    }
    
}
