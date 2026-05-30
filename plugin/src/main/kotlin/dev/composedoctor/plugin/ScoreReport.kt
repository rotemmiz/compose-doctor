package dev.composedoctor.plugin

import dev.composedoctor.scoring.Finding
import dev.composedoctor.scoring.Score

/** Renders a human-readable summary of a [Score] for the Gradle console. */
object ScoreReport {
    fun render(score: Score, findings: List<Finding>): String = buildString {
        appendLine()
        appendLine("compose-doctor — health score: ${score.overall}/100  [${score.label}]")
        appendLine("  unique error rules:   ${score.uniqueErrorRules}")
        appendLine("  unique warning rules: ${score.uniqueWarningRules}")
        appendLine("  total findings:       ${findings.size}")
        appendLine()
        appendLine("  by dimension:")
        score.perDimension.toSortedMap().forEach { (dim, sub) ->
            appendLine("    ${dim.name.padEnd(18)} $sub/100")
        }
    }
}
