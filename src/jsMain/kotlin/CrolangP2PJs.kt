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

import ConnectionToNodeFailedReasonJs.Converter.fromConnectionToNodeFailedReasonException
import internal.dependencies_injection.DependenciesInjectionProviderJs
import internal.dependencies_injection.webrtc.setupWebRTCPolyfill
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.crolangP2P.AsyncCrolangNodeCallbacks
import org.crolangP2P.BrokerConnectionAdditionalParameters
import org.crolangP2P.BrokerLifecycleCallbacks
import org.crolangP2P.ConnectionAttempt
import org.crolangP2P.CoreCrolangP2PFacade
import org.crolangP2P.CrolangNode
import org.crolangP2P.CrolangSettings
import org.crolangP2P.IncomingCrolangNodesCallbacks
import org.crolangP2P.LoggingOptions

@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangP2PJs {

    private val coreFacade = CoreCrolangP2PFacade(DependenciesInjectionProviderJs.getDependencies())
    
    // Initialize WebRTC polyfill automatically
    init {
        setupWebRTCPolyfill()
    }

    fun isLocalNodeConnectedToBroker(): Boolean {
        return coreFacade.isLocalNodeConnectedToBroker()
    }

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
        return GlobalScope.promise {
            coreFacade.connectToBroker(
                brokerAddress,
                nodeId,
                onConnectionAttemptData = onConnectionAttemptData,
                onNewSocketMsg = onNewSocketMsg.getListeners(),
                additionalParameters = additionalParametersKotlin
            ).getOrThrow()
            CrolangP2PJs
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun disconnectFromBroker(): kotlin.js.Promise<CrolangP2PJs> {
        return GlobalScope.promise {
            coreFacade.disconnectFromBroker()
            CrolangP2PJs
        }
    }

    fun areIncomingConnectionsAllowed(): Boolean {
        return coreFacade.areIncomingConnectionsAllowed()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendSocketMsg(id: String, channel: String, msg: String): kotlin.js.Promise<CrolangP2PJs> {
        return GlobalScope.promise {
            coreFacade.sendSocketMsg(id, channel, msg) //TODO handle exceptions properly
            CrolangP2PJs
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun areRemoteNodesConnectedToBroker(ids: Array<String>): kotlin.js.Promise<Array<NodeConnectionStatusJs>>{
        return GlobalScope.promise {
            return@promise coreFacade.areRemoteNodesConnectedToBroker(ids.toSet())
                .getOrElse { throw it }
                .map { (nodeId, isConnected) -> NodeConnectionStatusJs(nodeId, isConnected) }
                .toTypedArray()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun isRemoteNodeConnectedToBroker(id: String): kotlin.js.Promise<Boolean> {
        return GlobalScope.promise {
            return@promise coreFacade.isRemoteNodeConnectedToBroker(id).getOrElse { throw it }
        }
    }

    fun allowIncomingConnections(callbacks: IncomingCrolangNodesCallbacksJs) {
        coreFacade.allowIncomingConnections(IncomingCrolangNodesCallbacks(
            onConnectionAttempt = callbacks.getOnConnectionAttempt(),
            onConnectionSuccess = { callbacks.getOnConnectionSuccess().invoke(CrolangNodeJs(it)) },
            onConnectionFailed = { id, reason ->
                callbacks.getOnConnectionFailed().invoke(id, fromConnectionToNodeFailedReasonException(reason))
            },
            onDisconnection = callbacks.getOnDisconnection(),
            onNewMsg = callbacks.getOnNewMsgCallbacks().mapValues { (_, callback) ->
                { node: CrolangNode, msg: String -> callback(CrolangNodeJs(node), msg) }
            }
        ))
    }

    fun stopIncomingConnections() {
        coreFacade.stopIncomingConnections()
    }

    fun getAllConnectedNodes(): Array<CrolangNodeJs> {
        return coreFacade.getAllConnectedNodes().map { CrolangNodeJs(it.value) }.toTypedArray()
    }

    fun getConnectedNode(id: String): CrolangNodeJs? {
        return coreFacade.getConnectedNode(id)?.let { CrolangNodeJs(it) }
    }

    fun connectToSingleNode(id: String, callbacks: CrolangNodeCallbacksJs): ConnectionAttemptJs {
        return ConnectionAttemptJs(coreFacade.connectToSingleNodeAsync(id, AsyncCrolangNodeCallbacks(
            onConnectionSuccess = { callbacks.getOnConnectionSuccess().invoke(CrolangNodeJs(it)) },
            onConnectionFailed = { onConnectionFailedId, reason -> callbacks.getOnConnectionFailed().invoke(
                onConnectionFailedId, fromConnectionToNodeFailedReasonException(reason)
            ) },
            onDisconnection = callbacks.getOnDisconnection(),
            onNewMsg = callbacks.getOnNewMsgCallbacks().mapValues { (_, callback) ->
                { node: CrolangNode, msg: String -> callback(CrolangNodeJs(node), msg) }
            }
        )))
    }

    fun connectToMultipleNodes(targets: CrolangNodeConnectionTargetsJs): ConnectionAttemptJs {
        return ConnectionAttemptJs(coreFacade.connectToMultipleNodesAsync(
            targets.getTargets().mapValues { target -> AsyncCrolangNodeCallbacks(
                onConnectionSuccess = { target.value.getOnConnectionSuccess().invoke(CrolangNodeJs(it)) },
                onConnectionFailed = { id, reason ->
                    target.value.getOnConnectionFailed().invoke(id, fromConnectionToNodeFailedReasonException(reason))
                },
                onDisconnection = target.value.getOnDisconnection(),
                onNewMsg = target.value.getOnNewMsgCallbacks().mapValues { (_, callback) ->
                    { node: CrolangNode, msg: String -> callback(CrolangNodeJs(node), msg) }
                }
            ) }
        ))
    }

}
