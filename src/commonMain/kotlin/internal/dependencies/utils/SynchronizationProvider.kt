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

package internal.dependencies.utils

/**
 * Abstract contract for synchronization primitives.
 *
 * This abstraction allows different platforms to provide their own synchronization implementation
 * while keeping the core business logic platform-agnostic.
 */
abstract class SynchronizationProvider {
    
    /**
     * Creates a new countdown latch with the specified initial count.
     * 
     * @param count The initial count of the latch
     * @return A CountdownLatch instance
     */
    abstract fun createCountdownLatch(count: Int): CountdownLatch
    
    /**
     * Executes the given block with synchronization on the specified object.
     * This provides a platform-agnostic way to perform synchronized operations.
     * 
     * @param obj The object to synchronize on
     * @param block The block of code to execute synchronously
     * @return The result of the block execution
     */
    abstract fun <T> synchronized(obj: Any, block: () -> T): T
}

/**
 * Abstract contract for a countdown latch synchronization primitive.
 * 
 * A countdown latch allows one or more threads to wait until a set of operations
 * being performed in other threads completes.
 */
abstract class CountdownLatch {
    
    /**
     * Causes the current thread to wait until the latch has counted down to zero.
     */
    abstract fun await()
    
    /**
     * Decrements the count of the latch, releasing all waiting threads if the count reaches zero.
     */
    abstract fun countDown()
}
