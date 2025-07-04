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

@file:JsModule("crypto")
@file:JsNonModule

package internal.dependencies_injection.uuid

/**
 * External declarations for Node.js crypto module functions.
 * These provide access to Node.js cryptographic APIs.
 */

/**
 * Generates a random RFC 4122 version 4 UUID.
 * This function is available in Node.js v14.17.0 and later.
 * 
 * @return A randomly generated UUID string in standard format
 */
external fun randomUUID(): String
