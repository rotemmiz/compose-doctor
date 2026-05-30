package dev.composedoctor.plugin

import org.gradle.api.Project
import java.io.File

/**
 * Extracts compose-doctor's bundled policy from the plugin jar to the build directory so it can be
 * handed to detekt as config files: a base scope policy, plus per-engine severity overlays that are
 * only applied when that engine's [EngineLevel] isn't `as-is`.
 */
object DetektConfig {

    fun materialize(
        project: Project,
        applyDetektSeverities: Boolean,
        applyComposeSeverities: Boolean,
    ): List<File> = buildList {
        add(extract(project, "base.yml"))
        if (applyDetektSeverities) add(extract(project, "detekt-severities.yml"))
        if (applyComposeSeverities) add(extract(project, "compose-severities.yml"))
    }

    private fun extract(project: Project, name: String): File {
        val out = project.layout.buildDirectory.file("compose-doctor/$name").get().asFile
        out.parentFile?.mkdirs()
        val resource = javaClass.getResourceAsStream("/policy/$name")
            ?: error("bundled policy /policy/$name not found on the plugin classpath")
        resource.use { input -> out.outputStream().use(input::copyTo) }
        return out
    }
}
