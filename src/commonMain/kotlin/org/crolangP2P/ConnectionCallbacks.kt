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

import org.crolangP2P.errors.P2PConnectionFailedError

/**
 * Represents a communication channel.
 */
typealias Channel = String

/**
 * Map of callbacks to be called when a new P2P string message is received, keyed by channel.
 */
typealias ChannelMessageStringCallbacks = Map<Channel, (node: CrolangNode, msg: String) -> Unit>

/**
 * Map of callbacks to be called when a new P2P byte array message is received, keyed by channel.
 * The callbacks are executed asynchronously on an executor service.
 */
typealias ChannelMessageByteArrayCallbacks = Map<Channel, IncomingByteArrayMsgCallbacks>

/**
 * User-defined callbacks for a CrolangNode, common to every possible Node creation method;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onDisconnection Callback to be called when the node is disconnected.
 * @param onNewStringMsg Map of callbacks to be called when a new P2P string message is received, keyed by channel.
 * @param onNewByteArrayMsg Map of callbacks to be called when a new P2P byte array message is received, keyed by channel.
 */
abstract class BasicCrolangNodeCallbacks(
    val onDisconnection: (id: String) -> Unit,
    val onNewStringMsg: ChannelMessageStringCallbacks,
    val onNewByteArrayMsg: ChannelMessageByteArrayCallbacks
)

/**
 * User-defined callbacks for a CrolangNode that will receive byte array messages;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onNewMsgPartReceived Callback to be called when a new part of a byte array message is received.
 * @param onNewCompleteMsgReceived Callback to be called when a complete byte array message is received.
 * @param onMsgCorruption Callback to be called when a byte array message is corrupted.
 */
class IncomingByteArrayMsgCallbacks(
    val onNewMsgPartReceived: (node: CrolangNode, msgId: Int, part: Int, total: Int) -> Unit = { _, _, _, _ -> },
    val onNewCompleteMsgReceived: (node: CrolangNode, msgId: Int, msg: ByteArray) -> Unit = { _, _, _ -> },
    val onMsgCorruption: (node: CrolangNode, msgId: Int) -> Unit = { _, _ -> }
)

/**
 * User-defined callbacks for a CrolangNode that will be connected asynchronously;
 * the callbacks are executed asynchronously on an executor service.
 *
 * @param onConnectionSuccess Callback to be called when the node is successfully connected. Optional, defaults to an empty function.
 * @param onConnectionFailed Callback to be called when the node connection fails. Optional, defaults to an empty function.
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewStringMsg Map of callbacks to be called when a new P2P string message is received, keyed by channel. Optional, defaults to an empty map.
 * @param onNewByteArrayMsg Map of callbacks to be called when a new P2P byte array message is received, keyed by channel. Optional, defaults to an empty map.
 */
class OutgoingCrolangNodeCallbacks(
    val onConnectionSuccess: (node: CrolangNode) -> Unit = {},
    val onConnectionFailed: (id: String, reason: P2PConnectionFailedError) -> Unit = { _, _ -> },
    onDisconnection: (id: String) -> Unit = {},
    onNewStringMsg: ChannelMessageStringCallbacks = emptyMap(),
    onNewByteArrayMsg: ChannelMessageByteArrayCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewStringMsg, onNewByteArrayMsg)

/**
 * User-defined callbacks for a CrolangNode whose connection is initiated by another client;
 * all the callbacks are executed asynchronously on an executor service EXCEPT for the onConnectionAttempt callback.
 *
 * @param onConnectionAttempt Callback to be called when a connection attempt is made. Optional, defaults to always allowing the connection.
 * @param onConnectionSuccess Callback to be called when the node is successfully connected. Optional, defaults to an empty function.
 * @param onConnectionFailed Callback to be called when the node connection fails. Optional, defaults to an empty function.
 * @param onDisconnection Callback to be called when the node is disconnected. Optional, defaults to an empty function.
 * @param onNewStringMsg Map of callbacks to be called when a new P2P string message is received, keyed by channel. Optional, defaults to an empty map.
 * @param onNewByteArrayMsg Map of callbacks to be called when a new P2P byte array message is received, keyed by channel. Optional, defaults to an empty map.
 */
class IncomingCrolangNodesCallbacks(
    val onConnectionAttempt: (id: String, platform: String, version: String) -> Boolean = { _, _, _ -> true },
    val onConnectionSuccess: (node: CrolangNode) -> Unit = {},
    val onConnectionFailed: (id: String, reason: P2PConnectionFailedError) -> Unit = { _, _ -> },
    onDisconnection: (id: String) -> Unit = {},
    onNewStringMsg: ChannelMessageStringCallbacks = emptyMap(),
    onNewByteArrayMsg: ChannelMessageByteArrayCallbacks = emptyMap()
) : BasicCrolangNodeCallbacks(onDisconnection, onNewStringMsg, onNewByteArrayMsg)
