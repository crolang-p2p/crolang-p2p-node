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

import internal.node.AbstractNode
import internal.node.NodeState
import internal.utils.SharedStore.logger

/**
 * Represents the P2P connection with another remote Node in the Broker.
 */
class CrolangNode private constructor(private val abstractNode: AbstractNode) {

    /**
     * The ID of the remote Node.
     */
    val id: String = abstractNode.remoteNodeId

    /**
     * The platform of the remote Node.
     */
    val platform: String = abstractNode.remotePlatform

    /**
     * The version of the remote Node.
     */
    val version: String = abstractNode.remoteVersion

    /**
     * Sends a P2P message to the remote Node in the specified channel.
     *
     * @param channel The channel to send the message to.
     * @param msg The message to send.
     *
     * @return true if the message was sent successfully, false otherwise.
     *
     * @see IncomingCrolangNodesCallbacks.onNewMsg
     * @see SyncCrolangNodeCallbacks.onNewMsg
     * @see AsyncCrolangNodeCallbacks.onNewMsg
     */
    fun send(channel: String, msg: String): Boolean {
        return abstractNode.sendP2PMsg(channel, msg)
    }

    /**
     * Sends an empty P2P message to the remote Node in the specified channel.
     *
     * @param channel The channel to send the message to.
     *
     * @return true if the message was sent successfully, false otherwise.
     *
     * @see IncomingCrolangNodesCallbacks.onNewMsg
     * @see SyncCrolangNodeCallbacks.onNewMsg
     * @see AsyncCrolangNodeCallbacks.onNewMsg
     */
    fun send(channel: String): Boolean {
        return abstractNode.sendP2PMsg(channel, "")
    }

    /**
     * Returns the state of the Node.
     *
     * @see [CrolangNodeState]
     */
    fun getState(): CrolangNodeState {
        return if(abstractNode.isNegotiating(abstractNode.state)){
            CrolangNodeState.CONNECTING
        } else if(abstractNode.state == NodeState.CONNECTED){
            CrolangNodeState.CONNECTED
        } else {
            CrolangNodeState.DISCONNECTED
        }
    }

    /**
     * Disconnects from the remote Node.
     *
     * @see IncomingCrolangNodesCallbacks.onDisconnection
     * @see SyncCrolangNodeCallbacks.onDisconnection
     * @see AsyncCrolangNodeCallbacks.onDisconnection
     */
    fun disconnect() {
        if(abstractNode.forceClose(NodeState.DISCONNECTED)){
            logger.regularInfo("Disconnected from Node ${abstractNode.remoteNodeId} successfully")
        } else {
            logger.regularErr("Node ${abstractNode.remoteNodeId} was already disconnected")
        }
    }

    internal companion object {
        fun create(abstractNode: AbstractNode): CrolangNode {
            return CrolangNode(abstractNode)
        }
    }

}

/**
 * Represents the state of a CrolangNode.
 */
enum class CrolangNodeState {
    /**
     * The Node is in the process of connecting to the other Node.
     */
    CONNECTING,

    /**
     * The Node is connected to the other Node.
     */
    CONNECTED,

    /**
     * The Node is disconnected from the other Node.
     */
    DISCONNECTED
}
