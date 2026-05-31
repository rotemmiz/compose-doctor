# AGENTS.md — compose-doctor

Cross-tool agent instructions, read automatically by Codex, OpenCode, Google Antigravity, Cursor,
and other agents that honor `AGENTS.md`. This is the neutral mirror of the Claude Code skill at
[`skills/compose-doctor/SKILL.md`](skills/compose-doctor/SKILL.md) — that file is the source of
truth; read it for the full detail.

## What compose-doctor is

A deterministic health check for Android Jetpack Compose. It runs detekt + the
`io.nlopez.compose.rules` ruleset and scores the result 0–100. The tool **only diagnoses** — it
never edits code. You are the actuator: read the diagnosis, make one targeted fix, re-run.

Use it whenever you write, review, or fix Compose code (`@Composable` functions, `Modifier` usage,
`state`/`remember`, ViewModels).

## The fix loop

0. **Find or bootstrap the task.** Check whether the `composeDoctor` task exists:
   ```bash
   ./gradlew tasks --all 2>/dev/null | grep -q composeDoctor && echo applied || echo missing
   ```
   - **applied** → run it (step 1).
   - **missing** → bootstrap it without editing any build file, using the bundled init script:
     ```bash
     ./gradlew --init-script ./init/compose-doctor.init.gradle.kts composeDoctor
     ```
     This resolves the plugin from the Gradle Plugin Portal (works once compose-doctor is
     published). If resolution fails because it isn't published yet, fall back to applying
     `plugins { id("dev.composedoctor") }` to the target module's `build.gradle.kts` (or a composite
     `includeBuild` of the compose-doctor checkout — see the README) and re-run.

1. **Run it.**
   ```bash
   ./gradlew composeDoctor                  # whole project
   ./gradlew :feature:home:composeDoctor    # a single module, if applied there
   ```
2. **Read the machine-readable report**, not the console:
   - `build/reports/compose-doctor/score.json` — `score`, `status` (`ok`/`below_gate`), per-dimension
     sub-scores, a **`byRule`** remediation plan (pre-sorted by score-per-fix), a **`delta`** vs the
     previous run, and the `findings` array (each with a stable `id`).
   - `build/reports/detekt/detekt.sarif` — same findings with precise locations.
3. **Take `byRule[0]`** (highest score-per-fix). The score counts *unique rule IDs*, not instances
   (`score = 100 − uniqueErrorRules×1.5 − uniqueWarningRules×0.75`), so **fix every instance of that
   one rule** — partial fixes score nothing.
4. **Re-run and verify**: `delta.fixedRules` contains the rule you targeted, the score went up, and
   `delta.newRules` is empty. Then **also run `:module:compileKotlin` / tests** — a Compose fix that
   changes a signature can break call sites; a higher score with a broken build is a regression, so
   revert if so.
5. Repeat until the score is satisfactory (75+ is "Great") or the remaining rules need human judgement.

## Integrity (non-negotiable)

**Fix, don't suppress.** Do not add `@Suppress`, edit the baseline, or disable rules in `detekt.yml`
to raise the score unless the user explicitly approved it — and if so, leave a justification comment
at the suppression site. Gaming the score is a failure, not a win.

## Writing good Compose in the first place

See [`skills/compose-doctor/SKILL.md`](skills/compose-doctor/SKILL.md) for the full list. The common
flags: PascalCase `Unit`-returning composables (`ComposableNaming`); accept `modifier: Modifier =
Modifier` (`ModifierMissing`/`ModifierWithoutDefault`); wrap state in `remember { mutableStateOf() }`
(`RememberMissing`); hoist state instead of passing `MutableState` (`MutableStateParam`); don't
forward ViewModels (`ViewModelForwarding`); prefer `ImmutableList` for recomposition-driving params
(`UnstableCollections`).
