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

package internal.dependencies

import internal.dependencies.event_loop.EventLoop
import internal.dependencies.socket.CrolangP2PSocketCreator
import internal.dependencies.webrtc.contracts.CrolangP2PPeerConnectionFactory
import internal.dependencies.utils.ExecutorProvider
import internal.dependencies.utils.SleepProvider
import internal.dependencies.utils.SynchronizationProvider
import internal.dependencies.utils.TimestampProvider
import internal.dependencies.utils.TimerProvider
import internal.dependencies.utils.UUIDGenerator

/**
 * Dependency injection container that holds all platform-specific implementations
 * required by the CroLang P2P library.
 *
 * This class serves as a central registry for all dependencies, allowing for
 * easy testing and platform-specific implementations.
 *
 * @property myPlatform The target platform identifier (e.g., "JVM", "JavaScript", "Native")
 * @property myVersion The current library version
 * @property eventLoop Event loop implementation for handling asynchronous operations
 * @property socketCreator Factory for creating socket connections to brokers
 * @property crolangP2PPeerConnectionFactory Factory for creating WebRTC peer connections
 * @property uuidGenerator Generator for creating unique identifiers
 * @property timestampProvider Provider for obtaining current timestamps
 * @property timerProvider Provider for creating and managing timers
 * @property sleepProvider Provider for implementing platform-specific sleep operations
 * @property synchronizationProvider Provider for synchronization primitives
 * @property executorProvider Provider for executing operations on different threads/coroutines
 */
class DependenciesInjection(
    val myPlatform: String,
    val myVersion: String,
    val eventLoop: EventLoop,
    val socketCreator: CrolangP2PSocketCreator,
    val crolangP2PPeerConnectionFactory: CrolangP2PPeerConnectionFactory,
    val uuidGenerator: UUIDGenerator,
    val timestampProvider: TimestampProvider,
    val timerProvider: TimerProvider,
    val sleepProvider: SleepProvider,
    val synchronizationProvider: SynchronizationProvider,
    val executorProvider: ExecutorProvider,
)
