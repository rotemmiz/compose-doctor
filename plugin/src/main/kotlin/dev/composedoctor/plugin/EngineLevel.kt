package dev.composedoctor.plugin

/**
 * How much an engine contributes to the score. compose-doctor is a thin wrapper over the engines:
 * the engines own *which rules exist* (via their native configs); this dial owns *how strict the
 * score is* per engine.
 */
enum class EngineLevel {
    /** Exclude this engine from the score entirely. */
    OFF,

    /** Trust the engine's own severities — skip compose-doctor's curated error/warning remap. */
    AS_IS,

    /** Apply compose-doctor's curated tiers, then count only the error tier (genuine bugs). */
    ERRORS,

    /** Apply compose-doctor's curated tiers, count errors and warnings. The default. */
    ERRORS_AND_WARNINGS,
    ;

    /** Whether compose-doctor's curated severity overlay should be applied for this level. */
    val appliesSeverityOverlay: Boolean get() = this == ERRORS || this == ERRORS_AND_WARNINGS
}
