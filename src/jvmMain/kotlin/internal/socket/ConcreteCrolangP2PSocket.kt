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

package internal.socket

import internal.dependencies.socket.CrolangP2PSocket
import io.socket.client.Ack
import io.socket.client.Socket

internal class ConcreteCrolangP2PSocket(private val socket: Socket) : CrolangP2PSocket() {

    override fun close() {
        socket.close()
    }

    override fun connect() {
        socket.connect()
    }

    override fun connected(): Boolean {
        return socket.connected()
    }

    override fun disconnect() {
        socket.disconnect()
    }

    override fun emit(event: String, msg: String, onAck: (args: Array<out Any>) -> Unit) {
        socket.emit(event, msg, Ack { onAck(it) })
    }

    override fun on(event: String, callback: (args: Array<Any>) -> Unit) {
        socket.on(event) { callback(it) }
    }
}
