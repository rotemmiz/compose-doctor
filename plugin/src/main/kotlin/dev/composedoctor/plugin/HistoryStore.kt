package dev.composedoctor.plugin

import dev.composedoctor.scoring.Score
import java.io.File
import java.time.Instant

/** Appends one JSON line per run for local trend tracking (committable). */
object HistoryStore {
    fun append(file: File, score: Score) {
        file.parentFile?.mkdirs()
        val line = buildString {
            append("{")
            append("\"timestamp\":\"${Instant.now()}\",")
            append("\"score\":${score.overall},")
            append("\"errorRules\":${score.uniqueErrorRules},")
            append("\"warningRules\":${score.uniqueWarningRules}")
            append("}")
        }
        file.appendText(line + "\n")
    }
}
