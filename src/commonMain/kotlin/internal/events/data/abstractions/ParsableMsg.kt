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

package internal.events.data.abstractions

/**
 * This abstract class represents a parsable message that can be sent either via socket or P2P.
 * It must be parsed first and then converted into a checked message with a validated structure.
 *
 * @param C The type of the checked message that will be returned by the `toChecked` method.
 */
internal abstract class ParsableMsg<C> {

    /**
     * Abstract method to convert the parsed message into a checked and validated message of type C.
     *
     * @return A nullable containing the checked message, or null if the message is invalid or cannot be checked.
     */
    abstract fun toChecked(): C?
}
