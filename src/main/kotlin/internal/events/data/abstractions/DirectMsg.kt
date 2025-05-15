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

import com.google.gson.annotations.SerializedName

/**
 * Abstract class representing a parsable direct message.
 * This class extends [ParsableMsg] and adds the properties required for a direct message, such as the sender and receiver.
 * The sender and receiver are represented by the IDs of CrolangNode instances.
 *
 * @param C The type of the checked message returned by the `toChecked` method, which extends the functionality of [ParsableMsg].
 */
internal abstract class ParsableDirectMsg<C> : ParsableMsg<C>() {

    /**
     * The ID of the sender.
     * This property is serialized with the name "from" when the message is converted to JSON.
     */
    @SerializedName("from") var from: String? = null

    /**
     * The ID of the recipient.
     * This property is serialized with the name "to" when the message is converted to JSON.
     */
    @SerializedName("to") var to: String? = null

    /**
     * The ID of the session associated with the message.
     * This property is serialized with the name "sessionId" when the message is converted to JSON.
     * Used to avoid edge cases of multiple connection attempts.
     */
    @SerializedName("sessionId") var sessionId: String? = null
}

/**
 * Abstract class representing a direct message with a sender and recipient CrolangNode IDs.
 *
 * @property from The ID of the sender.
 * @property to The ID of the recipient.
 * @property sessionId The ID of the session associated with the message, used to avoid edge cases of multiple connection attempts.
 */
internal abstract class DirectMsg(val from: String, val to: String, val sessionId: String)
