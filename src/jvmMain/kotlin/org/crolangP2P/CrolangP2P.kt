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

import internal.BuildConfig
import internal.RuntimeDependencyResolver
import internal.webrtc.ConcreteCrolangP2PPeerConnectionFactory
import internal.socket.ConcreteCrolangP2PSocketCreator
import internal.event_loop.ConcreteEventLoop
import internal.utils.ConcreteTimestampProvider
import internal.utils.ConcreteUUIDGenerator
import internal.dependencies.utils.ConcreteTimerProvider
import internal.dependencies.utils.ConcreteSleepProvider
import internal.dependencies.utils.ConcreteSynchronizationProvider
import internal.dependencies.utils.ConcreteExecutorProvider
import internal.dependencies.DependenciesInjection
import org.crolangP2P.CrolangP2P.Kotlin

/**
 * CrolangP2P is a singleton object that manages the connection to the Crolang Broker and allows for connecting to
 * remote CrolangNodes.
 *
 * Contains both a Kotlin and Java interface, allowing for easy integration with both languages.
 *
 * @see Kotlin
 * @see Java
 */
object CrolangP2P {

    init {
        RuntimeDependencyResolver.loadDependency()
    }

    /**
     * The Kotlin interface for CrolangP2P.
     */
    val Kotlin = CoreCrolangP2PFacadeKotlin(DependenciesInjection(
        BuildConfig.MY_PLATFORM,
        BuildConfig.VERSION,
        ConcreteEventLoop(),
        ConcreteCrolangP2PSocketCreator(),
        ConcreteCrolangP2PPeerConnectionFactory(),
        ConcreteUUIDGenerator(),
        ConcreteTimestampProvider(),
        ConcreteTimerProvider(),
        ConcreteSleepProvider(),
        ConcreteSynchronizationProvider(),
        ConcreteExecutorProvider()
    ))

    /**
     * The Java interface for CrolangP2P.
     */
    val Java = CoreCrolangP2PFacadeJava(Kotlin)

}
