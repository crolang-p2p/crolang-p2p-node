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

/**
 * Configuration for handling messages received through the broker's signaling connection.
 * 
 * This class allows you to register callbacks for different message channels
 * that come through the broker (not direct P2P connections). These are typically
 * used for:
 * - Control messages and signaling
 * - Messages from nodes that cannot establish direct P2P connections
 * - Broadcast messages sent through the broker
 * 
 * Use [OnNewSocketMsgJsBuilder.create] to construct instances.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class OnNewSocketMsgJs {
    
    private val listeners = mutableMapOf<String, (String, String) -> Unit>()
    
    /**
     * Adds a message listener for a specific channel.
     * 
     * The listener will be called when a message arrives on the specified channel
     * through the broker's signaling connection.
     * 
     * @param channel The message channel to listen to
     * @param listener Function that receives sender ID and message content
     * @return This instance for method chaining
     */
    fun addListener(channel: String, listener: (String, String) -> Unit): OnNewSocketMsgJs {
        listeners[channel] = listener
        return this
    }
    
    /**
     * Gets all registered message listeners by channel.
     * 
     * @return Map of channel names to their listener functions
     */
    fun getListeners(): Map<String, (String, String) -> Unit> {
        return listeners
    }

}

/**
 * Builder for creating [OnNewSocketMsgJs] instances.
 * 
 * This builder provides a convenient way to create message handler objects
 * with no initial listeners.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
object OnNewSocketMsgJsBuilder {

    /**
     * Creates a new [OnNewSocketMsgJs] instance with no listeners.
     * 
     * @return A new message handler instance ready for listener registration
     */
    fun create(): OnNewSocketMsgJs {
        return OnNewSocketMsgJs()
    }

}
