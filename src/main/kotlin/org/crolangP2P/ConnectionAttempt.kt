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

import internal.broker.BrokerConnectionAttemptClosedReason
import internal.events.OnInitiatorNodeReadyToCreateOffer
import internal.node.InitiatorNode
import internal.node.NodeState
import internal.utils.EventLoop
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.brokerPeersContainer
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.rtcConfiguration
import internal.utils.SharedStore.socketIO
import java.util.*

/**
 * Returned by an asynchronous connection attempt to a remote Node.
 * Allows to check whether the connection attempt is concluded and to forcefully close the connection attempt.
 *
 * @see CrolangP2P.connectToSingleNodeAsync
 * @see CrolangP2P.connectToMultipleNodesSync
 */
class ConnectionAttempt(
    private val targets: Map<String, AsyncCrolangNodeCallbacks>,
    private val onConnectionAttemptConcluded: (result: Map<String, Result<CrolangNode>>) -> Unit
) {
    private val connectionAttemptInitiators: MutableMap<String, InitiatorNode> = mutableMapOf()
    private val failedConnectionPeers: MutableMap<String, P2PConnectionFailedReason> = mutableMapOf()
    private var isConcluded: Boolean = false
    private var missingNodesCountdown = targets.size

    init {
        if(socketIO.isEmpty || !socketIO.get().connected()){
            for(pair in targets){
                logger.regularErr("tried to connect to ${pair.key} while not connected to broker")
                failedConnectionPeers[pair.key] = P2PConnectionFailedReason.LOCAL_NODE_NOT_CONNECTED_TO_BROKER
                executeCallbackOnExecutor {
                    pair.value.onConnectionFailed(pair.key, P2PConnectionFailedReason.LOCAL_NODE_NOT_CONNECTED_TO_BROKER.toConnectionToNodeFailedReasonException())
                }
            }
            conclude(BrokerConnectionAttemptClosedReason.NOT_CONNECTED_TO_BROKER)
        } else if(missingNodesCountdown == 0){
            conclude(BrokerConnectionAttemptClosedReason.ALL_NODES_HANDLED)
        } else {
            rtcConfiguration.ifPresentOrElse(
                { rtcConfiguration ->
                    val idsToContact: MutableSet<String> = mutableSetOf()
                    val idsToCountdown: MutableSet<String> = mutableSetOf()
                    val sessionId: String = UUID.randomUUID().toString()
                    targets.forEach { pair ->
                        if(pair.key == localNodeId){
                            logger.regularErr("tried to connect to self")
                            failedConnectionPeers[pair.key] = P2PConnectionFailedReason.TRIED_TO_CONNECT_TO_SELF
                            executeCallbackOnExecutor {
                                pair.value.onConnectionFailed(pair.key, P2PConnectionFailedReason.TRIED_TO_CONNECT_TO_SELF.toConnectionToNodeFailedReasonException())
                            }
                            idsToCountdown.add(pair.key)
                        } else if(brokerPeersContainer.initiatorNodes.containsKey(pair.key) || brokerPeersContainer.responderNodes.containsKey(pair.key)){
                            logger.regularErr("tried to connect to already contacted node ${pair.key}")
                            failedConnectionPeers[pair.key] = P2PConnectionFailedReason.ALREADY_CONNECTED_TO_REMOTE_NODE
                            executeCallbackOnExecutor {
                                pair.value.onConnectionFailed(pair.key, P2PConnectionFailedReason.ALREADY_CONNECTED_TO_REMOTE_NODE.toConnectionToNodeFailedReasonException())
                            }
                            idsToCountdown.add(pair.key)
                        } else {
                            logger.debugInfo("created local InitiatorNode ${pair.key}")
                            val initiatorNode = InitiatorNode(
                                rtcConfiguration,
                                remoteNodeId = pair.key,
                                sessionId,
                                asyncCrolangNodeCallbacks = pair.value,
                                connectionAttemptInitiators,
                                failedConnectionPeers
                            ){
                                countdown()
                            }
                            brokerPeersContainer.initiatorNodes[pair.key] = initiatorNode
                            connectionAttemptInitiators[pair.key] = initiatorNode
                            idsToContact.add(pair.key)
                        }
                    }
                    // Wait for every node to be instantiated before creating events and stepping down
                    idsToContact.forEach { id ->
                        EventLoop.postEvent(OnInitiatorNodeReadyToCreateOffer(id))
                    }
                    for(id in idsToCountdown){
                        countdown()
                    }
                },
                {
                    conclude(BrokerConnectionAttemptClosedReason.NOT_CONNECTED_TO_BROKER)
                }
            )
        }
    }

    private fun countdown(){
        if(--missingNodesCountdown == 0){
            conclude(BrokerConnectionAttemptClosedReason.ALL_NODES_HANDLED)
        }
    }

    /**
     * @return true if the connection attempt is concluded (every node specified is resolved), false otherwise
     */
    fun isConcluded(): Boolean {
        return isConcluded
    }

    /**
     * Forcefully closes the connection attempt and notifies the user.
     * This method should be used when the user wants to cancel the connection attempt.
     */
    fun forceConclusion() {
        conclude(BrokerConnectionAttemptClosedReason.CLOSED_BY_USER)
    }

    private fun conclude(reason: BrokerConnectionAttemptClosedReason) {
        if(isConcluded){
            return
        }
        isConcluded = true
        executeCallbackOnExecutor {
            when(reason){
                BrokerConnectionAttemptClosedReason.ALL_NODES_HANDLED -> {
                    onConnectionAttemptConcluded(targets.mapValues { (key, _) ->
                        if(failedConnectionPeers.containsKey(key)){
                            Result.failure(failedConnectionPeers[key]!!.toConnectionToNodeFailedReasonException())
                        } else if (brokerPeersContainer.initiatorNodes.containsKey(key)) {
                            Result.success(brokerPeersContainer.initiatorNodes[key]!!.crolangNode)
                        } else {
                            Result.failure(P2PConnectionFailedReason.CONNECTION_NEGOTIATION_ERROR.toConnectionToNodeFailedReasonException())
                        }
                    })
                }
                BrokerConnectionAttemptClosedReason.NOT_CONNECTED_TO_BROKER -> {
                    onConnectionAttemptConcluded(targets.mapValues {
                        Result.failure(P2PConnectionFailedReason.LOCAL_NODE_NOT_CONNECTED_TO_BROKER.toConnectionToNodeFailedReasonException())
                    })
                }
                BrokerConnectionAttemptClosedReason.CLOSED_BY_USER -> {
                    onConnectionAttemptConcluded(targets.mapValues { (key, _) ->
                        if(failedConnectionPeers.containsKey(key)){
                            Result.failure(failedConnectionPeers[key]!!.toConnectionToNodeFailedReasonException())
                        } else if (brokerPeersContainer.initiatorNodes.containsKey(key)) {
                            val initiatorNode = brokerPeersContainer.initiatorNodes[key]!!
                            if(initiatorNode.state == NodeState.CONNECTED){
                                Result.success(brokerPeersContainer.initiatorNodes[key]!!.crolangNode)
                            } else {
                                initiatorNode.connectionTimeoutTimer.cancel()
                                initiatorNode.forceClose(NodeState.DISCONNECTED)
                                Result.failure(P2PConnectionFailedReason.CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY.toConnectionToNodeFailedReasonException())
                            }
                        } else {
                            Result.failure(P2PConnectionFailedReason.CONNECTION_ATTEMPT_CLOSED_BY_USER_FORCEFULLY.toConnectionToNodeFailedReasonException())
                        }
                    })
                }
            }
        }
    }

}
