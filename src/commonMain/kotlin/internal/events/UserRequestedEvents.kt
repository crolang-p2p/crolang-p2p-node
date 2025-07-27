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

package internal.events

import internal.broker.BrokerSocketCreator.createSocket
import internal.broker.OnConnectionToBrokerSettings
import internal.dependencies.event_loop.Event
import internal.events.data.AreNodesConnectedToBrokerMsg
import internal.events.data.AreNodesConnectedToBrokerMsgResponse
import internal.events.data.ParsableSocketDirectMsg
import internal.events.data.SocketDirectMsg
import internal.events.data.abstractions.SocketMsgType.Companion.ARE_NODES_CONNECTED_TO_BROKER
import internal.events.data.abstractions.SocketMsgType.Companion.SOCKET_MSG_EXCHANGE
import internal.events.data.abstractions.SocketResponses
import internal.node.NodeState
import internal.utils.BrokerMsgExtractor.extractMessageFromPayload
import internal.utils.CrolangLogger
import internal.utils.SharedStore
import internal.utils.SharedStore.brokerLifecycleCallbacks
import internal.utils.SharedStore.dependencies
import internal.utils.SharedStore.disconnectAllResponderNodesNotConnected
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.incomingCrolangNodesCallbacks
import internal.utils.SharedStore.localNodeId
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.onConnectionToBrokerSettings
import internal.utils.SharedStore.onNewSocketMsgCallbacks
import internal.utils.SharedStore.parser
import internal.utils.SharedStore.performingConnectionToBrokerRequestedByUser
import internal.utils.SharedStore.settings
import internal.utils.SharedStore.socket
import internal.utils.SharedStore.userCallbackSuccessfulDisconnection
import org.crolangP2P.BrokerConnectionAdditionalParameters
import org.crolangP2P.CrolangNode
import org.crolangP2P.IncomingCrolangNodesCallbacks
import org.crolangP2P.errors.AllowIncomingConnectionsError
import org.crolangP2P.errors.ConnectionToBrokerError
import org.crolangP2P.errors.DisconnectionFromBrokerError
import org.crolangP2P.errors.RemoteNodesConnectionStatusCheckError
import org.crolangP2P.errors.SendSocketMsgError

/**
 * Checks if the local node is connected to the Crolang Broker.
 * This method returns true if the socket is present and connected, false otherwise.
 *
 * @return true if the local node is connected to the Broker, false otherwise.
 */
internal fun isLocalNodeConnectedToBroker(): Boolean {
    return socket != null && socket!!.connected()
}

internal class OnConnectToBrokerRequested(
    private val brokerAddr: String,
    private val nodeId: String,
    private val onSuccess: () -> Unit,
    private val onError: (err: ConnectionToBrokerError) -> Unit,
    private val onConnectionAttemptData: String,
    private val onNewSocketMsg: Map<String, (from: String, msg: String) -> Unit>,
    private val additionalParameters: BrokerConnectionAdditionalParameters
): Event {
    override fun process() {
        if(socket != null){
            logger.regularErr("trying to connect to Broker while already connected")
            onError(ConnectionToBrokerError.LOCAL_CLIENT_ALREADY_CONNECTED)
            return
        } else if (performingConnectionToBrokerRequestedByUser){
            logger.regularErr("trying to connect to Broker while already performing a connection")
            onError(ConnectionToBrokerError.ALREADY_PERFORMING_CONNECTION)
            return
        }
        performingConnectionToBrokerRequestedByUser = true
        onNewSocketMsgCallbacks = onNewSocketMsg
        brokerLifecycleCallbacks = additionalParameters.lifecycleCallbacks
        logger = CrolangLogger(additionalParameters.logging)
        settings = additionalParameters.settings
        logger.regularInfo("initiating Broker connection attempt")
        localNodeId = nodeId
        onConnectionToBrokerSettings = OnConnectionToBrokerSettings(brokerAddr, onConnectionAttemptData)
        val socket = createSocket(
            onSuccess = onSuccess,
            onError = { err: ConnectionToBrokerError ->
                logger.regularErr("failed to connect to Broker")
                onConnectionToBrokerSettings = null
                SharedStore.flush()
                executeCallbackOnExecutor {
                    onError(err)
                }
            }

        )
        SharedStore.socket = socket
        socket.connect()
    }
}

