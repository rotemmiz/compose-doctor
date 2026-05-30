package dev.composedoctor.plugin

import dev.composedoctor.scoring.Finding
import dev.composedoctor.scoring.Severity

/** Keeps only the findings that count toward the score, per the per-engine [EngineLevel]. */
object EngineFilter {

    fun keep(findings: List<Finding>, detekt: EngineLevel, compose: EngineLevel): List<Finding> =
        findings.filter { f ->
            when (levelFor(f, detekt, compose)) {
                EngineLevel.OFF -> false
                EngineLevel.ERRORS -> f.severity == Severity.ERROR
                EngineLevel.AS_IS, EngineLevel.ERRORS_AND_WARNINGS -> f.severity != Severity.INFO
            }
        }

    /** compose-rules findings carry the "Compose" rule set; everything else is detekt. */
    private fun levelFor(f: Finding, detekt: EngineLevel, compose: EngineLevel): EngineLevel =
        if (f.ruleSet == "Compose") compose else detekt
}
