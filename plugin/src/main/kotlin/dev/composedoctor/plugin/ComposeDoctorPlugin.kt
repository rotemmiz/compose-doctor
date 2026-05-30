package dev.composedoctor.plugin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the `composeDoctor` task and, by default, wires up detekt + compose-rules so the
 * task has something to score.
 *
 * Pipeline: detekt (with the `io.nlopez.compose.rules` ruleset) emits SARIF -> [ComposeDoctorTask]
 * aggregates every SARIF report -> normalized findings -> 0-100 health score.
 *
 * Set `composeDoctor { autoConfigureDetekt.set(false) }` to manage detekt yourself and only
 * aggregate the reports you point at via `sarifReports`.
 */
class ComposeDoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val ext = target.extensions.create("composeDoctor", ComposeDoctorExtension::class.java)
        ext.autoConfigureDetekt.convention(true)
        ext.reportJson.convention(
            target.layout.buildDirectory.file("reports/compose-doctor/score.json"),
        )
        ext.stateFile.convention(
            target.layout.projectDirectory.file(".compose-doctor/last-run.json"),
        )
        ext.historyFile.convention(
            target.layout.projectDirectory.file(".compose-doctor/history.jsonl"),
        )

        val task = target.tasks.register("composeDoctor", ComposeDoctorTask::class.java) { t ->
            t.group = "verification"
            t.description = "Aggregates SARIF reports into a Compose health score."
            t.sarifReports.from(ext.sarifReports)
            t.failBelow.set(ext.failBelow)
            t.reportJson.set(ext.reportJson)
            t.stateFile.set(ext.stateFile)
            t.historyFile.set(ext.historyFile)
            t.rulesetVersion.set("compose-rules $COMPOSE_RULES_VERSION · detekt $DETEKT_VERSION")
            t.projectDir.set(target.layout.projectDirectory)
        }

        target.afterEvaluate {
            if (ext.autoConfigureDetekt.get()) {
                configureDetekt(target, task)
            }
        }
    }

    private fun configureDetekt(
        target: Project,
        composeDoctor: org.gradle.api.tasks.TaskProvider<ComposeDoctorTask>,
    ) {
        target.pluginManager.apply("io.gitlab.arturbosch.detekt")
        target.dependencies.add(
            "detektPlugins",
            "io.nlopez.compose.rules:detekt:$COMPOSE_RULES_VERSION",
        )

        target.extensions.configure(DetektExtension::class.java) { d ->
            d.buildUponDefaultConfig = true
            d.config.setFrom(DetektConfig.materialize(target))
        }

        val detektTasks = target.tasks.withType(Detekt::class.java)
        detektTasks.configureEach { t ->
            t.reports.sarif.required.set(true)
            // Report findings instead of failing the analysis task, so we can score them.
            t.ignoreFailures = true
        }

        composeDoctor.configure { t ->
            t.dependsOn(detektTasks)
            t.sarifReports.from(
                target.layout.buildDirectory.file("reports/detekt/detekt.sarif"),
            )
        }
    }

    companion object {
        const val COMPOSE_RULES_VERSION = "0.4.22"
        const val DETEKT_VERSION = "1.23.7"
    }
}
