package dev.composedoctor.plugin

import org.gradle.api.Project
import java.io.File

/**
 * Extracts the bundled detekt config (which enables the compose-rules `Compose` ruleset) from the
 * plugin jar to the build directory, so it can be handed to the detekt extension as a config file.
 */
object DetektConfig {
    private const val RESOURCE = "/compose-doctor-detekt.yml"

    fun materialize(project: Project): File {
        val out = project.layout.buildDirectory.file("compose-doctor/detekt.yml").get().asFile
        out.parentFile?.mkdirs()
        val resource = javaClass.getResourceAsStream(RESOURCE)
            ?: error("bundled detekt config $RESOURCE not found on the plugin classpath")
        resource.use { input -> out.outputStream().use(input::copyTo) }
        return out
    }
}
