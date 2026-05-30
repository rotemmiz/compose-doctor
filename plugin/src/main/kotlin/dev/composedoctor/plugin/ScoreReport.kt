package dev.composedoctor.plugin

/** Renders a human-readable summary of a run for the Gradle console. */
object ScoreReport {
    fun render(dto: ScoreJson): String = buildString {
        appendLine()
        appendLine("compose-doctor — health score: ${dto.score}/100  [${dto.label}]")
        appendLine("  unique error rules:   ${dto.uniqueErrorRules}")
        appendLine("  unique warning rules: ${dto.uniqueWarningRules}")
        appendLine("  total findings:       ${dto.totalFindings}")
        dto.delta?.let { d ->
            val sign = if (d.score >= 0) "+" else ""
            appendLine(
                "  Δ vs ${d.vs}:        $sign${d.score}  " +
                    "(cleared ${d.fixedRules.size}, new ${d.newRules.size})",
            )
        }
        appendLine()
        appendLine("  by dimension:")
        dto.dimensions.toSortedMap().forEach { (dim, sub) ->
            appendLine("    ${dim.padEnd(18)} $sub/100")
        }
        if (dto.byRule.isNotEmpty()) {
            appendLine()
            appendLine("  next fixes (most score per fix first):")
            dto.byRule.take(5).forEach { r ->
                appendLine("    +${r.scoreImpactIfCleared} ${r.ruleId} (×${r.count}) — ${r.fixHint ?: r.dimension}")
            }
        }
    }
}
