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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import java.util.*

/**
 * Utility class for JSON serialization and deserialization using Gson.
 *
 * This class provides methods to convert objects to JSON and parse JSON strings into objects.
 */
internal class JsonParser {

    // Gson instance used for JSON parsing and serialization
    private val gson: Gson = GsonBuilder().create()

    /**
     * Parses a JSON string into an object of the specified type.
     *
     * @param msg The JSON string to be parsed.
     * @param of The class type to parse the JSON into.
     * @return An Optional containing the parsed object if successful, or an empty Optional if parsing fails.
     */
    fun <T> fromJson(msg: String, of: Class<T>): Optional<T & Any> {
        return try {
            val parsed: T = gson.fromJson(msg, of)
            if (parsed == null) {
                Optional.empty()
            } else {
                Optional.of(parsed)
            }
        } catch (e: JsonParseException) {
            Optional.empty() // Returns an empty Optional if JSON parsing fails
        }
    }

    /**
     * Converts an object into its JSON string representation.
     *
     * @param element The object to serialize into JSON.
     * @return A JSON string representation of the object.
     */
    fun toJson(element: Any): String {
        return gson.toJson(element)
    }
}
