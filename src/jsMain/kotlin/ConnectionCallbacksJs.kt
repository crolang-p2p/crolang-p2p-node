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

import org.crolangP2P.CrolangNode
import org.crolangP2P.exceptions.ConnectionToNodeFailedReasonException

@OptIn(ExperimentalJsExport::class)
@JsExport
class IncomingCrolangNodesCallbacksJs {

    private var onConnectionAttempt: (id: String, platform: String, version: String) -> Boolean = { _, _, _ -> true }
    private var onConnectionSuccess: (node: CrolangNode) -> Unit = {}
    private var onConnectionFailed: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> }
    private var onDisconnection: (id: String) -> Unit = {}
    private var onNewMsq: Map<String, (CrolangNode, String) -> Unit> = emptyMap()

    fun setOnConnectionAttempt(
        callback: (id: String, platform: String, version: String) -> Boolean
    ): IncomingCrolangNodesCallbacksJs {
        onConnectionAttempt = callback
        return this
    }

    fun getOnConnectionAttempt(): (id: String, platform: String, version: String) -> Boolean {
        return onConnectionAttempt
    }

    fun setOnConnectionSuccess(callback: (node: CrolangNode) -> Unit): IncomingCrolangNodesCallbacksJs {
        onConnectionSuccess = callback
        return this
    }

    fun getOnConnectionSuccess(): (node: CrolangNode) -> Unit {
        return onConnectionSuccess
    }

    fun setOnConnectionFailed(
        callback: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit
    ): IncomingCrolangNodesCallbacksJs {
        onConnectionFailed = callback
        return this
    }

    fun getOnConnectionFailed(): (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit {
        return onConnectionFailed
    }

    fun setOnDisconnection(callback: (id: String) -> Unit): IncomingCrolangNodesCallbacksJs {
        onDisconnection = callback
        return this
    }

    fun getOnDisconnection(): (id: String) -> Unit {
        return onDisconnection
    }

    fun addOnNewMsgCallback(channel: String, callback: (CrolangNode, String) -> Unit): IncomingCrolangNodesCallbacksJs {
        onNewMsq = onNewMsq + (channel to callback)
        return this
    }

    fun getOnNewMsgCallbacks(): Map<String, (CrolangNode, String) -> Unit> {
        return onNewMsq
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
object IncomingCrolangNodesCallbacksJsBuilder {

    fun create(): IncomingCrolangNodesCallbacksJs {
        return IncomingCrolangNodesCallbacksJs()
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangNodeCallbacksJs {

    private var onConnectionSuccess: (node: CrolangNode) -> Unit = {}
    private var onConnectionFailed: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit = { _, _ -> }
    private var onDisconnection: (id: String) -> Unit = {}
    private var onNewMsq: Map<String, (CrolangNode, String) -> Unit> = emptyMap()

    fun setOnConnectionSuccess(callback: (node: CrolangNode) -> Unit): CrolangNodeCallbacksJs {
        onConnectionSuccess = callback
        return this
    }

    fun getOnConnectionSuccess(): (node: CrolangNode) -> Unit {
        return onConnectionSuccess
    }

    fun setOnConnectionFailed(callback: (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit): CrolangNodeCallbacksJs {
        onConnectionFailed = callback
        return this
    }

    fun getOnConnectionFailed(): (id: String, reason: ConnectionToNodeFailedReasonException) -> Unit {
        return onConnectionFailed
    }

    fun setOnDisconnection(callback: (id: String) -> Unit): CrolangNodeCallbacksJs {
        onDisconnection = callback
        return this
    }

    fun getOnDisconnection(): (id: String) -> Unit {
        return onDisconnection
    }

    fun addOnNewMsgCallback(channel: String, callback: (CrolangNode, String) -> Unit): CrolangNodeCallbacksJs {
        onNewMsq = onNewMsq + (channel to callback)
        return this
    }

    fun getOnNewMsgCallbacks(): Map<String, (CrolangNode, String) -> Unit> {
        return onNewMsq
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangNodeCallbacksJsBuilder {

    fun create(): CrolangNodeCallbacksJs {
        return CrolangNodeCallbacksJs()
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class CrolangNodeConnectionTargetsJs() {

    private val targets: MutableMap<String, CrolangNodeCallbacksJs> = mutableMapOf()

    fun addTarget(id: String, callbacks: CrolangNodeCallbacksJs): CrolangNodeConnectionTargetsJs {
        targets[id] = callbacks
        return this
    }

    fun getTargets(): Map<String, CrolangNodeCallbacksJs> {
        return targets
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object CrolangNodeConnectionTargetsJsBuilder {

    fun create(): CrolangNodeConnectionTargetsJs {
        return CrolangNodeConnectionTargetsJs()
    }
}
