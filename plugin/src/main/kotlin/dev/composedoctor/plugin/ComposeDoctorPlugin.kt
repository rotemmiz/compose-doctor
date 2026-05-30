package dev.composedoctor.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the `composeDoctor` task and its configuration extension.
 *
 * Phase 1 scope: the task aggregates SARIF reports (produced by detekt + android-lint),
 * scores them, prints a report, and optionally gates the build. Wiring of the underlying
 * detekt/lint tasks so they feed [ComposeDoctorExtension.sarifReports] automatically is the
 * next step.
 */
class ComposeDoctorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ext = target.extensions.create("composeDoctor", ComposeDoctorExtension::class.java)
        ext.historyFile.convention(
            target.layout.projectDirectory.file(".compose-doctor/history.jsonl"),
        )

        target.tasks.register("composeDoctor", ComposeDoctorTask::class.java) { task ->
            task.group = "verification"
            task.description = "Aggregates SARIF reports into a Compose health score."
            task.sarifReports.setFrom(ext.sarifReports)
            task.failBelow.set(ext.failBelow)
            task.historyFile.set(ext.historyFile)
        }
    }
}