internal class OnDisconnectFromBrokerRequested(
    private val onSuccess: () -> Unit,
    private val onError: (err: DisconnectionFromBrokerError) -> Unit
): Event {
    override fun process() {
        logger.regularInfo("initiating disconnection from Broker")
        if(socket == null){
            logger.regularInfo("already disconnected from Broker")
            executeCallbackOnExecutor {
                onError(DisconnectionFromBrokerError.ALREADY_DISCONNECTED)
            }
            return
        } else if (userCallbackSuccessfulDisconnection != null){
            logger.regularErr("already performing disconnection from Broker")
            executeCallbackOnExecutor {
                onError(DisconnectionFromBrokerError.ALREADY_PERFORMING_DISCONNECTION)
            }
            return
        }
        userCallbackSuccessfulDisconnection = onSuccess
        socket!!.disconnect()
    }
}

internal class OnAreRemoteNodesConnectedToBrokerRequested(
    private val ids: Set<String>,
    private val onResult: (resultMap: Map<String, Boolean>) -> Unit,
    private val onError: (err: RemoteNodesConnectionStatusCheckError) -> Unit
) : Event {
    override fun process() {
        if(!isLocalNodeConnectedToBroker()){
            executeCallbackOnExecutor {
                onError(RemoteNodesConnectionStatusCheckError.NOT_CONNECTED_TO_BROKER)
            }
        } else if(ids.isEmpty()){
            executeCallbackOnExecutor {
                onResult(emptyMap())
            }
        }
        socket!!.emit(ARE_NODES_CONNECTED_TO_BROKER, parser.toJson(AreNodesConnectedToBrokerMsg(ids))) {
            dependencies!!.eventLoop.postEvent(OnAreRemoteNodesConnectedToBrokerResponse(it, onResult, onError))
        }
    }
}

internal class OnAreRemoteNodesConnectedToBrokerResponse(
    private val args: Array<out Any>,
    private val onResult: (resultMap: Map<String, Boolean>) -> Unit,
    private val onError: (err: RemoteNodesConnectionStatusCheckError) -> Unit
): Event {
    override fun process() {
        val extracted = extractMessageFromPayload(args)
        if (extracted == null) {
            executeCallbackOnExecutor {
                onError(RemoteNodesConnectionStatusCheckError.UNKNOWN_ERROR)
            }
            return
        }
        val res = parser.fromJson<AreNodesConnectedToBrokerMsgResponse>(extracted)
        if(res?.results != null && res.results!!.all { it.id != null && it.connected != null }){
            executeCallbackOnExecutor {
                onResult(res.results!!.associate { it.id!! to it.connected!! })
            }
        } else {
            executeCallbackOnExecutor {
                onError(RemoteNodesConnectionStatusCheckError.UNKNOWN_ERROR)
            }
        }
    }
}

internal class OnSendSocketMsgRequested(
    private val id: String,
    private val channel: String,
    private val msg: String?,
    private val onMsgSent: () -> Unit,
    private val onError: (err: SendSocketMsgError) -> Unit
) : Event {
    override fun process() {
        if(!isLocalNodeConnectedToBroker()){
            executeCallbackOnExecutor {
                onError(SendSocketMsgError.NOT_CONNECTED_TO_BROKER)
            }
        } else if (channel.isEmpty()){
            executeCallbackOnExecutor {
                onError(SendSocketMsgError.EMPTY_CHANNEL)
            }
        } else if(id.isEmpty()){
            executeCallbackOnExecutor {
                onError(SendSocketMsgError.EMPTY_ID)
            }
        } else if(id === localNodeId){
            executeCallbackOnExecutor {
                onError(SendSocketMsgError.TRIED_TO_SEND_MSG_TO_SELF)
            }
        } else {
            socket!!.emit(
                SOCKET_MSG_EXCHANGE,
                parser.toJson(ParsableSocketDirectMsg.fromChecked(SocketDirectMsg(localNodeId, id, channel, msg ?: "")))
            ) {
                dependencies!!.eventLoop.postEvent(OnSendSocketMsgResponse(it, onMsgSent, onError))
            }
        }
    }
}

