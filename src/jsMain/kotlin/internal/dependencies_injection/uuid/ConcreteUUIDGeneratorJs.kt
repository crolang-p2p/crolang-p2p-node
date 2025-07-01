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

package internal.dependencies_injection.uuid

import internal.dependencies.utils.UUIDGenerator
import com.benasher44.uuid.uuid4

/**
 * JavaScript implementation of UUIDGenerator using pure KMP UUID library.
 * 
 * This implementation provides UUID generation functionality for JavaScript platforms
 * using the com.benasher44.uuid library, which is a pure Kotlin Multiplatform UUID 
 * implementation that works on all platforms without Node.js dependencies.
 * 
 * This avoids the need for Node.js crypto module polyfills in browser environments.
 */
internal class ConcreteUUIDGeneratorJs : UUIDGenerator() {
    
    /**
     * Generates a new random UUID using pure KMP uuid4().
     * 
     * @return A randomly generated UUID string in standard format (e.g., "123e4567-e89b-12d3-a456-426614174000")
     */
    override fun generateRandomUUID(): String {
        return uuid4().toString()
    }
}
