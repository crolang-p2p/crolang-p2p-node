![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue?logo=kotlin)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet?logo=kotlin) TODO kotlin 2.1
![Java](https://img.shields.io/badge/Java-11-blue)

![GitHub last commit](https://img.shields.io/github/last-commit/crolang-p2p/crolang-p2p-node)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.crolang-p2p/crolang-p2p-node-jvm.svg)](https://central.sonatype.com/artifact/io.github.crolang-p2p/crolang-p2p-node-jvm)
![GitHub Release Date](https://img.shields.io/github/release-date/crolang-p2p/crolang-p2p-node)

![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)
![Open Source](https://img.shields.io/badge/Open%20Source-%E2%9C%93-brightgreen)

# crolang-p2p-node
The official Kotlin Multiplatform Crolang Node implementation for the [CrolangP2P](https://github.com/crolang-p2p) project.

## Table of contents
- [The CrolangP2P Project](#the-crolangp2p-project)
- [Supported Platforms & Examples](#supported-platforms--examples)
  - [JVM (Kotlin/Java)](#jvm-kotlinjava)
- [Contributing](#contributing)
- [License](#license)

## The CrolangP2P Project
[CrolangP2P](https://github.com/crolang-p2p) is a simple, robust framework for cross-language peer-to-peer (P2P) connections. Clients (“Crolang Nodes”) libraries can be easily integrated into your project and connect using only the ID of the target node, exchanging messages directly via P2P or via WebSocket using the [Crolang Broker](https://github.com/crolang-p2p/crolang-p2p-broker) as relay. The framework manages the connection an you can focus on what matters most: your project's logic.

- **Simplicity:** Minimal setup—just import the Node library, specify the peer ID, and connect.
- **Cross-language:** [Multiple Node implementations](#supported-platforms--examples) allow seamless P2P between different languages.
- **No packet size limits:** Large data exchange is supported.
- **Extensible:** The Broker supports modular extensions for authentication, authorization, message handling, and more.

Nodes connect through the [Crolang Broker](https://github.com/crolang-p2p/crolang-p2p-broker), which acts as a rendezvous point: it helps nodes discover each other and establish direct WebRTC connections.

## Supported Platforms & Examples

This library is built with Kotlin Multiplatform and currently supports the following targets:

### JVM (Kotlin/Java)

**Requirements:**
- Java 11+
- Kotlin 1.9+ (for Kotlin projects)

**Installation:**

Add the dependency to your project using one of the snippets available in the [Maven Central Repository page](https://central.sonatype.com/artifact/io.github.crolang-p2p/crolang-p2p-node-jvm/overview):

```kotlin
dependencies {
    implementation("io.github.crolang-p2p:crolang-p2p-node-jvm:1.0.0")
}
```

**Note:** If you are using Linux on an aarch32 or aarch64 architecture, please do not use Java 21, as it is not supported by the WebRTC implementation used by this library. Use any other version of Java 11+ instead.

**Documentation and Examples:**
- Kotlin usage: [examples-kotlin-crolang-p2p-node-jvm](https://github.com/crolang-p2p/examples-kotlin-crolang-p2p-node-jvm)
- Java usage: [examples-java-crolang-p2p-node-jvm](https://github.com/crolang-p2p/examples-java-crolang-p2p-node-jvm)

### JavaScript/Node.js
TODO to be fixed, just to track the info
**Requirements:**
- Node.js 16+
- TypeScript 4.5+ (for TypeScript projects)

**Installation:**

```bash
npm install @crolang-p2p/crolang-p2p-node
```

**Usage:**

```typescript
import { crolangP2P } from '@crolang-p2p/crolang-p2p-node';

// Check if connected to broker
const isConnected = crolangP2P.isLocalNodeConnectedToBroker();
console.log('Connected to broker:', isConnected);
```

**Documentation and Examples:**
- TypeScript/Node.js usage: [examples-nodejs-crolang-p2p-node-js](./examples/examples-nodejs-crolang-p2p-node-js)

**Building and Publishing the npm Package:**

> **Note:** This section is for maintainers who need to build and publish the npm package from the Kotlin/JS source.

1. **Build the Kotlin/JS library:**
   ```bash
   ./gradlew clean build jsPackage
   ```

2. **Create the npm package with all dependencies:**
   ```bash
   ./gradlew createNpmPackage
   ```

3. **The package will be created in:** `build/js/packages/crolang-p2p-node/`

4. **Test locally:**
   ```bash
   cd build/js/packages/crolang-p2p-node
   npm pack
   ```

5. **Publish to npm:**
   ```bash
   npm publish
   ```

## Contributing
Contributions, bug reports, and feature requests are welcome! Please open an issue or pull request on GitHub.

## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.