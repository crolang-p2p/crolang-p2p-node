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

package internal.dependencies_injection.strings_deserialization

import internal.dependencies.strings_deserialization.MemoryCrashStringDetector

/**
 * MemoryCrashStringDetectorJvm is a JVM implementation of the MemoryCrashStringDetector interface.
 * It prevents memory crashes when parsing strings from incoming byte arrays by always returning false,
 * indicating that all byte arrays can be parsed to strings.
 */
internal class DependenciesInjectionProviderJvm: MemoryCrashStringDetector {

    override fun preventIncomingByteArrayFromCausingMemoryCrashInStringParsing(payload: ByteArray): Boolean {
        return false
    }

}
