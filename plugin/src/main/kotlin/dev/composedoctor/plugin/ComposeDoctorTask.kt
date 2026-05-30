package dev.composedoctor.plugin

import dev.composedoctor.scoring.Scorer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Aggregates SARIF reports into a single Compose health score. */
abstract class ComposeDoctorTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sarifReports: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val failBelow: Property<Int>

    @get:OutputFile
    @get:Optional
    abstract val reportJson: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val historyFile: RegularFileProperty

    @TaskAction
    fun run() {
        val findings = sarifReports.files
            .filter { it.exists() }
            .flatMap { SarifReader.read(it) }

        val score = Scorer.score(findings)
        logger.lifecycle(ScoreReport.render(score, findings))

        reportJson.orNull?.asFile?.let { f ->
            f.parentFile?.mkdirs()
            f.writeText(ScoreJsonWriter.toJson(score, findings))
        }
        historyFile.orNull?.asFile?.let { HistoryStore.append(it, score) }

        val gate = failBelow.orNull
        if (gate != null && score.overall < gate) {
            throw GradleException(
                "compose-doctor score ${score.overall} is below the required minimum of $gate",
            )
        }
    }
}
