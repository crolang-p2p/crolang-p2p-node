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

import Constants.BROKER_ADDR
import org.crolangP2P.CrolangP2P
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ConnectToBrokerTest {
    @Test
    fun connectToBroker() {
        assertFalse(CrolangP2P.Kotlin.isLocalNodeConnectedToBroker(), "isLocalNodeConnectedToBroker is true before connecting")
        val latch = CountDownLatch(1)
        var success = false
        var err = ""
        CrolangP2P.Kotlin.connectToBroker(BROKER_ADDR, "Alice")
            .onSuccess {
                success = true
                latch.countDown()
            }.onFailure { error ->
                err = error.message ?: "Unknown error"
                latch.countDown()
            }
        latch.await(10, TimeUnit.SECONDS)
        assertTrue(success, "Connection to Broker failed: $err")
        assertTrue(CrolangP2P.Kotlin.isLocalNodeConnectedToBroker(), "isLocalNodeConnectedToBroker is false after successful connection")
        CrolangP2P.Kotlin.disconnectFromBroker()
        assertFalse(CrolangP2P.Kotlin.isLocalNodeConnectedToBroker(), "isLocalNodeConnectedToBroker is true after disconnecting")
    }
}
