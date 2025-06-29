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

package internal.dependencies.webrtc.concrete

/**
 * Enumeration of WebRTC bundle policies for media stream grouping.
 * 
 * Bundle policy determines how media streams are grouped together
 * to optimize transport connections and reduce resource usage.
 */
enum class CrolangP2PRTCBundlePolicy {
    /** 
     * Balanced approach that bundles media when beneficial for performance
     * while maintaining compatibility.
     */
    BALANCED,
    
    /** 
     * Maximum compatibility mode that avoids bundling to ensure
     * compatibility with older implementations.
     */
    MAX_COMPAT,
    
    /** 
     * Maximum bundling mode that groups all media streams together
     * to minimize the number of transport connections.
     */
    MAX_BUNDLE;
}
