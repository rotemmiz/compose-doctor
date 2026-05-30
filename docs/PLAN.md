# compose-doctor — Implementation Plan

> **Status note (historical doc).** This is the original design plan. For *current* behavior see
> [README](../README.md), [RULES.md](RULES.md), and [AGENT-HARNESS.md](AGENT-HARNESS.md). Some
> specifics below evolved: there is no `composeDoctorBaseline` task yet and no merged
> `compose-doctor.sarif` (we read detekt's SARIF directly); config is detekt-native
> (`config/detekt/detekt.yml`) rather than a single bundled file; trend history is
> `.compose-doctor/history.jsonl`; per-engine strictness is set via `composeDoctor { detekt/compose
> = EngineLevel.* }`; android-lint (Security/Accessibility) is still pending.

## Context

There is no single drop-in equivalent of [React Doctor](https://github.com/millionco/react-doctor) / [react.doctor](https://www.react.doctor/) for Android Jetpack Compose. The pieces exist but are **unbundled**: deterministic static analysis lives in `compose-rules` (mrmans0n) and Slack's `compose-lints`, run via detekt/ktlint/android-lint; there are scattered AI agent "compose skills"; but **nobody ships the React Doctor package** — one tool that produces a deterministic **0–100 health score**, gives an **agent a structured fix loop**, and drops a **CI/PR gate** in.

`compose-doctor` fills that gap. It is greenfield. The differentiator is explicitly **not** the rules — it is the *score + agent loop + CI bundle* wrapped around battle-tested engines.

### Key decisions (locked)

| Decision | Choice | Why |
|---|---|---|
| Form factor | **Gradle plugin only** — tasks run via `./gradlew composeDoctor`. No standalone CLI / JBang / Homebrew. | A plugin gets the compiled classpath (full type resolution), correct multi-module source sets, android-lint access, and Gradle build caching **for free**. The agent and CI just run the task. |
| Engines (wrap, don't rebuild) | **detekt + `compose-rules`** (PSI + type resolution) · **AGP android-lint** for accessibility/security checks · **ktlint** (via detekt) for autofixable formatting. Aggregate via **SARIF**. | compose-rules encodes years of stability analysis; android-lint owns a11y/security. Our value is the layer on top, not the rules. detekt-api is reserved for *authoring* future native gap-filler rules. |
| Scoring | **React Doctor's formula** (see below), unique-rule based, **version-pinned** ruleset. | Deterministic by construction; needs no size normalization and no calibration corpus. |
| Dimensions | **Display buckets only**, not score weights. | Mirrors React Doctor — the overall score is one flat formula; dimensions just group findings for the report. |
| Baseline / config | **Reuse detekt's** `baseline.xml`, `detekt.yml`, and `@Suppress`. | Existing codebases need to accept current debt or the tool gets uninstalled on day one. Nothing new to build. |
| v1 focus | The `composeDoctor` aggregate task + score + SARIF. | Everything (agent loop, CI gate) consumes its output. |
| Scope | **Android Jetpack Compose first**; source-set handling left pluggable for Multiplatform later. | |

## How it works

`compose-doctor` is a Gradle plugin that orchestrates existing analysis tasks and aggregates their output:

1. **Configure & run engines** as standard Gradle tasks (cacheable, per-module):
   - detekt with the `io.nlopez.compose.rules:detekt` ruleset (type resolution on, via the build classpath),
   - AGP `lint` for Compose accessibility/security checks,
   - ktlint (through detekt) for autofixable formatting.
2. **Each engine emits SARIF.** The plugin's aggregation task reads every module's SARIF report — no embedding of detekt-core, no custom runner.
3. **Normalize** all findings into a unified model:
   `Finding(ruleId, dimension, severity, filePath, line, message, engine)`.
   A maintained **rule → dimension** map assigns each ruleId to one display bucket (State/Correctness, Performance, Architecture, Security, Accessibility).
4. **Score** (below) and **render**: terminal summary, a merged `compose-doctor.sarif`, and `--json` equivalent output for agents/CI.

### Module layout (Gradle composite)

- `plugin/` — the Gradle plugin: registers `composeDoctor` (+ `composeDoctorBaseline`), wires the engine tasks, owns aggregation/scoring/reporting.
- `scoring/` — pure, deterministic scoring function `List<Finding> → Score`. Unit-tested for reproducibility.
- `rule-map/` — the maintained ruleId → dimension + default-severity taxonomy (data, not logic).
- `skill/` — agent skill (`SKILL.md`) teaching an agent to run the task, read SARIF, and fix iteratively.
- `.github/` — reusable GitHub Action that runs `./gradlew composeDoctor` and posts results.

## Scoring (the differentiator)

Translated directly from React Doctor:

```
score = 100 − (uniqueErrorRuleIds × 1.5) − (uniqueWarningRuleIds × 0.75)     // clamped to [0, 100]
```

- **Unit = unique rule IDs triggered**, NOT instance count and NOT per-KLOC. Fixing 49 of 50 violations of a rule does not move the score; clearing the last one removes that rule's penalty. (This is what makes it deterministic without calibration — and what makes the agent loop "clear one rule at a time" effective.)
- `uniqueErrorRuleIds` / `uniqueWarningRuleIds` = distinct ruleIds with ≥1 finding at that severity, across all engines. Severity comes from each engine's config (`detekt.yml`, lint severity); info-level is excluded.
- Labels: **75+ Great · 50–74 Needs work · <50 Critical**.
- Optional per-dimension sub-scores = same formula restricted to that bucket's rules.
- **Version-pin the ruleset.** Adding rules can lower the score for unchanged code, so the score is comparable only within a ruleset version; a bump is a deliberate, announced event.

## Autofix & agent output

- **Where autofix exists, use it:** run detekt with `autoCorrect` for rules that support it (formatting / ktlint-wrapper rules).
- **Everything else → structured SARIF**, which is exactly what an agent consumes to fix iteratively, and what GitHub code-scanning ingests natively. The agent skill loop: run `composeDoctor` → read SARIF findings → fix the highest-value rule → re-run → watch the score climb as each unique rule clears.

## Performance

Lean on Gradle, don't reinvent: per-module detekt/lint tasks are **cacheable** with up-to-date checks, so unchanged modules are skipped; keep tasks **configuration-cache compatible**. The aggregation task only depends on each module's (cached) SARIF output.

## Trend tracking

Write each run's score to `.compose-doctor/history.jsonl` (committable). The task prints the delta vs the previous run. In CI, compare the PR's score against the base branch and include it in the sticky comment.

## Build phases

**Phase 1 — `composeDoctor` task + score**
- Scaffold the Gradle plugin + `scoring` + `rule-map` modules.
- Wire detekt+compose-rules (type resolution) and android-lint as per-module tasks emitting SARIF.
- Aggregate SARIF → `Finding` model; implement the scoring function with determinism tests.
- Render terminal summary + merged SARIF + JSON.
- Reuse detekt baseline/config; add a `composeDoctorBaseline` task.

**Phase 2 — Agent skill**
- `SKILL.md`: Compose best-practice guidance + the run→read-SARIF→fix→re-run loop.

**Phase 3 — CI / PR gate**
- GitHub Action running `./gradlew composeDoctor`: upload SARIF (code-scanning annotations), post a sticky PR comment with score + delta vs base, fail on a configurable threshold.

## Risks & open questions

- **Score meaningfulness** — the formula is deterministic, but whether the *number* tracks real quality still needs a sanity check against a handful of real Compose repos. Lowest-confidence assumption.
- **Ruleset version drift** — pinning keeps scores comparable but means upgrades visibly move scores; needs a clear changelog/migration story.
- **android-lint cost** — `lint` is heavier than detekt; may make it opt-in (`--with-lint`) if it dominates runtime on large repos.
- **Diff/PR scoping** — analyzing only changed files can miss issues whose cause is in unchanged files (changed call site, unchanged composable). Default to whole-module analysis with caching rather than file-level diffing.
- **Multiplatform source sets** — deferred; current discovery assumes Android source sets.

## Verification

- Fixture Compose module seeded with known anti-patterns (ViewModel passed to a child composable, `mutableStateOf` without `remember`, unstable list param, `Modifier` ordering, a missing `contentDescription` for the lint/a11y path).
- Tests assert: (a) each anti-pattern produces its expected `Finding` + correct dimension; (b) the score is byte-identical across repeated runs (determinism); (c) clearing the last instance of a rule increases the score, partial fixes do not.
- Manual end-to-end: run `./gradlew composeDoctor` on the fixture and a real OSS Compose app; confirm the terminal summary, merged SARIF, and 0–100 score are sensible.
