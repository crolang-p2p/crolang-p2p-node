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

package internal.utils

import dev.onvoid.webrtc.internal.NativeLoader
import internal.utils.SharedStore.logger
import java.io.File
import java.io.InputStream
import java.lang.reflect.Field
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves and loads the appropriate native WebRTC library at runtime,
 * depending on the host OS and architecture.
 */
internal object RuntimeDependencyResolver {

    private const val LIB_VERSION = "0.8.0"
    private var isLoaded = false

    /**
     * Loads the WebRTC dependency JAR and native library based on the current system architecture.
     *
     * @param logger the logger to report status, info, and errors.
     * @return true if the dependency was successfully loaded; false otherwise.
     */
    fun loadDependency(logger: CrolangLogger): Boolean {
        if(isLoaded){
            return true
        }
        val osArch = retrieveOsArch()
        if(!osArch.isPresent) {
            logger.regularErr("Unsupported OS or architecture")
            return false
        }
        logger.regularInfo("Detected OS/Arch: ${osArch.get()}")

        val filename = "webrtc-java-$LIB_VERSION-${osArch.get()}.jar"

        val jarFile = extractJarFromResources(logger, filename)
        if (jarFile == null) {
            logger.regularErr("Error extracting the JAR from resources")
            return false
        }

        logger.debugInfo("Loading WebRTC jar from: ${jarFile.absolutePath}")

        loadArchitectureSpecificOnSystem(jarFile).let { success ->
            if (!success) {
                return false
            }
        }

        preventWebrtcLibraryFromTryingToLoadOnSystem()

        isLoaded = true
        return true
    }

    /**
     * Detects the current operating system and architecture.
     *
     * @return an [Optional] describing the corresponding WebRTC platform string (e.g., "linux-x86_64"), or empty if unsupported.
     */
    private fun retrieveOsArch(): Optional<String> {
        val osName = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()

        return Optional.ofNullable(when {
            osName.contains("mac") -> when {
                arch.contains("aarch64") -> "macos-aarch64"
                arch.contains("x86_64") || arch.contains("amd64") -> "macos-x86_64"
                else -> null
            }
            osName.contains("win") -> when {
                arch.contains("x86_64") || arch.contains("amd64") -> "windows-x86_64"
                else -> null
            }
            osName.contains("nix") || osName.contains("nux") -> when {
                arch.contains("x86_64") || arch.contains("amd64") -> "linux-x86_64"
                arch.contains("aarch64") -> "linux-aarch64"
                arch.contains("aarch32") -> "linux-aarch32"
                else -> null
            }
            else -> null
        })
    }

    /**
     * Extracts the specified WebRTC JAR file from the resources and writes it to a temporary file.
     *
     * @param logger the logger to report any error messages.
     * @param filename the name of the resource JAR file to extract.
     * @return a [File] pointing to the extracted JAR, or null if the extraction failed.
     */
    private fun extractJarFromResources(logger: CrolangLogger, filename: String): File? {
        val resourceStream: InputStream? = this.javaClass.classLoader.getResourceAsStream(filename)

        if (resourceStream == null) {
            logger.regularErr("Resource $filename not found")
            return null
        }

        val tempFile = File.createTempFile("webrtc-java", ".jar")
        tempFile.deleteOnExit()

        resourceStream.use { input ->
            Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        return tempFile
    }

    /**
     * Loads the native architecture-specific WebRTC shared library from the provided JAR file.
     *
     * The webrtc library normally would handle this on its own, but it assumes that among the gradle dependencies there are
     * the general dependency (dev.onvoid.webrtc:webrtc-java:$webrtcVersion) and the architecture specific dependency.
     * Since I don't want to have multiple versions of this library for the various architectures, I have to load the
     * architecture specific library manually at runtime.
     *
     * @param jarFile the extracted architecture-specific JAR.
     * @return true if loading was successful; false otherwise.
     */
    private fun loadArchitectureSpecificOnSystem(jarFile: File): Boolean{
        val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), NativeLoader::class.java.classLoader)

        val libFileName = System.mapLibraryName("webrtc-java")
        val tempName = libFileName.substring(0, libFileName.lastIndexOf("."))
        val ext = libFileName.substring(libFileName.lastIndexOf(".") + 1)

        val tempPath = Files.createTempFile(tempName, ext)
        val tempFile = tempPath.toFile()

        try {
            classLoader.getResourceAsStream(libFileName).use {
                if (it != null) {
                    Files.copy(it, tempPath, StandardCopyOption.REPLACE_EXISTING)
                    System.load(tempPath.toAbsolutePath().toString())
                } else {
                    logger.regularErr("Error loading WebRTC library from JAR: cannot find $libFileName")
                    return false
                }
            }
        } catch (e: java.lang.Exception) {
            logger.regularErr("Error loading WebRTC library from JAR: ${e.message}")
            tempFile.delete()
            return false
        }

        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            // Assume POSIX compliant file system, library can be deleted after loading.
            tempFile.delete()
        } else {
            tempFile.deleteOnExit()
        }

        return true
    }

    /**
     * Prevents the WebRTC library from trying to automatically resolve and load native libraries
     * by manually registering the architecture-specific one as already loaded.
     *
     * This bypasses the default behavior of the `NativeLoader` class using reflections.
     */
    @Suppress("UNCHECKED_CAST")
    private fun preventWebrtcLibraryFromTryingToLoadOnSystem() {
        val libSetField: Field = NativeLoader::class.java.getDeclaredField("LOADED_LIB_SET")
        libSetField.isAccessible = true
        val loadedLibSet = libSetField.get(null) as ConcurrentHashMap.KeySetView<String, Boolean>
        loadedLibSet.add("webrtc-java")
    }
}
