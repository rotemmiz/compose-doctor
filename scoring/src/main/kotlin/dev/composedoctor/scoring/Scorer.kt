package dev.composedoctor.scoring

import kotlin.math.roundToInt

/**
 * Deterministic health scoring, translated directly from React Doctor:
 *
 * ```
 * score = 100 − (uniqueErrorRules × 1.5) − (uniqueWarningRules × 0.75)
 * ```
 *
 * The unit is the number of *unique rule IDs* triggered — not instances, and not normalized
 * by code size. Fixing all but one instance of a rule does not change the score; clearing the
 * last instance removes that rule's penalty entirely. This is what makes the score
 * deterministic without a calibration corpus, and what makes the agent loop
 * ("clear one rule at a time") effective.
 *
 * A rule's effective severity is the most severe severity observed for it, so a rule is
 * penalised once — as either an error or a warning — never twice.
 */
object Scorer {
    private const val ERROR_WEIGHT = 1.5
    private const val WARNING_WEIGHT = 0.75

    fun score(findings: List<Finding>): Score {
        val (errorRules, warningRules) = partitionRules(findings)

        val perDimension = Dimension.entries.associateWith { dim ->
            val (e, w) = partitionRules(findings.filter { it.dimension == dim })
            compute(e.size, w.size)
        }

        val overall = compute(errorRules.size, warningRules.size)
        return Score(
            overall = overall,
            label = ScoreLabel.forScore(overall),
            uniqueErrorRules = errorRules.size,
            uniqueWarningRules = warningRules.size,
            perDimension = perDimension,
        )
    }

    private fun compute(errorRules: Int, warningRules: Int): Int =
        (100.0 - errorRules * ERROR_WEIGHT - warningRules * WARNING_WEIGHT)
            .coerceIn(0.0, 100.0)
            .roundToInt()

    /** (uniqueErrorRuleIds, uniqueWarningRuleIds), each rule counted by its most severe finding. */
    private fun partitionRules(findings: List<Finding>): Pair<Set<String>, Set<String>> {
        val effective: Map<String, Severity> = findings
            .groupBy { it.ruleId }
            .mapValues { (_, fs) -> fs.minOf { it.severity } } // ERROR has the lowest ordinal
        return effective.filterValues { it == Severity.ERROR }.keys to
            effective.filterValues { it == Severity.WARNING }.keys
    }
}
