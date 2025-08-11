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

import internal.dependencies_injection.DependenciesInjectionProviderJs
import internal.dependencies_injection.webrtc.setupWebRTCPolyfill
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.crolangP2P.BrokerConnectionAdditionalParameters
import org.crolangP2P.BrokerLifecycleCallbacks
import org.crolangP2P.CoreCrolangP2PFacade
import org.crolangP2P.CrolangNode
import org.crolangP2P.CrolangSettings
import org.crolangP2P.IncomingCrolangNodesCallbacks
import org.crolangP2P.LoggingOptions
import org.crolangP2P.OutgoingCrolangNodeCallbacks
import org.crolangP2P.errors.P2PConnectionFailedError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Main entry point for the CrolangP2P library in JavaScript/Node.js environments.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangP2PJs {

    private val coreFacade = CoreCrolangP2PFacade(DependenciesInjectionProviderJs.getDependencies())
    
    // Initialize WebRTC polyfill automatically
    init {
        setupWebRTCPolyfill()
    }

    /**
     * Checks if the local node is currently connected to the Broker.
     * 
     * @return true if connected to the Broker, false otherwise
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun isLocalNodeConnectedToBroker(): kotlin.js.Promise<Boolean> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.isLocalNodeConnectedToBroker{ continuation.resume(it) }
        } }
    }

    /**
     * Connects to a CrolangP2P broker server.
     * 
     * This establishes the signaling connection required for P2P node discovery and connection setup.
     * Once connected to a broker, you can connect to other nodes or allow incoming connections.
     * 
     * @param brokerAddress The WebSocket address of the broker
     * @param nodeId Your unique node identifier
     * @param onNewSocketMsg Callbacks for handling broker messages
     * @param additionalParameters Connection configuration (callbacks, settings, logging)
     * @return Promise that resolves when connection is established
     */
    fun connectToBroker(
        brokerAddress: String,
        nodeId: String,
        onNewSocketMsg: OnNewSocketMsgJs,
        additionalParameters: BrokerConnectionAdditionalParametersJs
    ): kotlin.js.Promise<CrolangP2PJs> {
        return connectToBrokerWithAuthentication(
            brokerAddress,
            nodeId,
            onConnectionAttemptData = "",
            onNewSocketMsg = onNewSocketMsg,
            additionalParameters = additionalParameters
        )
    }

    /**
     * Connects to a CrolangP2P broker server with authentication data.
     * 
     * This method allows you to provide authentication data that will be sent
     * to the broker's authentication webhook (if configured).
     * 
     * @param brokerAddress The WebSocket address of the broker
     * @param nodeId Your unique node identifier
     * @param onConnectionAttemptData Authentication data to send to the broker
     * @param onNewSocketMsg Callbacks for handling broker messages
     * @param additionalParameters Connection configuration
     * @return Promise that resolves when connection is established
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun connectToBrokerWithAuthentication(
        brokerAddress: String,
        nodeId: String,
        onConnectionAttemptData: String,
        onNewSocketMsg: OnNewSocketMsgJs,
        additionalParameters: BrokerConnectionAdditionalParametersJs
    ): kotlin.js.Promise<CrolangP2PJs> {
        val additionalParametersKotlin = BrokerConnectionAdditionalParameters(
            lifecycleCallbacks = BrokerLifecycleCallbacks(
                onInvoluntaryDisconnection = additionalParameters.getLifecycleCallbacks().getOnInvoluntaryDisconnection(),
                onReconnectionAttempt = additionalParameters.getLifecycleCallbacks().getOnReconnectionAttempt(),
                onSuccessfullyReconnected = additionalParameters.getLifecycleCallbacks().getOnSuccessfullyReconnected()
            ),
            settings = CrolangSettings(
                p2pConnectionTimeoutMillis = additionalParameters.getSettings().getP2pConnectionTimeoutMillis(),
                multipartP2PMessageTimeoutMillis = additionalParameters.getSettings().getMultipartP2PMessageTimeoutMillis(),
                reconnection = additionalParameters.getSettings().isReconnectionEnabled(),
                maxReconnectionAttempts = additionalParameters.getSettings().getMaxReconnectionAttempts(),
                reconnectionAttemptsDeltaMs = additionalParameters.getSettings().getReconnectionAttemptsDeltaMs()
            ),
            logging = LoggingOptions(
                enableBaseLogging = additionalParameters.getLogging().isBaseLoggingEnabled(),
                enableDebugLogging = additionalParameters.getLogging().isDebugLoggingEnabled()
            )
        )
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.connectToBroker(
                brokerAddress,
                nodeId,
                onConnectionAttemptData = onConnectionAttemptData,
                onNewSocketMsg = onNewSocketMsg.getListeners(),
                additionalParameters = additionalParametersKotlin,
                onSuccess = { continuation.resume(CrolangP2PJs) },
                onError = { continuation.resumeWithException(Exception(it.toString())) }
            )
        } }
    }

    /**
     * Disconnects from the current broker.
     * 
     * Currently active P2P connections will NOT be closed.
     * After disconnection, you cannot connect to new nodes until reconnecting to a broker.
     * 
     * @return Promise that resolves when disconnection is complete
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun disconnectFromBroker(): kotlin.js.Promise<CrolangP2PJs> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.disconnectFromBroker(
                onSuccess = { continuation.resume(CrolangP2PJs) },
                onError = { continuation.resumeWithException(Exception(it.toString())) }
            )
        } }
    }

    /**
     * Checks if this node is currently accepting incoming connections.
     * 
     * @return true if incoming connections are allowed, false otherwise
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun areIncomingConnectionsAllowed(): kotlin.js.Promise<Boolean> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.areIncomingConnectionsAllowed{ continuation.resume(it) }
        } }
    }

    /**
     * Sends a message to a remote node through the broker.
     * 
     * This sends messages via the broker's signaling connection rather than
     * direct P2P.
     * 
     * @param id The target node's identifier
     * @param channel The message channel/topic
     * @param msg The message content
     * @return Promise that resolves when message is sent
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun sendSocketMsg(id: String, channel: String, msg: String): kotlin.js.Promise<CrolangP2PJs> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.sendSocketMsg(
                id,
                channel,
                msg,
                onMsgSent = { continuation.resume(CrolangP2PJs) },
                onError = { continuation.resumeWithException(Exception(it.toString())) }
            )
        } }
    }

    /**
     * Checks the broker connection status of multiple remote nodes.
     * 
     * @param ids Array of node identifiers to check
     * @return Promise resolving to array of connection status objects
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun areRemoteNodesConnectedToBroker(ids: Array<String>): kotlin.js.Promise<Array<NodeConnectionStatusJs>> {
        return GlobalScope.promise {
            suspendCoroutine { continuation ->
                coreFacade.areRemoteNodesConnectedToBroker(
                    ids.toSet(),
                    onResult = { result ->
                        continuation.resume(result.map { (nodeId, isConnected) -> NodeConnectionStatusJs(nodeId, isConnected) }.toTypedArray())
                    },
                    onError = { error ->
                        continuation.resumeWithException(Exception(error.toString()))
                    }
                )
            }
        }
    }

    /**
     * Checks if a specific remote node is connected to the broker.
     * 
     * @param id The node identifier to check
     * @return Promise resolving to true if the node is connected, false otherwise
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun isRemoteNodeConnectedToBroker(id: String): kotlin.js.Promise<Boolean> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.isRemoteNodeConnectedToBroker(
                id,
                onResult = { continuation.resume(it) },
                onError = { continuation.resumeWithException(Exception(it.toString())) }
            )
        } }
    }

    /**
     * Enables incoming P2P connections with specified callbacks.
     * 
     * After calling this method, other nodes can connect to this node.
     * The provided callbacks will handle connection attempts and events.
     * 
     * @param callbacks Configuration for handling incoming connection events
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun allowIncomingConnections(callbacks: IncomingCrolangNodesCallbacksJs): kotlin.js.Promise<CrolangP2PJs> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.allowIncomingConnections(
                IncomingCrolangNodesCallbacks(
                    onConnectionAttempt = callbacks.getOnConnectionAttempt(),
                    onConnectionSuccess = { callbacks.getOnConnectionSuccess().invoke(CrolangNodeJs(it)) },
                    onConnectionFailed = { id, reason -> callbacks.getOnConnectionFailed().invoke(id, reason) },
                    onDisconnection = callbacks.getOnDisconnection(),
                    onNewMsg = callbacks.getOnNewMsgCallbacks().mapValues { (_, callback) ->
                        { node: CrolangNode, msg: String -> callback(CrolangNodeJs(node), msg) }
                    }
                ),
                onSuccess = { continuation.resume(CrolangP2PJs) },
                onError = { continuation.resumeWithException(Exception(it.toString())) }
            )
        } }
    }

    /**
     * Disables incoming P2P connections.
     * 
     * After calling this method, other nodes will not be able to connect to this node.
     * Existing connections remain active.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun stopIncomingConnections(): kotlin.js.Promise<CrolangP2PJs> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.stopIncomingConnections { continuation.resume(CrolangP2PJs) }
        } }
    }

    /**
     * Gets all currently connected P2P nodes.
     * 
     * @return Array of connected node instances
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun getAllConnectedNodes(): kotlin.js.Promise<Array<CrolangNodeJs>> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.getAllConnectedNodes{ connectedNodes ->
                continuation.resume(connectedNodes.map { CrolangNodeJs(it.value) }.toTypedArray())
            }
        } }
    }

    /**
     * Gets a specific connected node by its identifier.
     * 
     * @param id The node identifier to look for
     * @return The connected node instance, or null if not found/connected
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun getConnectedNode(id: String): kotlin.js.Promise<CrolangNodeJs?> {
        return GlobalScope.promise { suspendCoroutine { continuation ->
            coreFacade.getConnectedNode(id){
                continuation.resume(it?.let { CrolangNodeJs(it) })
            }
        } }
    }

    /**
     * Initiates a P2P connection to a single remote node.
     * 
     * This establishes a direct WebRTC connection to the specified node.
     * The connection process is asynchronous and status updates are provided via callbacks.
     * 
     * @param id The target node's unique identifier
     * @param callbacks Handlers for connection events (success, failure, messages)
     * @return Connection attempt object for monitoring and control
     */
    fun connectToSingleNode(id: String, callbacks: CrolangNodeCallbacksJs): ConnectionAttemptJs {
        return ConnectionAttemptJs(coreFacade.connectToSingleNode(id, OutgoingCrolangNodeCallbacks(
            onConnectionSuccess = { callbacks.getOnConnectionSuccess().invoke(CrolangNodeJs(it)) },
            onConnectionFailed = { onConnectionFailedId, reason -> callbacks.getOnConnectionFailed().invoke(
                onConnectionFailedId, reason
            ) },
            onDisconnection = callbacks.getOnDisconnection(),
            onNewMsg = callbacks.getOnNewMsgCallbacks().mapValues { (_, callback) ->
                { node: CrolangNode, msg: String -> callback(CrolangNodeJs(node), msg) }
            }
        )))
    }

    /**
     * Initiates P2P connections to multiple remote nodes simultaneously.
     * 
     * This establishes direct WebRTC connections to all specified nodes concurrently.
     * Each target node can have its own set of callbacks for handling events.
     * 
     * @param targets Configuration specifying target nodes and their respective callbacks
     * @param onConnectionAttemptConcluded Callback invoked when the connection attempt concludes, returning a ConnectionAttemptResultJs
     * @return Connection attempt object for monitoring and controlling all connections
     *
     * @see ConnectionAttemptResultJs
     */
    fun connectToMultipleNodes(
        targets: CrolangNodeConnectionTargetsJs,
        onConnectionAttemptConcluded: (result: ConnectionAttemptResultJs) -> Unit
    ): ConnectionAttemptJs {
        return ConnectionAttemptJs(coreFacade.connectToMultipleNodes(
            targets = targets.getTargets().mapValues { target -> OutgoingCrolangNodeCallbacks(
                onConnectionSuccess = { target.value.getOnConnectionSuccess().invoke(CrolangNodeJs(it)) },
                onConnectionFailed = { id, reason -> target.value.getOnConnectionFailed().invoke(id, reason) },
                onDisconnection = target.value.getOnDisconnection(),
                onNewMsg = target.value.getOnNewMsgCallbacks().mapValues { (_, callback) ->
                    { node: CrolangNode, msg: String -> callback(CrolangNodeJs(node), msg) }
                }
            ) },
            onConnectionAttemptConcluded = { connected, failed ->
                onConnectionAttemptConcluded(ConnectionAttemptResultJs(
                    connectedNodes = connected.map { CrolangNodeJs(it.value) }.toTypedArray(),
                    failedNodes = failed.map { ConnectionAttemptFailedNodeJs(it.key, it.value) }.toTypedArray()
                ))
            }
        ))
    }

}