internal class OnSendSocketMsgResponse(
    private val args: Array<out Any>,
    private val onMsgSent: () -> Unit,
    private val onError: (err: SendSocketMsgError) -> Unit
): Event {
    override fun process() {
        if (args.size != 1 || args[0] !is String) {
            executeCallbackOnExecutor {
                onError(SendSocketMsgError.fromMessage(SocketResponses.ERROR))
            }
            return
        }
        val response = args[0] as String
        if (SocketResponses.ALL.contains(response)) {
            if (SocketResponses.isOk(response)) {
                executeCallbackOnExecutor {
                    onMsgSent()
                }
            } else {
                executeCallbackOnExecutor {
                    onError(SendSocketMsgError.fromMessage(response))
                }
            }
        } else {
            executeCallbackOnExecutor {
                onError(SendSocketMsgError.fromMessage(SocketResponses.ERROR))
            }
        }
    }
}

internal class OnAllowIncomingConnectionsRequested(
    private val callbacks: IncomingCrolangNodesCallbacks,
    private val onSuccess: () -> Unit,
    private val onError: (err: AllowIncomingConnectionsError) -> Unit
): Event {
    override fun process() {
        if (socket == null || !socket!!.connected()) {
            logger.regularErr("cannot allow incoming connections: not connected to the Crolang Broker")
            executeCallbackOnExecutor {
                onError(AllowIncomingConnectionsError.NOT_CONNECTED_TO_BROKER)
            }
        } else if (incomingCrolangNodesCallbacks != null) {
            logger.regularErr("cannot allow incoming connections: incoming connections already allowed")
            executeCallbackOnExecutor {
                onError(AllowIncomingConnectionsError.INCOMING_CONNECTIONS_ALREADY_ALLOWED)
            }
        } else {
            incomingCrolangNodesCallbacks = callbacks
            logger.regularInfo("incoming connections are now allowed")
            executeCallbackOnExecutor {
                onSuccess()
            }
        }
    }
}

internal class OnIsLocalNodeConnectedToBrokerRequested(
    private val onResult: (isConnected: Boolean) -> Unit
): Event {
    override fun process() {
        executeCallbackOnExecutor {
            onResult(isLocalNodeConnectedToBroker())
        }
    }
}

internal class OnAreIncomingConnectionsAllowedRequested(
    private val onResult: (areAllowed: Boolean) -> Unit
): Event {
    override fun process() {
        executeCallbackOnExecutor {
            onResult(SharedStore.areIncomingConnectionsAllowed())
        }
    }
}

internal class OnGetAllConnectedNodesRequested(
    private val onResult: (connectedNodes: Map<String, CrolangNode>) -> Unit
): Event {
    override fun process() {
        val initiators = SharedStore.brokerPeersContainer.responderNodes.values
            .filter { it.state == NodeState.CONNECTED }
            .map { it.crolangNode }
        val responders = SharedStore.brokerPeersContainer.initiatorNodes.values
            .filter { it.state == NodeState.CONNECTED }
            .map { it.crolangNode }
        executeCallbackOnExecutor {
            onResult((initiators + responders).associateBy { it.id })
        }
    }

}

internal class OnGetConnectedNodeRequested(
    private val id: String, private val onResult: (CrolangNode?) -> Unit
): Event {
    override fun process() {
        val initiator = SharedStore.brokerPeersContainer.responderNodes[id]
        if(initiator != null && initiator.state == NodeState.CONNECTED){
            executeCallbackOnExecutor {
                onResult(initiator.crolangNode)
            }
        } else {
            val responder = SharedStore.brokerPeersContainer.initiatorNodes[id]
            if(responder != null && responder.state == NodeState.CONNECTED){
                executeCallbackOnExecutor {
                    onResult(responder.crolangNode)
                }
            } else {
                executeCallbackOnExecutor {
                    onResult(null)
                }
            }
        }
    }

}

internal class OnStopIncomingConnectionsRequested(
    private val onStopped: () -> Unit
): Event {
    override fun process() {
        if(socket == null){
            logger.regularErr("cannot stop incoming connections: not connected to the Crolang Broker")
        } else if(incomingCrolangNodesCallbacks == null){
            logger.regularErr("cannot stop incoming connections: incoming connections already stopped")
        }
        incomingCrolangNodesCallbacks = null
        disconnectAllResponderNodesNotConnected()
        logger.regularInfo("incoming connections are now stopped")
        executeCallbackOnExecutor {
            onStopped()
        }
    }
}
