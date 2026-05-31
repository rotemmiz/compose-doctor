# compose-doctor

[![ci](https://github.com/rotemmiz/compose-doctor/actions/workflows/ci.yml/badge.svg)](https://github.com/rotemmiz/compose-doctor/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**A deterministic health check for Android Jetpack Compose — the [React Doctor](https://www.react.doctor/) idea, for Compose.**

Your agent writes Compose; this scores it. compose-doctor runs [detekt](https://detekt.dev/) +
[compose-rules](https://mrmans0n.github.io/compose-rules/) under the hood, then turns the findings
into a single **0–100 health score**, a structured report an agent can fix against, and a CI/PR gate.

> ⚠️ **Pre-release.** The scoring, the Gradle plugin, the agent skill, and the CI workflows all
> work and are covered by tests, but the plugin is **not yet published** to the Gradle Plugin
> Portal. For now you consume it from source (see [Try it](#try-it)).

## Why

The pieces exist in the Compose world but are unbundled — `compose-rules`/`compose-lints` for the
rules, scattered agent guides, and no shared score. compose-doctor's value isn't the rules (those
are battle-tested upstream); it's the **bundle**: one score, an agent fix-loop, and a CI gate.

## The score

```
score = 100 − (uniqueErrorRules × 1.5) − (uniqueWarningRules × 0.75)      // clamped to [0, 100]
```

Translated directly from React Doctor: the unit is **unique rule IDs triggered**, not instance
count and not normalized by code size. Fixing 49 of 50 violations of a rule does not move the
score; clearing the **last** one removes that rule's penalty. That makes the score deterministic
without calibration — and makes the agent loop ("clear one rule at a time") effective.

Labels: **75+ Great · 50–74 Needs work · <50 Critical**.

Findings are grouped into display **dimensions** (State/Correctness, Performance, Architecture,
Security, Accessibility) for the report — dimensions do not weight the overall score.

compose-doctor applies a curated policy on top of detekt + compose-rules — Compose health plus
genuine bugs, with a two-tier severity (errors −1.5, warnings −0.75) and style noise disabled. See
[docs/RULES.md](docs/RULES.md).

## Try it

The repo ships a deliberately-flawed [`playground/`](playground) feed app, wired to the plugin from
source via a composite build. With JDK 21:

```bash
git clone https://github.com/rotemmiz/compose-doctor && cd compose-doctor
./gradlew -p playground composeDoctor
```

> 📖 [docs/TRY-IT-PLAYGROUND.md](docs/TRY-IT-PLAYGROUND.md) is a guided walkthrough — run it, read
> the report, fix a rule, and watch the score move.

```
compose-doctor — health score: 72/100  [NEEDS_WORK]
  unique error rules:   9
  unique warning rules: 19
  total findings:       34

  by dimension:
    ARCHITECTURE       82/100
    STATE_CORRECTNESS  94/100
    ...
```

Outputs:
- `build/reports/compose-doctor/score.json` — machine-readable score + findings (for agents/CI). Excerpt:

  ```json
  {
    "schemaVersion": 1, "status": "ok", "score": 72, "label": "NEEDS_WORK",
    "uniqueErrorRules": 9, "uniqueWarningRules": 19, "totalFindings": 34,
    "dimensions": { "ARCHITECTURE": 82, "PERFORMANCE": 96, "STATE_CORRECTNESS": 94 },
    "byRule": [
      { "ruleId": "CompositionLocalAllowlist", "severity": "ERROR", "count": 1,
        "scoreImpactIfCleared": 1.5, "fixHint": "Avoid this CompositionLocal or add it to the allowlist." }
    ]
  }
  ```
- `build/reports/detekt/detekt.sarif` — findings in SARIF, with precise locations.

## Use it in your project

> Once published this will be `plugins { id("dev.composedoctor") version "<x.y.z>" }`. Until then,
> add the build as a composite (`includeBuild`) — see `playground/settings.gradle.kts` for the
> exact pattern.

```kotlin
// build.gradle.kts of a module with Compose source
plugins {
    id("dev.composedoctor")
}

composeDoctor {
    failBelow.set(75)          // fail the build below this score (optional)
    // per-engine strictness — see docs/RULES.md (needs: import dev.composedoctor.plugin.EngineLevel)
    // detekt  = EngineLevel.ERRORS              // count only detekt's genuine bugs
    // compose = EngineLevel.ERRORS_AND_WARNINGS // count all Compose issues (default)
    // autoConfigureDetekt.set(false)            // if you already configure detekt yourself
}
```

Then `./gradlew composeDoctor`. The plugin applies detekt, attaches the compose-rules ruleset
(config bundled), enables SARIF, and scores it. Existing detekt machinery — `baseline.xml`,
`detekt.yml`, `@Suppress` — applies as usual.

## CI

A reusable workflow posts the score on every PR, uploads SARIF for code-scanning annotations, and
gates on a threshold:

```yaml
# .github/workflows/health.yml
jobs:
  health:
    uses: rotemmiz/compose-doctor/.github/workflows/compose-doctor.yml@main
    with:
      gradle-args: composeDoctor
      fail-below: 75
```

It comments a sticky **🩺 compose-doctor — NN/100** summary on the PR.

## Agent skill

[`skills/compose-doctor/SKILL.md`](skills/compose-doctor/SKILL.md) teaches a coding agent to run the
task, read the SARIF, and fix the highest-value rule iteratively — plus Compose best-practices to
avoid the findings up front. It's the single source of truth; the per-agent packaging below wraps it.

**Claude Code — install as a plugin** (bundles the skill + a `/compose-doctor` command):

```text
/plugin marketplace add rotemmiz/compose-doctor
/plugin install compose-doctor@compose-doctor
```

**Codex · OpenCode · Google Antigravity · Cursor** — zero install: all auto-read the root
[`AGENTS.md`](AGENTS.md), the neutral mirror of the skill. OpenCode also ships the `/compose-doctor`
command in-repo at [`.opencode/commands/`](.opencode/commands).

**Gemini CLI** — install as an extension (bundles the command + `AGENTS.md` context):

```bash
gemini extensions install https://github.com/rotemmiz/compose-doctor
```

See [`skills/README.md`](skills/README.md) for all install paths. The skill is **self-bootstrapping**:
if a module has no `composeDoctor` task yet, it runs the bundled
[`init/compose-doctor.init.gradle.kts`](init/compose-doctor.init.gradle.kts) to apply the plugin
without editing any build file.

The full agent loop, the `score.json` contract, and the memory/integrity model are specified in
[docs/AGENT-HARNESS.md](docs/AGENT-HARNESS.md).

## How it works

A single self-contained Gradle plugin orchestrates the engines and aggregates their SARIF — it does
not embed detekt-core or reimplement rules. Internally: the `scoring` package is a pure,
deterministic function; `rulemap` maps rule IDs to dimensions; the plugin does the wiring, scoring,
and reporting.

## Roadmap

- Publish to the Gradle Plugin Portal.
- Wire **android-lint** to populate the Security/Accessibility dimensions.
- `composeDoctorBaseline` task to seed detekt's `baseline.xml`.

## License

[MIT](LICENSE) © 2026 Rotem Meidan
