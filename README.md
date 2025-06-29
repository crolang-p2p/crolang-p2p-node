![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue?logo=kotlin)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet?logo=kotlin)
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

## Contributing
Contributions, bug reports, and feature requests are welcome! Please open an issue or pull request on GitHub.

## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.