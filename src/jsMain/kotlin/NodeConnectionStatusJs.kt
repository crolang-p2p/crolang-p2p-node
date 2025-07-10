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
 * Represents the connection status of a specific node to the broker.
 * 
 * This class provides a simple data structure for reporting whether
 * a specific node is currently connected to the broker. Used as return
 * type for bulk connection status queries.
 * 
 * @param id The unique identifier of the node
 * @param status true if the node is connected to the broker, false otherwise
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NodeConnectionStatusJs(val id: String, val status: Boolean)
