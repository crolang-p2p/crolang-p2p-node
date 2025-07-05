import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.GradleException
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.net.URI

fun getGitTag(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "tag", "--sort=-creatordate")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    val tags = stdout.toString().trim().lines().filter { it.isNotBlank() }
    return tags.firstOrNull() ?: throw GradleException("No git tag found.")
}

val projectVersion = getGitTag()
val targetJavaMinVersion = 11

// Platform-specific BuildConfig generation for multiplatform setup
val buildConfigBaseDir = "${layout.buildDirectory.get()}/generated/sources/buildConfig"

// Active platforms and their display names
// To add new targets:
// 1. Add entry to this map (e.g., "js" to "JavaScript", "native" to "Native")
// 2. Add corresponding sourceSet configuration in kotlin {} block
// 3. Add compilation dependency if needed (e.g., tasks.named("compileKotlinJs"))
val platforms = mapOf(
    "jvm" to "JVM",
    "js" to "JavaScript",
)

plugins {
    kotlin("multiplatform") version "2.1.21"
    id("maven-publish")
    kotlin("plugin.serialization") version "2.1.21"
    id("com.github.jk1.dependency-license-report") version "1.16"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.crolang-p2p"
version = projectVersion

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaMinVersion))
    }
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(targetJavaMinVersion.toString()))
        }
    }
    
    js(IR) {
        useCommonJs()
        nodejs()
        generateTypeScriptDefinitions()
        // For npm publishing, we need both executable and library
        // executable() generates the JS files, library() enables npm publishing
        binaries.executable()
        binaries.library()
        
        // Configure the package.json for npm publishing
        compilations["main"].packageJson {
            name = "crolang-p2p-node"
            version = projectVersion
            description = "Kotlin Multiplatform WebRTC P2P networking library for JavaScript/Node.js"
            main = "crolang-p2p-node.js"
            types = "crolang-p2p-node.d.ts"
        }
        
        compilerOptions {
            moduleKind.set(org.jetbrains.kotlin.gradle.dsl.JsModuleKind.MODULE_COMMONJS)
            sourceMap.set(true)
            sourceMapEmbedSources.set(org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.6.0")
                api("co.touchlab:kermit:2.0.3")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation("io.socket:socket.io-client:2.1.2") {
                    exclude(group = "org.json", module = "json")
                    exclude(group = "io.socket", module = "engine.io-client")
                }
                implementation("io.socket:engine.io-client:2.1.0") {
                    exclude(group = "org.json", module = "json")
                }
                implementation("org.json:json:20250107")
                implementation("dev.onvoid.webrtc:webrtc-java:0.10.0")
            }
            
            // Add generated JVM-specific BuildConfig to jvmMain sourceSets
            kotlin.srcDir("$buildConfigBaseDir/jvm")
        }
        
        val jsMain by getting {
            dependencies {
                implementation(npm("socket.io-client", "4.8.1"))
                api("com.shepeliev:webrtc-kmp:0.125.9")
            }
            
            // Add generated JS-specific BuildConfig to jsMain sourceSets
            kotlin.srcDir("$buildConfigBaseDir/js")
        }
    }
}

licenseReport {
    configurations = arrayOf("jvmRuntimeClasspath", "jsNodeProductionLibraryCompileClasspath")
}

tasks {
    withType<Jar> {
        from("LICENSE") {
            into("META-INF")
        }
        from("NOTICE") {
            into("META-INF")
        }
    }
}

// Dokka configuration for multiplatform documentation
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dependsOn("generateBuildConfig")
    moduleName.set("CroLang P2P Node")
    
    dokkaSourceSets {
        configureEach {
            // Include multiplatform documentation
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            reportUndocumented.set(true)
            
            // External documentation links
            externalDocumentationLink {
                url.set(URI.create("https://kotlinlang.org/api/kotlinx.coroutines/").toURL())
                packageListUrl.set(URI.create("https://kotlinlang.org/api/kotlinx.coroutines/package-list").toURL())
            }
            
            externalDocumentationLink {
                url.set(URI.create("https://kotlinlang.org/api/kotlinx.serialization/").toURL())
                packageListUrl.set(URI.create("https://kotlinlang.org/api/kotlinx.serialization/package-list").toURL())
            }
        }
        
        named("commonMain") {
            displayName.set("Common")
            platform.set(org.jetbrains.dokka.Platform.common)
        }
        
        named("jvmMain") {
            displayName.set("JVM")
            platform.set(org.jetbrains.dokka.Platform.jvm)
        }
        
        named("jsMain") {
            displayName.set("JavaScript/Node.js")
            platform.set(org.jetbrains.dokka.Platform.js)
        }
    }
}

// Fix Dokka V2 task dependencies
tasks.named("dokkaGeneratePublicationHtml") {
    dependsOn("generateJavaScriptBuildConfig", "generateJVMBuildConfig")
}

