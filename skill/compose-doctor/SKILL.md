---
name: compose-doctor
description: Use when writing, reviewing, or fixing Android Jetpack Compose code (@Composable functions, Modifier usage, state/remember, ViewModels). Runs the compose-doctor Gradle task to get a 0-100 health score and SARIF findings, then fixes them iteratively. Trigger on requests like "check my Compose code", "improve this composable", "why is this recomposing", or after generating Compose UI.
---

# compose-doctor

A deterministic health check for Jetpack Compose. It runs detekt + the
`io.nlopez.compose.rules` ruleset, then scores the result 0–100. Use it both to **diagnose** an
existing codebase and to **verify** Compose you just wrote.

## The fix loop

1. **Run it.**
   ```bash
   ./gradlew composeDoctor          # whole project
   ./gradlew :feature:home:composeDoctor   # a single module, if applied there
   ```
2. **Read the machine-readable report**, not just the console:
   - `build/reports/compose-doctor/score.json` — `score`, `label`, per-`dimension` sub-scores, and the full `findings` array (`ruleId`, `dimension`, `severity`, `file`, `line`, `message`).
   - `build/reports/detekt/detekt.sarif` — same findings in SARIF, with precise locations.
3. **Fix the highest-value rule first.** The score counts *unique rule IDs*, not instances:
   `score = 100 − uniqueErrorRules×1.5 − uniqueWarningRules×0.75`. So **fixing every instance of
   one rule raises the score; fixing only some instances does not.** Pick a `ruleId`, fix all of
   its findings, then move to the next.
4. **Re-run and confirm** the score went up and the rule is gone. Repeat until the score is
   satisfactory (75+ is "Great").

## Writing good Compose in the first place

Avoid these — they are exactly what the tool flags:

- **Composable naming:** UI-emitting `@Composable` functions return `Unit` and are **PascalCase**
  (`ProfileCard`, not `profileCard`). (`ComposableNaming`)
- **Modifier parameter:** any composable that emits UI should accept `modifier: Modifier =
  Modifier`, placed after required params and before other optionals, with a default.
  (`ModifierMissing`, `ModifierWithoutDefault`, `ComposableParamOrder`)
- **Remember your state:** never call `mutableStateOf(...)` directly in a composable body —
  wrap it: `var x by remember { mutableStateOf(...) }`. (`RememberMissing`)
- **Don't pass `MutableState` as a parameter.** Hoist state: pass the value down and an
  `onXChange: (T) -> Unit` lambda up. (`MutableStateParam`)
- **Don't forward ViewModels** through multiple composables. Pass the concrete state and
  callbacks the child needs, not the whole ViewModel. (`ViewModelForwarding`, `ViewModelInjection`)
- **Stable parameters:** prefer `ImmutableList`/`PersistentList` over `List` for params that drive
  recomposition. (`UnstableCollections`)

## Gating

To block a build below a threshold, configure the plugin:
```kotlin
composeDoctor { failBelow.set(75) }
```
Or in CI, use the reusable workflow at `.github/workflows/compose-doctor.yml` (uploads SARIF for
inline annotations and posts the score on the PR).

## Notes

- Findings come from compose-rules via detekt (PSI based) — they apply even when a module is not
  fully type-resolved. A few rules are skipped without type resolution; running through the Gradle
  plugin (which has the compiled classpath) gives the most complete results.
- compose-doctor does not auto-rewrite code: it gives you precise, structured findings; **you**
  apply the fix and re-run to confirm.
