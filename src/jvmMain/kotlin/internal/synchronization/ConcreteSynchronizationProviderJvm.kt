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

package internal.synchronization

import internal.dependencies.utils.CountdownLatch
import internal.dependencies.utils.SynchronizationProvider
import java.util.concurrent.CountDownLatch as JvmCountDownLatch

/**
 * Concrete implementation of SynchronizationProvider for JVM platform.
 * 
 * This implementation uses java.util.concurrent.CountDownLatch to provide synchronization functionality.
 */
class ConcreteSynchronizationProvider : SynchronizationProvider() {
    
    override fun createCountdownLatch(count: Int): CountdownLatch {
        return ConcreteJvmCountdownLatch(count)
    }
    
    override fun <T> synchronized(obj: Any, block: () -> T): T {
        return kotlin.synchronized(obj, block)
    }
}

/**
 * Concrete implementation of CountdownLatch for JVM platform.
 * 
 * This implementation wraps java.util.concurrent.CountDownLatch.
 */
class ConcreteJvmCountdownLatch(count: Int) : CountdownLatch() {
    
    private val latch = JvmCountDownLatch(count)
    
    override fun await() {
        latch.await()
    }
    
    override fun countDown() {
        latch.countDown()
    }
}
