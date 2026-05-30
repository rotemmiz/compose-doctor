package dev.composedoctor.scoring

/** Ordered most-severe first, so natural ordering can pick a rule's effective severity. */
enum class Severity { ERROR, WARNING, INFO }

/**
 * Display buckets for grouping findings in the report. These do NOT weight the overall
 * score — the score is a single flat formula across all rules (see [Scorer]).
 */
enum class Dimension {
    STATE_CORRECTNESS,
    PERFORMANCE,
    ARCHITECTURE,
    SECURITY,
    ACCESSIBILITY,
}

/** A single normalized diagnostic from any engine (detekt, android-lint, ...). */
data class Finding(
    val ruleId: String,
    val dimension: Dimension,
    val severity: Severity,
    val filePath: String,
    val line: Int,
    val message: String,
    val engine: String,
    /** The originating rule set, e.g. "Compose" or "potential-bugs"; used to classify the engine. */
    val ruleSet: String? = null,
)

enum class ScoreLabel(val min: Int) {
    GREAT(75),
    NEEDS_WORK(50),
    CRITICAL(0),
    ;

    companion object {
        fun forScore(score: Int): ScoreLabel = when {
            score >= GREAT.min -> GREAT
            score >= NEEDS_WORK.min -> NEEDS_WORK
            else -> CRITICAL
        }
    }
}

data class Score(
    val overall: Int,
    val label: ScoreLabel,
    val uniqueErrorRules: Int,
    val uniqueWarningRules: Int,
    val perDimension: Map<Dimension, Int>,
)
