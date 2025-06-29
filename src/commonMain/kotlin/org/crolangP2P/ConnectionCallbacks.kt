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

import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException

/**
 * Represents a communication channel.
 */
typealias Channel = String

/**
 * Map of callbacks to be called when a new P2P message is received, keyed by channel.
 */
typealias ChannelMessageCallbacks = Map<Channel, (node: CrolangNode, msg: String) -> Unit>

/**
 * User-defined callbacks for a CrolangNode, common to every possible Node creation method;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onDisconnection Callback to be called when the node is disconnected.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel.
 */
abstract class BasicCrolangNodeCallbacks(
    val onDisconnection: (id: String) -> Unit,
    val onNewMsg: ChannelMessageCallbacks
)

/**
 * User-defined callbacks for a CrolangNode that will be connected synchronously;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel. Optional, defaults to an empty map.
 */
class SyncCrolangNodeCallbacks(
    onDisconnection: (id: String) -> Unit = {},
    onNewMsg: ChannelMessageCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewMsg)

/**
 * User-defined callbacks for a CrolangNode that will be connected asynchronously;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onConnectionSuccess Callback to be called when the node is successfully connected. Optional, defaults to an empty function.
 * @param onConnectionFailed Callback to be called when the node connection fails. Optional, defaults to an empty function.
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel. Optional, defaults to an empty map.
 */
class AsyncCrolangNodeCallbacks(
    val onConnectionSuccess: (node: CrolangNode) -> Unit = {},
    val onConnectionFailed: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> },
    onDisconnection: (id: String) -> Unit = {},
    onNewMsg: ChannelMessageCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewMsg)

/**
 * User-defined callbacks for a CrolangNode whose connection is initiated by another client;
 * all the callbacks are executed asynchronously on an executor service EXCEPT for the onConnectionAttempt callback.
 *
 * @param onConnectionAttempt Callback to be called when a connection attempt is made. Optional, defaults to always allowing the connection.
 * @param onConnectionSuccess Callback to be called when the node is successfully connected. Optional, defaults to an empty function.
 * @param onConnectionFailed Callback to be called when the node connection fails. Optional, defaults to an empty function.
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewMsg Map of callbacks to be called when a new P2P message is received, keyed by channel. Optional, defaults to an empty map.
 */
class IncomingCrolangNodesCallbacks(
    val onConnectionAttempt: (id: String, platform: String, version: String) -> Boolean = { _, _, _ -> true },
    val onConnectionSuccess: (node: CrolangNode) -> Unit = {},
    val onConnectionFailed: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> },
    onDisconnection: (id: String) -> Unit = {},
    onNewMsg: ChannelMessageCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewMsg)