// Configure publishing with vanniktech plugin
mavenPublishing {
    coordinates(
        groupId = "io.github.crolang-p2p",
        artifactId = "crolang-p2p-node",
        version = projectVersion
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("CroLang P2P Node")
        description.set("A Kotlin Multiplatform library for CroLang P2P networking")
        inceptionYear.set("2025")
        url.set("https://github.com/crolang/crolang-p2p-node")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("Tale152")
                name.set("Alessandro Talmi")
                email.set("alessandro.talmi@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/crolang/crolang-p2p-node")
            connection.set("scm:git:git://github.com/crolang/crolang-p2p-node.git")
            developerConnection.set("scm:git:ssh://github.com/crolang/crolang-p2p-node.git")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    if (gradle.startParameter.taskNames.any { it.contains("publish") && !it.contains("ToMavenLocal") }) {
        signAllPublications()
    }
}

// Create a base task for generating BuildConfig
abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val platformKey: Property<String>
    
    @get:Input
    abstract val platformName: Property<String>
    
    @get:Input
    abstract val projectVersion: Property<String>
    
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty
    
    @TaskAction
    fun generateBuildConfig() {
        val pkg = "internal"
        val outputDirectory = outputDir.get().asFile
        val file = File(outputDirectory, "${pkg.replace('.', '/')}/BuildConfig.kt")
        
        // Read license header from HEADER.txt file
        val headerFile = project.file("HEADER.txt")
        if (!headerFile.exists()) {
            throw GradleException("HEADER.txt file not found. License header is required for BuildConfig generation.")
        }
        val licenseHeader = headerFile.readText().trim()
        
        file.parentFile.mkdirs()
        file.writeText("""$licenseHeader
package $pkg

/**
 * Build configuration for ${platformName.get()} target.
 * This object provides compile-time constants for the ${platformName.get()} platform.
 */
object BuildConfig {
    /** Current library version */
    const val VERSION: String = "${projectVersion.get()}"
    
    /** Library name */
    const val LIBRARY_NAME: String = "crolang-p2p-node"
    
    /** Target platform identifier */
    const val MY_PLATFORM: String = "${platformName.get()}"
}
""")
        
        logger.info("Generated BuildConfig for ${platformName.get()} platform at: ${file.absolutePath}")
    }
}

// Generate BuildConfig for each active platform
platforms.forEach { (platformKey, platformName) ->
    tasks.register<GenerateBuildConfigTask>("generate${platformName}BuildConfig") {
        this.platformKey.set(platformKey)
        this.platformName.set(platformName)
        this.projectVersion.set(project.version.toString())
        this.outputDir.set(layout.buildDirectory.dir("generated/sources/buildConfig/$platformKey"))
        
        group = "build setup"
        description = "Generates BuildConfig for $platformName target"
    }
}

// Master task that generates BuildConfig for all active targets
tasks.register("generateBuildConfig") {
    dependsOn(platforms.map { (_, platformName) -> "generate${platformName}BuildConfig" })
    group = "build setup"
    description = "Generates BuildConfig files for all active Kotlin Multiplatform targets"
    
    doLast {
        logger.lifecycle("✅ Generated BuildConfig for all active platforms: ${platforms.values.joinToString(", ")}")
    }
}

tasks.register("addLicenseHeader") {
    doLast {
        val headerFile = file("HEADER.txt")

        if (!headerFile.exists()) {
            throw GradleException("The HEADER.txt file does not exist")
        }

        val header = headerFile.readText()

        file("src").walkTopDown().filter { it.isFile && it.extension in listOf("java", "kt", "groovy") }.forEach { file ->
            val lines = file.readLines().toMutableList()

            if (lines.isNotEmpty() && !lines[0].startsWith("/*")) {
                println("Adding license header to this file: ${file.relativeTo(projectDir)}")
                lines.add(0, header)
                Files.write(file.toPath(), lines)
            }
        }
    }
}

tasks.register("viewDocumentation") {
    dependsOn("dokkaGeneratePublicationHtml")
    doLast {
        val docFile = file("build/dokka/html/index.html")
        
        if (docFile.exists()) {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("mac") -> "open"
                os.contains("win") -> "start"
                else -> "xdg-open"
            }
            println("📖 Opening documentation in browser...")
            exec {
                commandLine = listOf(command, docFile.absolutePath)
            }
        } else {
            println("❌ Documentation not found. Please generate the documentation first.")
        }
    }
}

