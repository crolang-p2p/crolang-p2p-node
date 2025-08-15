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

package internal.dependencies.strings_deserialization

/**
 * MemoryCrashStringDetector is an interface that provides a method to prevent memory crashes
 * when parsing strings from incoming byte arrays.
 *
 * This is particularly useful in scenarios where the byte array may contain  excessively large data
 * that could lead to memory issues during string deserialization in the particular target platform.
 */
interface MemoryCrashStringDetector {

    /**
     * Prevents incoming byte array from causing memory crash in string parsing.
     *
     * @param payload The byte array to be checked.
     * @return true if the byte array is safe to parse as a string, false otherwise.
     */
    fun preventIncomingByteArrayFromCausingMemoryCrashInStringParsing(payload: ByteArray): Boolean

}
