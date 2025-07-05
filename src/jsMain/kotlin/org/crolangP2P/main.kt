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

package org.crolangP2P

/**
 * Main entry point for the JavaScript/Node.js module.
 * This ensures that the CrolangP2P API is properly exported.
 */
@JsExport
fun main() {
    // This function ensures the module is properly initialized
    // The CrolangP2P object will be available as a global export
}

// Export the CrolangP2P API for JavaScript/Node.js consumption
@JsExport
val crolangP2P = CrolangP2P()
