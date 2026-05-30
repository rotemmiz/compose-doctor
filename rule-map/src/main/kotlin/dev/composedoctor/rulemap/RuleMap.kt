package dev.composedoctor.rulemap

import dev.composedoctor.scoring.Dimension

/** Metadata about a rule the agent harness uses to plan and apply fixes. */
data class RuleInfo(
    val dimension: Dimension,
    val docsUrl: String? = null,
    val fixHint: String? = null,
    val autoFixable: Boolean = false,
)

/**
 * Maps an engine rule ID to its [RuleInfo]. The dimension is a display bucket only
 * (see docs/PLAN.md) — it does not affect the overall score. `docsUrl`/`fixHint` feed the
 * agent harness's `byRule` plan; see docs/AGENT-HARNESS.md.
 *
 * Intentionally *data, not logic*. Unknown rule IDs fall back to [Dimension.ARCHITECTURE] with
 * no hint, so a new upstream rule never silently disappears from the report.
 */
object RuleMap {
    private const val DOCS = "https://mrmans0n.github.io/compose-rules/rules/"

    private val map: Map<String, RuleInfo> = buildMap {
        // --- State / correctness ---
        put(
            "RememberMissing",
            RuleInfo(
                Dimension.STATE_CORRECTNESS,
                "$DOCS#state-should-be-remembered-in-composables",
                "Wrap the mutableStateOf(...) call in remember { }.",
            ),
        )
        put("RememberContentMissing", RuleInfo(Dimension.STATE_CORRECTNESS, fixHint = "Remember the derived/content state."))
        put(
            "MutableStateParam",
            RuleInfo(
                Dimension.STATE_CORRECTNESS,
                "$DOCS#do-not-use-mutablestate-as-a-parameter",
                "Hoist the state: pass the value plus an onValueChange: (T) -> Unit lambda, not a MutableState.",
            ),
        )
        put("MutableStateAutoboxing", RuleInfo(Dimension.STATE_CORRECTNESS, fixHint = "Use the primitive state type (e.g. mutableIntStateOf)."))
        put("LambdaParameterInRestartableEffect", RuleInfo(Dimension.STATE_CORRECTNESS, fixHint = "Wrap the lambda in rememberUpdatedState inside the effect."))

        // --- Performance ---
        put(
            "UnstableCollections",
            RuleInfo(
                Dimension.PERFORMANCE,
                fixHint = "Use ImmutableList/PersistentList (kotlinx.collections.immutable) for the parameter.",
            ),
        )
        put("ContentEmitterReturningValues", RuleInfo(Dimension.PERFORMANCE, fixHint = "A composable should emit content OR return a value, not both."))
        put("MultipleEmitters", RuleInfo(Dimension.PERFORMANCE, fixHint = "Emit a single root element from the composable."))

        // --- Architecture / API shape ---
        put(
            "ComposableNaming",
            RuleInfo(
                Dimension.ARCHITECTURE,
                "$DOCS#naming-composable-functions-properly",
                "Rename the UI-emitting composable to PascalCase.",
            ),
        )
        put(
            "ComposableParamOrder",
            RuleInfo(
                Dimension.ARCHITECTURE,
                "$DOCS#ordering-composable-parameters-properly",
                "Reorder params: required first, then modifier, then optionals.",
            ),
        )
        put(
            "ModifierMissing",
            RuleInfo(
                Dimension.ARCHITECTURE,
                "$DOCS#when-should-i-expose-modifier-parameters",
                "Add a modifier: Modifier = Modifier parameter and pass it to the root emitter.",
            ),
        )
        put(
            "ModifierWithoutDefault",
            RuleInfo(
                Dimension.ARCHITECTURE,
                "$DOCS#modifiers-should-have-default-parameters",
                "Give the modifier parameter a default value: modifier: Modifier = Modifier.",
            ),
        )
        put(
            "ViewModelForwarding",
            RuleInfo(
                Dimension.ARCHITECTURE,
                "$DOCS#hoist-all-the-things",
                "Don't pass the ViewModel down; hoist the state and callbacks the child actually needs.",
            ),
        )
        put("ViewModelInjection", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Inject the ViewModel only at the screen level (e.g. viewModel())."))
        put("CompositionLocalAllowlist", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Avoid this CompositionLocal or add it to the allowlist."))
        put("ModifierReused", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Don't reuse the same modifier on multiple elements."))
        put("ModifierComposable", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Make the Modifier factory non-composable."))
        put("ModifierNaming", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Name the modifier parameter 'modifier'."))
        put("MutableParams", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Use an immutable parameter type."))
        put("ParameterNaming", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Follow Compose parameter naming conventions."))
        put("DefaultsVisibility", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Make the defaults object's visibility match the composable."))
        put("ContentTrailingLambda", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Make the content slot the trailing lambda."))
        put("PreviewPublic", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Make the @Preview composable private."))
        put("Material2", RuleInfo(Dimension.ARCHITECTURE, fixHint = "Migrate from Material 2 to Material 3."))

        // --- Accessibility (android-lint) ---
        put("ContentDescription", RuleInfo(Dimension.ACCESSIBILITY, fixHint = "Add a contentDescription (or null for decorative elements)."))
        put("ClickableViewAccessibility", RuleInfo(Dimension.ACCESSIBILITY))
        put("KeyboardInaccessibleWidget", RuleInfo(Dimension.ACCESSIBILITY))

        // --- Security (android-lint) ---
        put("HardcodedDebugMode", RuleInfo(Dimension.SECURITY))
        put("UnsafeImplicitIntentLaunch", RuleInfo(Dimension.SECURITY))
    }

    fun infoFor(ruleId: String): RuleInfo = map[ruleId] ?: RuleInfo(Dimension.ARCHITECTURE)

    fun dimensionFor(ruleId: String): Dimension = infoFor(ruleId).dimension

    fun isKnown(ruleId: String): Boolean = ruleId in map
}
