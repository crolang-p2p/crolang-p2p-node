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

package internal.dependencies_injection.webrtc

import kotlin.js.js

/**
 * WebRTC polyfill for Node.js environments.
 *
 * This module automatically detects if we're running in Node.js and sets up
 * the necessary WebRTC polyfill. In browser environments, it does nothing
 * since WebRTC APIs are already available.
 */

/**
 * Sets up WebRTC polyfill for Node.js if needed.
 * This function is called automatically when the library loads.
 */
fun setupWebRTCPolyfill() {
    // Use direct JavaScript execution for runtime checks and polyfill setup
    js("""
        try {
            // Check if we're in Node.js and WebRTC is not available
            if (typeof process !== 'undefined' && process.versions && process.versions.node) {
                if (typeof globalThis.RTCPeerConnection === 'undefined') {
                    
                    try {
                        // Import the wrtc polyfill at runtime
                        var wrtc = require('@roamhq/wrtc');
                        
                        // Setup global WebRTC APIs
                        globalThis.RTCPeerConnection = wrtc.RTCPeerConnection;
                        globalThis.RTCSessionDescription = wrtc.RTCSessionDescription;
                        globalThis.RTCIceCandidate = wrtc.RTCIceCandidate;
                        globalThis.RTCDataChannel = wrtc.RTCDataChannel;
                        globalThis.MediaStream = wrtc.MediaStream;
                        globalThis.MediaStreamTrack = wrtc.MediaStreamTrack;
                    } catch (e) {
                        console.warn('⚠️  WebRTC polyfill setup failed. Please install @roamhq/wrtc:');
                        console.warn('   npm install @roamhq/wrtc');
                        console.warn('   Error:', e.message);
                    }
                }
            }
        } catch (e) {
            console.warn('⚠️  WebRTC environment detection failed:', e.message);
        }
    """)
}
