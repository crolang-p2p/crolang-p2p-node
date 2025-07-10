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

/**
 * Callbacks for handling broker connection lifecycle events.
 * 
 * This class allows you to configure handlers for various broker connection events:
 * - Involuntary disconnections (network issues, server errors)
 * - Reconnection attempts when auto-reconnection is enabled
 * - Successful reconnections after temporary disconnections
 * 
 * Use [BrokerLifecycleCallbacksJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class BrokerLifecycleCallbacksJs {
    
    private var onInvoluntaryDisconnection: (Any) -> Unit = { }
    private var onReconnectionAttempt: () -> Unit = { }
    private var onSuccessfullyReconnected: () -> Unit = { }
    
    /**
     * Sets the callback for involuntary disconnection events.
     * 
     * This callback is triggered when the connection to the broker is lost unexpectedly,
     * such as due to network issues or server errors.
     * 
     * @param callback Function to call when an involuntary disconnection occurs
     * @return This instance for method chaining
     */
    fun setOnInvoluntaryDisconnection(callback: (Any) -> Unit): BrokerLifecycleCallbacksJs {
        this.onInvoluntaryDisconnection = callback
        return this
    }

    /**
     * Gets the currently configured involuntary disconnection callback.
     * 
     * @return The configured callback function
     */
    fun getOnInvoluntaryDisconnection(): (Any) -> Unit {
        return onInvoluntaryDisconnection
    }
    
    /**
     * Sets the callback for reconnection attempts.
     * 
     * This callback is triggered when the library attempts to reconnect to the broker
     * after an involuntary disconnection, if auto-reconnection is enabled.
     * 
     * @param callback Function to call when a reconnection attempt starts
     * @return This instance for method chaining
     */
    fun setOnReconnectionAttempt(callback: () -> Unit): BrokerLifecycleCallbacksJs {
        this.onReconnectionAttempt = callback
        return this
    }

    /**
     * Gets the currently configured reconnection attempt callback.
     * 
     * @return The configured callback function
     */
    fun getOnReconnectionAttempt(): () -> Unit {
        return onReconnectionAttempt
    }
    
    /**
     * Sets the callback for successful reconnection events.
     * 
     * This callback is triggered when the connection to the broker is successfully
     * re-established after an involuntary disconnection.
     * 
     * @param callback Function to call when reconnection succeeds
     * @return This instance for method chaining
     */
    fun setOnSuccessfullyReconnected(callback: () -> Unit): BrokerLifecycleCallbacksJs {
        this.onSuccessfullyReconnected = callback
        return this
    }

    /**
     * Gets the currently configured successful reconnection callback.
     * 
     * @return The configured callback function
     */
    fun getOnSuccessfullyReconnected(): () -> Unit {
        return onSuccessfullyReconnected
    }

}

/**
 * Builder for creating [BrokerLifecycleCallbacksJs] instances.
 * 
 * This builder provides a convenient way to create lifecycle callback objects
 * with default no-op implementations.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object BrokerLifecycleCallbacksJsBuilder {

    /**
     * Creates a new [BrokerLifecycleCallbacksJs] instance with default callbacks.
     * 
     * @return A new callback configuration instance ready for customization
     */
    fun create(): BrokerLifecycleCallbacksJs {
        return BrokerLifecycleCallbacksJs()
    }

}
