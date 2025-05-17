import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.GradleException
import java.io.ByteArrayOutputStream
import java.nio.file.Files

fun getGitTag(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "describe", "--tags", "--abbrev=0")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    return stdout.toString().trim().ifEmpty {
        throw GradleException("No git tag found.")
    }
}

val projectVersion = getGitTag()
val targetJavaMinVersion = 11

plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.8.10"
    id("com.github.jk1.dependency-license-report") version "1.16"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.crolang-p2p"
version = projectVersion

kotlin {
    jvmToolchain(targetJavaMinVersion)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.0")
    implementation("io.socket:socket.io-client:2.1.2") {
        exclude(group = "org.json", module = "json")
        exclude(group = "io.socket", module = "engine.io-client")
    }
    implementation("io.socket:engine.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("org.json:json:20250107")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("dev.onvoid.webrtc:webrtc-java:0.10.0")
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

tasks {

    named<Jar>("jar") {
        from("LICENSE") {
            into("META-INF")
        }
        from("NOTICE") {
            into("META-INF")
        }
    }

    named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
        dependsOn("generateBuildConfig")
    }

    val dokkaJavadoc by getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
        dependsOn("generateBuildConfig")
        outputDirectory.set(buildDir.resolve("dokkaJavadoc"))
        dokkaSourceSets {
            configureEach {
                reportUndocumented.set(true)
            }
        }
    }


    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc.outputDirectory)
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
        dependsOn("generateBuildConfig")
    }


}

mavenPublishing {
    coordinates(
        groupId = "io.github.crolang-p2p",
        artifactId = "crolang-p2p-node-jvm",
        version = projectVersion
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("crolang-p2p-node-jvm")
        description.set("Kotlin/Java client for CrolangP2P")
        inceptionYear.set("2025")
        url.set("https://github.com/crolang-p2p/crolang-p2p-node-jvm")

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
            url.set("https://github.com/crolang-p2p/crolang-p2p-node-jvm")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}

tasks.register("printBuildSummary") {
    doLast {
        val javaVersion = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(targetJavaMinVersion))
        }.get().metadata.javaRuntimeVersion

        val jarTask = tasks.named("jar").get() as Jar
        val jarFile = jarTask.archiveFile.get().asFile

        println("✅  Build completed successfully!")
        println("💾 Project version: $projectVersion")
        println("🎯 Target Java min version: $targetJavaMinVersion")
        println("☕  Java version used: $javaVersion")
        println("📦 JAR file created: ${jarFile.name} at ${jarFile.parent}")
    }
}

tasks.named("build") {
    dependsOn("addLicenseHeader")
    finalizedBy("printBuildSummary")
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

val generatedBuildConfigDir = "$buildDir/generated/sources/buildConfig"

sourceSets {
    main {
        java.srcDir(generatedBuildConfigDir)
    }
}

tasks.register("generateBuildConfig") {
    val outputDir = file(generatedBuildConfigDir)
    outputs.dir(outputDir)
    doLast {
        val pkg = "internal"
        val file = file("$generatedBuildConfigDir/${pkg.replace('.', '/')}/BuildConfig.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package $pkg
            
            object BuildConfig {
                const val VERSION: String = "$projectVersion"
            }
            """.trimIndent()
        )
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateBuildConfig")
}

