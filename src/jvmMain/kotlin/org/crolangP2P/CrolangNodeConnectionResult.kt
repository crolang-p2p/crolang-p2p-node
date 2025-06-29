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

package org.crolangP2P

import java.util.Optional

/**
 * Represents the result of a connection attempt to a Crolang node.
 *
 * @property node The connected Crolang node, if the connection was successful.
 * @property exception The reason for the connection failure, if applicable.
 *
 * @see CrolangP2P.Java.connectToMultipleNodesSync
 * @see CrolangP2P.Java.connectToMultipleNodesAsync
 */
class CrolangNodeConnectionResult(
    val node: Optional<CrolangNode>,
    val exception: Optional<P2PConnectionFailedReason>
)
