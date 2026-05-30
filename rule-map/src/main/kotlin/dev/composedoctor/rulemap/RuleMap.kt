package dev.composedoctor.rulemap

import dev.composedoctor.scoring.Dimension

/**
 * Maps an engine rule ID to a display [Dimension]. Dimensions are display buckets only
 * (see docs/PLAN.md) — they do not affect the overall score weighting.
 *
 * This is intentionally *data, not logic*: a maintained taxonomy of the rules we wrap from
 * compose-rules (detekt) and android-lint. Unknown rule IDs fall back to
 * [Dimension.ARCHITECTURE] so a new upstream rule never silently disappears from the report.
 */
object RuleMap {
    private val map: Map<String, Dimension> = buildMap {
        // --- State / correctness (compose-rules) ---
        putAll(
            listOf(
                "RememberMissing",
                "RememberContentMissing",
                "MutableStateParam",
                "MutableStateAutoboxing",
                "LambdaParameterInRestartableEffect",
            ).associateWith { Dimension.STATE_CORRECTNESS },
        )

        // --- Performance (compose-rules) ---
        putAll(
            listOf(
                "UnstableCollections",
                "ContentEmitterReturningValues",
                "MultipleEmitters",
            ).associateWith { Dimension.PERFORMANCE },
        )

        // --- Architecture / API shape (compose-rules) ---
        putAll(
            listOf(
                "ViewModelForwarding",
                "ViewModelInjection",
                "CompositionLocalAllowlist",
                "ComposableNaming",
                "ComposableParamOrder",
                "ModifierMissing",
                "ModifierReused",
                "ModifierWithoutDefault",
                "ModifierComposable",
                "ModifierNaming",
                "MutableParams",
                "ParameterNaming",
                "DefaultsVisibility",
                "ContentTrailingLambda",
                "PreviewPublic",
                "Material2",
            ).associateWith { Dimension.ARCHITECTURE },
        )

        // --- Accessibility (android-lint) ---
        putAll(
            listOf(
                "ContentDescription",
                "ClickableViewAccessibility",
                "KeyboardInaccessibleWidget",
            ).associateWith { Dimension.ACCESSIBILITY },
        )

        // --- Security (android-lint) ---
        putAll(
            listOf(
                "HardcodedDebugMode",
                "UnsafeImplicitIntentLaunch",
            ).associateWith { Dimension.SECURITY },
        )
    }

    fun dimensionFor(ruleId: String): Dimension = map[ruleId] ?: Dimension.ARCHITECTURE

    fun isKnown(ruleId: String): Boolean = ruleId in map
}
