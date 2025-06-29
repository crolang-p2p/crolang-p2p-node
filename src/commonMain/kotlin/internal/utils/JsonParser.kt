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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility class for JSON serialization and deserialization using Kotlinx Serialization.
 *
 * This class provides methods to convert objects to JSON and parse JSON strings into objects.
 */
internal class JsonParser {

    // Json instance used for JSON parsing and serialization
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parses a JSON string into an object of the specified type (new reified version).
     *
     * @param msg The JSON string to be parsed.
     * @return A nullable containing the parsed object if successful, nulll if parsing fails.
     */
    inline fun <reified T : Any> fromJson(msg: String): T? {
        return try {
            val parsed: T = json.decodeFromString<T>(msg)
            parsed
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Converts an object into its JSON string representation.
     *
     * @param element The object to serialize into JSON.
     * @return A JSON string representation of the object.
     */
    inline fun <reified T> toJson(element: T): String {
        return json.encodeToString(element)
    }
}
