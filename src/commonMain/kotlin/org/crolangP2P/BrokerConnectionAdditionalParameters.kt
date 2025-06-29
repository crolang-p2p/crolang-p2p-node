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

/**
 * Class representing additional parameters for a Broker connection.
 *
 * @property lifecycleCallbacks Callbacks for various lifecycle events of the Broker reconnection/disconnection.
 * @property settings Settings for the Crolang P2P library.
 * @property logging Logging options for the Crolang P2P library.
 */
class BrokerConnectionAdditionalParameters constructor(

    /**
     * Callbacks for various lifecycle events of the Broker reconnection/disconnection.
     */
    val lifecycleCallbacks: BrokerLifecycleCallbacks = BrokerLifecycleCallbacks(),

    /**
     * Settings for the Crolang P2P library.
     */
    val settings: CrolangSettings = CrolangSettings(),

    /**
     * Logging options for the Crolang library.
     */
    val logging: LoggingOptions = LoggingOptions()
)
