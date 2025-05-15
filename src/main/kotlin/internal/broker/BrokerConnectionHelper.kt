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

package internal.broker

import internal.utils.AwaitAsyncEventGuard

/**
 * Helper class to manage synchronous connection and disconnection events for a client trying to connect or disconnect
 * from the remote server (Broker). This ensures that the caller has a synchronous view of the connection and disconnection.
 *
 * It uses `AwaitAsyncEventGuard` to wait for the connection or disconnection to complete before proceeding.
 */
internal class BrokerConnectionHelper {

    /**
     * Error that occurred during the connection to the Broker
     */
    var connectionToBrokerErrorReason: ConnectionToBrokerErrorReason? = null

    /**
     * Guard used to synchronize the connection to the Broker (waiting for the connection process to complete)
     */
    val connectionToBrokerGuard: AwaitAsyncEventGuard = AwaitAsyncEventGuard("connection to Broker")

    /**
     * Guard used to synchronize the disconnection from the Broker (waiting for the disconnection process to complete)
     */
    val disconnectionFromBrokerGuard: AwaitAsyncEventGuard = AwaitAsyncEventGuard("disconnection from Broker")

    /**
     * This method decrements the countdown for the connection to the Broker, indicating that the connection
     * process has progressed. It also updates the `error` property with the provided error if any.
     *
     * @param error The error that occurred during the connection to the Broker, if any.
     */
    fun countDownConnectionLatch(error: ConnectionToBrokerErrorReason? = null) {
        this.connectionToBrokerErrorReason = error
        // Decreases the countdown for the connection guard, allowing the caller to proceed after the connection completes
        connectionToBrokerGuard.stepDown()
    }
}
