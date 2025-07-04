package internal.dependencies_injection

import internal.BuildConfig
import internal.dependencies.DependenciesInjection
import internal.dependencies_injection.event_loop.ConcreteEventLoopJvm
import internal.dependencies_injection.executor.ConcreteExecutorProviderJvm
import internal.dependencies_injection.sleep.ConcreteSleepProviderJvm
import internal.dependencies_injection.socket.ConcreteCrolangP2PSocketCreatorJvm
import internal.dependencies_injection.synchronization.ConcreteSynchronizationProvider
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
            ConcreteSynchronizationProvider(),
            ConcreteExecutorProviderJvm()
        )
    }

}