tasks.register("generateAllDocs") {
    group = "documentation"
    description = "Generates all documentation formats (HTML, Javadoc, and GFM)"
    dependsOn("dokkaGenerate", "jvmDokkaJavadocJar")
    doLast {
        println("📚 All documentation formats generated successfully!")
        println("📖 HTML docs: build/dokka/html/index.html")
        println("📄 Javadoc: build/dokka/javadoc/index.html")
        println("📝 GitHub Markdown: build/dokka/gfm/")
        println("📦 Javadoc JAR: build/libs/${project.name}-${project.version}-javadoc.jar")
    }
}

tasks.register("viewLicenseReport") {
    dependsOn("generateLicenseReport")
    doLast {
        val reportFile = file("build/reports/dependency-license/index.html")

        if (reportFile.exists()) {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("mac") -> "open"
                os.contains("win") -> "start"
                else -> "xdg-open"
            }
            println("📄 Opening license report in browser...")
            exec {
                commandLine = listOf(command, reportFile.absolutePath)
            }
        } else {
            println("❌ License report not found. Please generate the report first.")
        }
    }
}

tasks.register("printBuildSummary") {
    doLast {
        val javaVersion = project.extensions.getByType<JavaToolchainService>()
            .launcherFor {
                languageVersion.set(JavaLanguageVersion.of(targetJavaMinVersion))
            }.get().metadata.javaRuntimeVersion

        val jvmJarTask = tasks.named("jvmJar").get() as Jar
        val jvmJarFile = jvmJarTask.archiveFile.get().asFile
        
        val docHtmlFile = file("build/dokka/html/index.html")
        val docJavadocFile = file("build/dokka/javadoc/index.html")
        val javadocJarFile = file("build/libs/${project.name}-${project.version}-javadoc.jar")

        println("✅  Build completed successfully!")
        println("💾 Project version: $projectVersion")
        println("🎯 Target Java min version: $targetJavaMinVersion")
        println("☕  Java version used: $javaVersion")
        println("📦 JVM JAR file created: ${jvmJarFile.name} at ${jvmJarFile.parent}")
        println("🔧 Kotlin Multiplatform targets: JVM, JavaScript/Node.js")
        println("📋 License report configured for: jvmRuntimeClasspath, jsNodeProductionLibraryCompileClasspath")
        
        println("\n📚 Documentation:")
        if (docHtmlFile.exists()) {
            println("📖 HTML documentation: Available (run './gradlew viewDocumentation' to open)")
        } else {
            println("📖 HTML documentation: Not generated (run './gradlew dokkaGeneratePublicationHtml')")
        }
        
        if (docJavadocFile.exists()) {
            println("📄 Javadoc documentation: Available")
        } else {
            println("📄 Javadoc documentation: Not generated (run './gradlew dokkaGenerate')")
        }
        
        if (javadocJarFile.exists()) {
            println("📦 Javadoc JAR: ${javadocJarFile.name}")
        } else {
            println("📦 Javadoc JAR: Not generated (run './gradlew javadocJar')")
        }
        
        println("\n🚀 Quick commands:")
        println("   ./gradlew generateAllDocs    # Generate all documentation formats")
        println("   ./gradlew viewDocumentation  # Open HTML docs in browser")
        println("   ./gradlew viewLicenseReport  # Open license report in browser")
    }
}

tasks.named("build") {
    dependsOn("addLicenseHeader", "dokkaGeneratePublicationHtml", "jvmDokkaJavadocJar")
    finalizedBy("printBuildSummary")
}

// Ensure platform-specific BuildConfig generation runs before compilation
tasks.named("compileKotlinJvm") {
    dependsOn("generateJVMBuildConfig")
}

tasks.named("compileKotlinJs") {
    dependsOn("generateJavaScriptBuildConfig")
}

// Ensure BuildConfig is generated before source JAR creation
tasks.named("jvmSourcesJar") {
    dependsOn("generateJVMBuildConfig")
}

tasks.named("jsSourcesJar") {
    dependsOn("generateJavaScriptBuildConfig")
}

// Custom task to create complete npm package
tasks.register("createNpmPackage") {
    group = "publishing"
    description = "Creates a complete npm package with all necessary files"
    dependsOn("jsNodeProductionLibraryDistribution", "jsPackageJson")
    
    doLast {
        val packageDir = file("build/js/packages/crolang-p2p-node")
        val distDir = file("build/dist/js/productionLibrary")
        
        // Copy ALL JS files to package directory (including dependencies)
        copy {
            from(distDir)
            into(packageDir)
        }
        
        println("📦 NPM package created successfully!")
        println("📁 Package directory: ${packageDir.absolutePath}")
        
        // Create npm tarball
        exec {
            workingDir = packageDir
            commandLine = listOf("npm", "pack")
        }
        
        println("🎯 Package file: ${packageDir.absolutePath}/crolang-p2p-node-${projectVersion}.tgz")
    }
}

// Fix Gradle dependency issue
tasks.named("jsNodeProductionLibraryDistribution") {
    dependsOn("jsProductionExecutableCompileSync")
}
