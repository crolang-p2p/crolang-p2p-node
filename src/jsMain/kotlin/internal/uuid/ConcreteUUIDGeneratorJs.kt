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

package internal.uuid

import internal.dependencies.utils.UUIDGenerator

/**
 * JavaScript/Node.js implementation of UUIDGenerator using Node.js crypto module.
 * 
 * This implementation provides UUID generation functionality for JavaScript/Node.js platforms
 * using the native crypto.randomUUID() method available in Node.js v14.17.0+.
 */
internal class ConcreteUUIDGeneratorJs : UUIDGenerator() {
    
    /**
     * Generates a new random UUID using Node.js crypto.randomUUID().
     * 
     * @return A randomly generated UUID string in standard format (e.g., "123e4567-e89b-12d3-a456-426614174000")
     */
    override fun generateRandomUUID(): String {
        return randomUUID()
    }
}
