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
 * Abstract class representing a direct message with a sender and recipient CrolangNode IDs.
 *
 * @property platformFrom The platform from which the message was sent.
 * @property versionFrom The version of the platform from which the message was sent.
 * @property from The ID of the sender.
 * @property to The ID of the recipient.
 * @property sessionId The ID of the session associated with the message, used to avoid edge cases of multiple connection attempts.
 */
internal abstract class DirectMsg(
    val platformFrom: String, val versionFrom: String, val from: String, val to: String, val sessionId: String
)
