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

package internal.dependencies_injection

import internal.BuildConfig
import internal.dependencies.DependenciesInjection
import internal.dependencies_injection.event_loop.ConcreteEventLoopJs
import internal.dependencies_injection.executor.ConcreteExecutorProviderJs
import internal.dependencies_injection.sleep.ConcreteSleepProviderJs
import internal.dependencies_injection.socket.ConcreteCrolangP2PSocketCreatorJs
import internal.dependencies_injection.timer.ConcreteTimerProviderJs
import internal.dependencies_injection.timestamp.ConcreteTimestampProviderJs
import internal.dependencies_injection.uuid.ConcreteUUIDGeneratorJs

/**
 * JavaScript/Node.js implementation of the dependencies injection provider.
 * 
 * This object provides all platform-specific implementations required by the
 * CroLang P2P library for JavaScript/Node.js environments.
 * 
 * Note: Some implementations are currently incomplete (marked with null)
 * and need to be implemented to complete the JavaScript target support.
 */
internal object DependenciesInjectionProviderJs {

    fun getDependencies(): DependenciesInjection {
        return DependenciesInjection(
            myPlatform = BuildConfig.MY_PLATFORM,
            myVersion = BuildConfig.VERSION,
            eventLoop = ConcreteEventLoopJs(),
            socketCreator = ConcreteCrolangP2PSocketCreatorJs(),
            crolangP2PPeerConnectionFactory = null!!, // TODO: Implement ConcreteCrolangP2PPeerConnectionFactoryJs
            uuidGenerator = ConcreteUUIDGeneratorJs(),
            timestampProvider = ConcreteTimestampProviderJs(),
            timerProvider = ConcreteTimerProviderJs(),
            sleepProvider = ConcreteSleepProviderJs(),
            synchronizationProvider = null!!, // TODO: Implement ConcreteSynchronizationProviderJs
            executorProvider = ConcreteExecutorProviderJs()
        )
    }

}
