![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blueviolet?logo=kotlin)
![Java](https://img.shields.io/badge/Java-11-blue)

![GitHub last commit](https://img.shields.io/github/last-commit/crolang-p2p/crolang-p2p-node-jvm)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.crolang-p2p/crolang-p2p-node-jvm.svg)](https://central.sonatype.com/artifact/io.github.crolang-p2p/crolang-p2p-node-jvm)
![GitHub Release Date](https://img.shields.io/github/release-date/crolang-p2p/crolang-p2p-node-jvm)

![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)
![Open Source](https://img.shields.io/badge/Open%20Source-%E2%9C%93-brightgreen)

# crolang-p2p-node-jvm
The official JVM (Kotlin/Java) Crolang Node implementation for the [CrolangP2P](https://github.com/crolang-p2p) project.

## Table of contents
- [The CrolangP2P Project](#the-crolangp2p-project)
- [Requirements](#requirements)
- [Installation](#installation)
- [Documentation and examples](#documentation-and-examples)
- [Contributing](#contributing)
- [License](#license)

## The CrolangP2P Project
[CrolangP2P](https://github.com/crolang-p2p) is a simple, robust framework for cross-language peer-to-peer (P2P) connections. Clients (“Crolang Nodes”) libraries can be easily integrated into your project and connect using only the ID of the target node, exchanging messages directly via P2P or via WebSocket using the [Crolang Broker](https://github.com/crolang-p2p/crolang-p2p-broker) as relay. The framework manages the connection an you can focus on what matters most: your project's logic.

- **Simplicity:** Minimal setup—just import the Node library, specify the peer ID, and connect.
- **Cross-language:** [Multiple Node implementations](#available-crolangp2p-node-implementations) allow seamless P2P between different languages.
- **No packet size limits:** Large data exchange is supported.
- **Extensible:** The Broker supports modular extensions for authentication, authorization, message handling, and more.

Nodes connect through the [Crolang Broker](https://github.com/crolang-p2p/crolang-p2p-broker), which acts as a rendezvous point: it helps nodes discover each other and establish direct WebRTC connections.

## Requirements
- Java 11+
- Kotlin 1.9+ (for Kotlin projects)

**Note:** If you are using linux on an aarch32 or aarch64 architecture, please do not use java 21, as it is not supported by the JVM implementation used by this library. 
Use any other version of Java 11+ instead.

## Installation
Integrate the library into your project using one of the snippets available in the [project's Maven Central Repository page](https://central.sonatype.com/artifact/io.github.crolang-p2p/crolang-p2p-node-jvm/overview)

Example:
```
dependencies {
    implementation("io.github.crolang-p2p:crolang-p2p-node-jvm:0.1.1-alpha")
}
```

## Documentation and examples
The library is very easy to use and documentation is provided by well documented example repositories:
- Kotlin usage: [examples-kotlin-crolang-p2p-node-jvm](https://github.com/crolang-p2p/examples-kotlin-crolang-p2p-node-jvm)
- Java usage: [examples-java-crolang-p2p-node-jvm](https://github.com/crolang-p2p/examples-java-crolang-p2p-node-jvm)

You can also visit the [complete list of crolang nodes examples](https://github.com/crolang-p2p#usage-examples)

## Contributing
Contributions, bug reports, and feature requests are welcome! Please open an issue or pull request on GitHub.

## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.