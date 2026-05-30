package dev.composedoctor.plugin

import dev.composedoctor.scoring.Scorer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Aggregates SARIF reports into a single Compose health score and machine-readable report. */
abstract class ComposeDoctorTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sarifReports: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val failBelow: Property<Int>

    @get:Input
    abstract val rulesetVersion: Property<String>

    @get:Internal
    abstract val projectDir: DirectoryProperty

    @get:OutputFile
    @get:Optional
    abstract val reportJson: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val stateFile: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val historyFile: RegularFileProperty

    @TaskAction
    fun run() {
        val findings = sarifReports.files
            .filter { it.exists() }
            .flatMap { SarifReader.read(it) }

        val score = Scorer.score(findings)
        val gate = failBelow.orNull
        val belowGate = gate != null && score.overall < gate
        val status = if (belowGate) "below_gate" else "ok"

        val previous = stateFile.orNull?.asFile?.let { RunStateStore.read(it) }
        val dto = ScoreModel.build(
            score = score,
            findings = findings,
            rulesetVersion = rulesetVersion.getOrElse("unknown"),
            status = status,
            previous = previous,
            projectDir = projectDir.get().asFile,
        )

        logger.lifecycle(ScoreReport.render(dto))

        reportJson.orNull?.asFile?.let { f ->
            f.parentFile?.mkdirs()
            f.writeText(ScoreModel.toJsonString(dto))
        }
        stateFile.orNull?.asFile?.let { f ->
            RunStateStore.write(f, RunState(score.overall, dto.byRule.map { it.ruleId }))
        }
        historyFile.orNull?.asFile?.let { HistoryStore.append(it, score) }

        if (belowGate) {
            throw GradleException(
                "compose-doctor score ${score.overall} is below the required minimum of $gate",
            )
        }
    }
}
