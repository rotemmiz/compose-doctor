# compose-doctor

A deterministic health-check tool for Android Jetpack Compose codebases — the "React Doctor" equivalent for Compose. Ships as a **Gradle plugin** that produces a **0–100 health score**, emits **SARIF** for an agent fix-loop, and powers a **CI/PR gate**, wrapped around `compose-rules` (mrmans0n) + detekt + android-lint.

> Full design lives in [`docs/PLAN.md`](docs/PLAN.md). Read it before making architectural changes.

## Status

Greenfield. Phase 1 (`composeDoctor` task + score) not yet scaffolded.

## Architecture (planned)

Gradle plugin only — no standalone CLI. The plugin orchestrates existing engines as cacheable Gradle tasks and aggregates their **SARIF** output (it does not embed detekt-core).

- `plugin/` — registers `composeDoctor` (+ `composeDoctorBaseline`); wires detekt+`compose-rules` (type resolution) and AGP `lint`; owns aggregation/scoring/reporting.
- `scoring/` — pure, deterministic scoring function. Must be reproducible; cover with tests.
- `rule-map/` — maintained ruleId → dimension + default-severity taxonomy (data, not logic).
- `skill/` — agent skill (`SKILL.md`): run task → read SARIF → fix one rule → re-run.
- `.github/` — reusable GitHub Action running `./gradlew composeDoctor`.

## Scoring

```
score = 100 − (uniqueErrorRuleIds × 1.5) − (uniqueWarningRuleIds × 0.75)   // clamped [0,100]
```

Counts **unique rules triggered, not instances; no size normalization** (translated from React Doctor). Labels: 75+ Great · 50–74 Needs work · <50 Critical. Pin the ruleset version so scores stay comparable.

## Conventions

- **Determinism is a hard requirement** for `scoring/` — same input must always yield the same score.
- Don't reimplement rules; wrap compose-rules/detekt/android-lint, extend natively only for genuine gaps.
- Reuse detekt's `baseline.xml` / `detekt.yml` / `@Suppress` for debt and config — don't build a parallel system.
- Dimensions are **display buckets**, not score weights.
- Tech: Kotlin, Gradle plugin, detekt + `io.nlopez.compose.rules`, AGP lint, SARIF, kotlinx.serialization.

## Common commands (once scaffolded)

```bash
./gradlew composeDoctor           # run the health check + score
./gradlew composeDoctorBaseline   # accept current findings as baseline
./gradlew test                    # tests (incl. scoring determinism)
```
