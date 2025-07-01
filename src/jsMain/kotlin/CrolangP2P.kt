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
object CrolangP2P {

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
    ): kotlin.js.Promise<CrolangP2P> {
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
    ): kotlin.js.Promise<CrolangP2P> {
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
            CrolangP2P
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun disconnectFromBroker(): kotlin.js.Promise<CrolangP2P> {
        return GlobalScope.promise {
            coreFacade.disconnectFromBroker()
            CrolangP2P
        }
    }

    fun areIncomingConnectionsAllowed(): Boolean {
        return coreFacade.areIncomingConnectionsAllowed()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendSocketMsg(id: String, channel: String, msg: String): kotlin.js.Promise<CrolangP2P> {
        return GlobalScope.promise {
            coreFacade.sendSocketMsg(id, channel, msg) //TODO handle exceptions properly
            CrolangP2P
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun areRemoteNodesConnectedToBroker(ids: Array<String>): kotlin.js.Promise<Array<NodeConnectionStatus>>{
        return GlobalScope.promise {
            return@promise coreFacade.areRemoteNodesConnectedToBroker(ids.toSet())
                .getOrElse { throw it }
                .map { (nodeId, isConnected) -> NodeConnectionStatus(nodeId, isConnected) }
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
            onConnectionSuccess = callbacks.getOnConnectionSuccess(),
            onConnectionFailed = callbacks.getOnConnectionFailed(),
            onDisconnection = callbacks.getOnDisconnection(),
            onNewMsg = callbacks.getOnNewMsgCallbacks()
        ))
    }

    fun stopIncomingConnections() {
        coreFacade.stopIncomingConnections()
    }

    fun getAllConnectedNodes(): Array<CrolangNode> {
        return coreFacade.getAllConnectedNodes().map { it.value }.toTypedArray()
    }

    fun getConnectedNode(id: String): CrolangNode? {
        return coreFacade.getConnectedNode(id)
    }

    fun connectToSingleNode(id: String, callbacks: CrolangNodeCallbacksJs): ConnectionAttempt {
        return coreFacade.connectToSingleNodeAsync(id, AsyncCrolangNodeCallbacks(
            onConnectionSuccess = callbacks.getOnConnectionSuccess(),
            onConnectionFailed = callbacks.getOnConnectionFailed(),
            onDisconnection = callbacks.getOnDisconnection(),
            onNewMsg = callbacks.getOnNewMsgCallbacks()
        ))
    }

    fun connectToMultipleNodes(targets: CrolangNodeConnectionTargetsJs): ConnectionAttempt {
        return coreFacade.connectToMultipleNodesAsync(
            targets.getTargets().mapValues { AsyncCrolangNodeCallbacks(
                onConnectionSuccess = it.value.getOnConnectionSuccess(),
                onConnectionFailed = it.value.getOnConnectionFailed(),
                onDisconnection = it.value.getOnDisconnection(),
                onNewMsg = it.value.getOnNewMsgCallbacks()
            ) }
        )
    }

}
