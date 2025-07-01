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

@OptIn(ExperimentalJsExport::class)
@JsExport
class BrokerLifecycleCallbacksJs {
    
    private var onInvoluntaryDisconnection: (Any) -> Unit = { }
    private var onReconnectionAttempt: () -> Unit = { }
    private var onSuccessfullyReconnected: () -> Unit = { }
    
    /**
     * Sets the callback for involuntary disconnection.
     */
    fun setOnInvoluntaryDisconnection(callback: (Any) -> Unit): BrokerLifecycleCallbacksJs {
        this.onInvoluntaryDisconnection = callback
        return this
    }

    fun getOnInvoluntaryDisconnection(): (Any) -> Unit {
        return onInvoluntaryDisconnection
    }
    
    /**
     * Sets the callback for reconnection attempts.
     */
    fun setOnReconnectionAttempt(callback: () -> Unit): BrokerLifecycleCallbacksJs {
        this.onReconnectionAttempt = callback
        return this
    }

    fun getOnReconnectionAttempt(): () -> Unit {
        return onReconnectionAttempt
    }
    
    /**
     * Sets the callback for successful reconnection.
     */
    fun setOnSuccessfullyReconnected(callback: () -> Unit): BrokerLifecycleCallbacksJs {
        this.onSuccessfullyReconnected = callback
        return this
    }

    fun getOnSuccessfullyReconnected(): () -> Unit {
        return onSuccessfullyReconnected
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
object BrokerLifecycleCallbacksJsBuilder {

    fun create(): BrokerLifecycleCallbacksJs {
        return BrokerLifecycleCallbacksJs()
    }

}
