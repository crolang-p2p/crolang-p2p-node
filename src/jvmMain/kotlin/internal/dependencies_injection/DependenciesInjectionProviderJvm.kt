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
import internal.dependencies_injection.event_loop.ConcreteEventLoopJvm
import internal.dependencies_injection.executor.ConcreteExecutorProviderJvm
import internal.dependencies_injection.sleep.ConcreteSleepProviderJvm
import internal.dependencies_injection.socket.ConcreteCrolangP2PSocketCreatorJvm
import internal.dependencies_injection.timer.ConcreteTimerProvider
import internal.dependencies_injection.timestamp.ConcreteTimestampProviderJvm
import internal.dependencies_injection.uuid.ConcreteUUIDGeneratorJvm
import internal.dependencies_injection.webrtc.ConcreteCrolangP2PPeerConnectionFactoryJvm

internal object DependenciesInjectionProviderJvm {

    fun getDependencies(): DependenciesInjection {
        return DependenciesInjection(
            BuildConfig.MY_PLATFORM,
            BuildConfig.VERSION,
            ConcreteEventLoopJvm(),
            ConcreteCrolangP2PSocketCreatorJvm(),
            ConcreteCrolangP2PPeerConnectionFactoryJvm(),
            ConcreteUUIDGeneratorJvm(),
            ConcreteTimestampProviderJvm(),
            ConcreteTimerProvider(),
            ConcreteSleepProviderJvm(),
            ConcreteExecutorProviderJvm()
        )
    }

}
