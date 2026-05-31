# compose-doctor

A deterministic health-check tool for Android Jetpack Compose codebases — the "React Doctor" equivalent for Compose. Ships as a **Gradle plugin** that produces a **0–100 health score**, emits **SARIF** for an agent fix-loop, and powers a **CI/PR gate**, wrapped around `compose-rules` (mrmans0n) + detekt (android-lint planned).

> Full design lives in [`docs/PLAN.md`](docs/PLAN.md). Read it before making architectural changes.

## Status

All three plan phases are implemented and CI is green:

- **Phase 1** — the plugin applies detekt + compose-rules, emits SARIF, scores it, and writes a
  machine-readable `score.json`. Verified against `playground/` (a deliberately-broken module).
- **Phase 2** — agent skill at `skills/compose-doctor/SKILL.md` (run → read SARIF → fix loop).
- **Phase 3** — `.github/workflows/compose-doctor.yml` is a reusable gate (uploads SARIF for
  code-scanning, posts a sticky PR score comment, fails below a threshold); `ci.yml` builds +
  tests + smoke-runs the playground; `pr-gate.yml` dogfoods the gate (report-only). Verified green
  end-to-end (the sticky comment posts the current playground score).

Publishing infra is wired but nothing is published: the plugin module applies
`com.gradle.plugin-publish` with full metadata, `publishToMavenLocal` works, and
`.github/workflows/release.yml` is a manual-only release (defaults to a credential-free dry run).
To actually release: bump the version off `-SNAPSHOT` and add `GRADLE_PUBLISH_KEY`/`SECRET` secrets.

Remaining follow-ups (non-blocking): android-lint wiring (a11y/security dimensions; needs the
Android SDK) and a `composeDoctorBaseline` task to seed detekt's `baseline.xml`.

## Architecture

Gradle plugin only — no standalone CLI. The plugin orchestrates detekt as a cacheable Gradle task and aggregates its **SARIF** output (it does not embed detekt-core).

- `plugin/` — the whole self-contained plugin (one published artifact). Registers the `composeDoctor` task; applies detekt + `compose-rules`, layers the bundled policy (`src/main/resources/policy/*.yml`), filters findings per the engine levels, scores, and reports. Internal packages:
  - `dev.composedoctor.scoring` — pure, deterministic scoring function + model. Must be reproducible; cover with tests.
  - `dev.composedoctor.rulemap` — `ruleId → dimension` (+ docsUrl / fixHint) taxonomy, with a rule-set fallback. Data, not logic.
  - (`composeDoctorBaseline` and AGP `lint` are planned, not built.)
- `skills/compose-doctor/` — agent skill (`SKILL.md`): find-or-bootstrap task → run → read SARIF →
  fix one rule → re-run. Step 0 bootstraps via `init/compose-doctor.init.gradle.kts` if no task exists.
- `.claude-plugin/` — Claude Code plugin + marketplace manifests, so the skill (and the
  `/compose-doctor` command in `commands/`) install via `/plugin install compose-doctor@compose-doctor`.
- **Multi-agent packaging** — `skills/compose-doctor/SKILL.md` is the single source of truth; thin
  wrappers re-expose it per agent and must stay in sync when the loop changes: `AGENTS.md` (root,
  auto-read by Codex/OpenCode/Antigravity/Cursor), `gemini-extension.json` + `commands/compose-doctor.toml`
  (Gemini CLI extension), `.opencode/commands/compose-doctor.md` (OpenCode). The wrappers use the
  repo-relative init path, not `$CLAUDE_PLUGIN_ROOT` (Claude-only).
- `init/` — `compose-doctor.init.gradle.kts`, the zero-touch init script the skill bootstraps with.
- `.github/` — reusable GitHub Action running `./gradlew composeDoctor`.

## Scoring

```
score = 100 − (uniqueErrorRuleIds × 1.5) − (uniqueWarningRuleIds × 0.75)   // clamped [0,100]
```

Counts **unique rules triggered, not instances; no size normalization** (translated from React Doctor). Labels: 75+ Great · 50–74 Needs work · <50 Critical. Pin the ruleset version so scores stay comparable.

## Conventions

- **Determinism is a hard requirement** for `scoring/` — same input must always yield the same score.
- Don't reimplement rules; wrap compose-rules/detekt/android-lint, extend natively only for genuine gaps.
- Reuse detekt's `baseline.xml` / `detekt.yml` (`config/detekt/detekt.yml`) / `@Suppress` for debt and config — don't build a parallel system. compose-doctor's only own config is the per-engine strictness dial (`composeDoctor { detekt/compose = EngineLevel.* }`).
- Dimensions are **display buckets**, not score weights.
- Tech: Kotlin, Gradle plugin, detekt + `io.nlopez.compose.rules`, SARIF, kotlinx.serialization. (AGP lint planned.)

## Contributing workflow

All changes go through a branch → PR → green CI → merge. **Never push directly to `main`.**
Open a PR, wait for the `ci` and `pr-gate` checks to pass, then merge (squash) and delete the
branch. Commits and PRs carry no Claude co-authorship.

## Modules

- `plugin/` — the `dev.composedoctor` plugin (self-contained; the only published module). Applies
  detekt + the `io.nlopez.compose.rules` ruleset (policy bundled at
  `plugin/src/main/resources/policy/*.yml`: `base` scope + per-engine `*-severities` overlays),
  reads the detekt SARIF (`SarifReader`), filters by engine level (`EngineFilter`), scores it
  (`scoring` package), reports (`score.json` + console), writes trend history, gates via `failBelow`.
  The `scoring` and `rulemap` packages live inside this module (no separate Gradle modules, so the
  published jar has no unpublished dependencies).
- `playground/` — standalone composite build (`includeBuild("..")`) — a deliberately-flawed feed
  app used to verify detection against the plugin sources (scores ~72/100 NEEDS_WORK).

## Common commands

```bash
./gradlew build                          # compile + all hermetic tests
./gradlew test                           # tests only (incl. scoring determinism)
./gradlew -p playground composeDoctor    # run the real scan against the broken playground
```

The hermetic plugin tests set `autoConfigureDetekt.set(false)`; the playground exercises the
full detekt + compose-rules path (needs network to resolve the ruleset).
