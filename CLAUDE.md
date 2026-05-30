# compose-doctor

A deterministic health-check tool for Android Jetpack Compose codebases — the "React Doctor" equivalent for Compose. Ships as a **Gradle plugin** that produces a **0–100 health score**, emits **SARIF** for an agent fix-loop, and powers a **CI/PR gate**, wrapped around `compose-rules` (mrmans0n) + detekt + android-lint.

> Full design lives in [`docs/PLAN.md`](docs/PLAN.md). Read it before making architectural changes.

## Status

All three plan phases are implemented and CI is green:

- **Phase 1** — the plugin applies detekt + compose-rules, emits SARIF, scores it, and writes a
  machine-readable `score.json`. Verified against `playground/` (a deliberately-broken module).
- **Phase 2** — agent skill at `skill/compose-doctor/SKILL.md` (run → read SARIF → fix loop).
- **Phase 3** — `.github/workflows/compose-doctor.yml` is a reusable gate (uploads SARIF for
  code-scanning, posts a sticky PR score comment, fails below a threshold); `ci.yml` builds +
  tests + smoke-runs the playground; `pr-gate.yml` dogfoods the gate. Verified green end-to-end
  (PR posted "95/100 GREAT" and passed the gate).

Publishing infra is wired but nothing is published: the plugin module applies
`com.gradle.plugin-publish` with full metadata, `publishToMavenLocal` works, and
`.github/workflows/release.yml` is a manual-only release (defaults to a credential-free dry run).
To actually release: bump the version off `-SNAPSHOT` and add `GRADLE_PUBLISH_KEY`/`SECRET` secrets.

Remaining follow-ups (non-blocking): android-lint wiring (a11y/security dimensions; needs the
Android SDK) and a `composeDoctorBaseline` task to seed detekt's `baseline.xml`.

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

## Contributing workflow

All changes go through a branch → PR → green CI → merge. **Never push directly to `main`.**
Open a PR, wait for the `ci` and `pr-gate` checks to pass, then merge (squash) and delete the
branch. Commits and PRs carry no Claude co-authorship.

## Modules

- `scoring/` — pure `Scorer` + model. `rule-map/` — `ruleId → Dimension` taxonomy.
- `plugin/` — `dev.composedoctor` plugin: applies detekt + the `io.nlopez.compose.rules`
  ruleset (config bundled at `plugin/src/main/resources/compose-doctor-detekt.yml`), reads the
  detekt SARIF (`SarifReader`), scores it, reports, writes trend history, and gates via `failBelow`.
- `playground/` — standalone composite build (`includeBuild("..")`) of deliberately-broken
  composables, used to verify detection against the plugin sources.

## Common commands

```bash
./gradlew build                          # compile + all hermetic tests
./gradlew test                           # tests only (incl. scoring determinism)
./gradlew -p playground composeDoctor    # run the real scan against the broken playground
```

The hermetic plugin tests set `autoConfigureDetekt.set(false)`; the playground exercises the
full detekt + compose-rules path (needs network to resolve the ruleset).
