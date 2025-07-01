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

@OptIn(ExperimentalJsExport::class)
@JsExport
class OnNewSocketMsgJs {
    
    private val listeners = mutableMapOf<String, (String, String) -> Unit>()
    
    fun addListener(channel: String, listener: (String, String) -> Unit): OnNewSocketMsgJs {
        listeners[channel] = listener
        return this
    }
    
    fun getListeners(): Map<String, (String, String) -> Unit> {
        return listeners
    }

}

@OptIn(ExperimentalJsExport::class)
@JsExport
object OnNewSocketMsgJsBuilder {

    fun create(): OnNewSocketMsgJs {
        return OnNewSocketMsgJs()
    }

}
