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

package internal.events.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Message sent to the Broker to check if the specified Nodes are connected to the Broker.
 */
@Serializable
internal class AreNodesConnectedToBrokerMsg(val ids: Set<String>)

/**
 * Response message containing the results of the connection check for the specified Nodes.
 *
 * @property results A list of results indicating whether each Node is connected to the Broker.
 */
@Serializable
internal class AreNodesConnectedToBrokerMsgResponse {
    @SerialName("results") var results: List<AreNodesConnectedToBrokerResult>? = null
}

/**
 * Result of the connection check for a specific Node.
 *
 * @property id The ID of the Node.
 * @property connected Indicates whether the Node is connected to the Broker.
 */
@Serializable
internal class AreNodesConnectedToBrokerResult {
    @SerialName("id") var id: String? = null
    @SerialName("connected") var connected: Boolean? = null
}
