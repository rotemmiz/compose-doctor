# Ready-to-Post Launch Copy for compose-doctor

Below is the complete set of formatted, copy-paste ready launch posts for every target platform.

---

## 1. 🐦 X (Twitter) — 5-Tweet Launch Thread

### Tweet 1 (Hook + Demo)
> 🚀 Introducing **compose-doctor**: React Doctor for Android Jetpack Compose!
> 
> Get a deterministic 0–100 health score, detekt + compose-rules scoring, sticky PR comments, and a self-healing AI agent loop for your Compose codebase.
> 
> 🌐 https://composedoctor.dev
> 📦 https://github.com/rotemmiz/compose-doctor

---

### Tweet 2 (The Problem & Score Formula)
> Why compose-doctor? Compose static analysis rules exist, but they were unbundled.
> 
> `compose-doctor` turns findings into a single score:
> `score = 100 - (uniqueErrorRules * 1.5) - (uniqueWarningRules * 0.75)`
> 
> Clearing unique rules raises your score! 📈

---

### Tweet 3 (CI PR Gate)
> Drop one line into GitHub Actions. On every PR, `compose-doctor` posts a sticky health report with exact line numbers, fix hints, and per-dimension breakdowns (State, Performance, Architecture). 🛡️

---

### Tweet 4 (Self-Healing AI Agent Loop)
> Paired with Claude Code, Antigravity, OpenCode, Codex, or Cursor, your agent runs `composeDoctor`, reads `score.json`, fixes code iteratively, and verifies build integrity. Zero setup (`AGENTS.md` included). 🤖

---

### Tweet 5 (Try it in 30 Seconds)
> Try it right now on the built-in playground feed app:
> `git clone git@github.com:rotemmiz/compose-doctor.git && cd compose-doctor && ./gradlew -p playground composeDoctor`
> 
> Star the repo ⭐️ and let us know what you think!

---

## 2. 💼 LinkedIn Post

```text
🚀 Excited to announce compose-doctor: React Doctor for Android Jetpack Compose!

If your team builds with Jetpack Compose, you know static analysis rules exist (detekt + compose-rules), but they've been unbundled — scattered output, no single quality metric, and no standard way to gate PRs or harness AI agents.

compose-doctor fills that gap:
✅ Deterministic 0–100 Health Score — score = 100 − (uniqueErrorRules × 1.5) − (uniqueWarningRules × 0.75).
✅ Reusable CI / PR Gate — Posts a sticky score comment on pull requests with file:line locations and dimension breakdowns.
✅ AI Agent Harness — Emits structured score.json + SARIF and bundles skills for Claude Code, Antigravity, OpenCode, Codex, and Cursor to auto-fix Compose code safely.

Built as a single Gradle plugin (`dev.composedoctor`), live on the Gradle Plugin Portal!

Try it in 30 seconds:
$ git clone git@github.com:rotemmiz/compose-doctor.git && cd compose-doctor && ./gradlew -p playground composeDoctor

Website: https://composedoctor.dev
GitHub: https://github.com/rotemmiz/compose-doctor

#Android #AndroidDev #JetpackCompose #Kotlin #Gradle #DevTools #SoftwareEngineering #AI #ClaudeCode
```

---

## 3. 🤖 Reddit — `r/androiddev`

**Title**: `Showcase: compose-doctor — React Doctor for Android Jetpack Compose (Gradle Plugin + PR Gate + Agent Harness)`

**Body**:
```markdown
Hi r/androiddev!

I built **compose-doctor** — a deterministic health check for Android Jetpack Compose, inspired by [React Doctor](https://www.react.doctor/).

### 🔍 The Problem
Compose linting rules exist (`detekt` + `compose-rules`), but they've been unbundled: scattered CLI output, no unified quality metric, and no standard harness to gate PRs or allow AI coding agents to fix findings safely.

### 💡 What `compose-doctor` does

1. **Deterministic 0–100 Score**: Runs `detekt` + `compose-rules` under the hood and turns findings into a single score:
   `score = 100 - (uniqueErrorRules * 1.5) - (uniqueWarningRules * 0.75)`
   *(Unit is unique rule IDs triggered, not instance count. Clearing a rule raises your score!)*

2. **CI / PR Gate**: Reusable GitHub Action posts a sticky PR comment with a health badge, score deltas, line numbers, and per-dimension breakdowns (State, Performance, Architecture).

3. **Self-Healing AI Agent Loop**: Emits `score.json` and SARIF while bundling skills for Claude Code (`/plugin marketplace add rotemmiz/compose-doctor`), Gemini CLI, OpenCode, Codex, and Cursor (`AGENTS.md`). Agents run the task, read the findings, fix one rule at a time, and verify build integrity.

### 🚀 Try it right now
The repo includes a deliberately-flawed playground feed app:

```bash
git clone git@github.com:rotemmiz/compose-doctor.git && cd compose-doctor
./gradlew -p playground composeDoctor
```

- **Website**: [composedoctor.dev](https://composedoctor.dev)
- **GitHub**: [github.com/rotemmiz/compose-doctor](https://github.com/rotemmiz/compose-doctor)
- **Gradle Plugin Portal**: `plugins { id("dev.composedoctor") version "0.1.0" }`

Would love to hear your feedback!
```

---

## 4. 🤖 Reddit — `r/ClaudeAI` & `r/LocalLLaMA`

**Title**: `I built a self-healing Jetpack Compose harness for Claude Code & AI Agents (compose-doctor)`

**Body**:
```markdown
Hey everyone!

When AI coding agents (Claude Code, Codex, Antigravity, Cursor) write Jetpack Compose code, they often introduce subtle anti-patterns — raw `MutableState` params, missing `remember`, improper `Modifier` chaining, or unstable list params.

I built **compose-doctor**, a tool that gives agents a **deterministic 0–100 health score** and a structured fix loop.

### How the agent loop works:
1. The agent runs `./gradlew composeDoctor`.
2. It reads `build/reports/compose-doctor/score.json` — which includes a pre-sorted `byRule` remediation plan.
3. The agent targets `byRule[0]` (highest impact per fix), clears all instances of that single rule, and re-runs to verify score increase + compilation safety.

### Install in Claude Code:
```text
/plugin marketplace add rotemmiz/compose-doctor
/plugin install compose-doctor@compose-doctor
```

Zero-install for Codex / OpenCode / Antigravity / Cursor via root `AGENTS.md`.

GitHub: https://github.com/rotemmiz/compose-doctor
Website: https://composedoctor.dev
```

---

## 5. 💬 Kotlinlang Slack

### For `#compose` & `#tooling`:
> Hey everyone! 👋 I built **compose-doctor** — a deterministic health check for Android Jetpack Compose (inspired by React Doctor).
> 
> It wraps `detekt` + `compose-rules`, computes a single 0–100 score (`score = 100 - uniqueErrors*1.5 - uniqueWarnings*0.75`), posts sticky PR comments in CI, and provides a self-healing harness for AI agents.
> 
> 🌐 https://composedoctor.dev
> 📦 https://github.com/rotemmiz/compose-doctor

### For `#detekt`:
> Hi all! Wanted to share **compose-doctor**, a Gradle plugin that orchestrates `detekt` + `compose-rules` and aggregates SARIF output into a deterministic 0-100 health score and PR gate. Check it out: https://github.com/rotemmiz/compose-doctor

---

## 6. 📰 Newsletters (Android Weekly / Kotlin Weekly / Jetc.dev)

### Android Weekly ([androidweekly.net/submit](https://androidweekly.net/submit))
- **Title**: `compose-doctor: React Doctor for Android Jetpack Compose`
- **URL**: `https://github.com/rotemmiz/compose-doctor`
- **Category**: `Tools / Open Source`
- **Description**:
  > `compose-doctor` brings the React Doctor concept to Android Jetpack Compose. It runs detekt + compose-rules, outputs a deterministic 0–100 score (`score.json`), posts sticky PR score gates, and provides an AI agent fix loop for Claude Code, Antigravity, OpenCode, Codex, and Cursor.

### Kotlin Weekly ([kotlinweekly.net](https://kotlinweekly.net))
- **Title**: `compose-doctor — Deterministic Health Check for Jetpack Compose`
- **URL**: `https://github.com/rotemmiz/compose-doctor`
- **Description**:
  > A Gradle plugin offering deterministic 0–100 health scoring and SARIF aggregation for Android Jetpack Compose codebases, built on detekt and compose-rules.

---

## 7. ✍️ Blog Post Draft (Dev.to / Medium / ProAndroidDev)

**Title**: `Introducing compose-doctor: React Doctor for Jetpack Compose & AI Coding Agents`

```markdown
# Introducing compose-doctor: React Doctor for Jetpack Compose & AI Coding Agents

Static analysis rules for Android Jetpack Compose have matured tremendously thanks to projects like `compose-rules` (Nacho Lopez) and `detekt`. However, until now, these rules were **unbundled**:
- CLI outputs were scattered.
- There was no single 0–100 health metric for engineering leaders.
- Gating pull requests required custom scripting.
- AI coding agents lacked a structured harness to auto-fix Compose debt without regressing code.

Enter **`compose-doctor`** — the React Doctor idea, brought to Jetpack Compose.

---

## The 0–100 Score Formula

Inspired by React Doctor, `compose-doctor` scores your codebase deterministically:

```
score = 100 − (uniqueErrorRules × 1.5) − (uniqueWarningRules × 0.75)
```

The key design choice is that the unit is **unique rule IDs triggered**, not instance count. Clearing 49 of 50 instances of a rule does not move the score; clearing the **last** instance removes that rule's penalty. This makes the score deterministic without calibration and provides an ideal target for AI agent fix loops.

---

## CI / PR Gating

With a single line in GitHub Actions, `compose-doctor` runs on every pull request and posts a sticky score comment detailing:
- Overall score & label (**75+ Great · 50–74 Needs work · <50 Critical**).
- Score delta vs. main branch.
- File:line findings with actionable fix hints.
- Per-dimension breakdowns (*State/Correctness*, *Performance*, *Architecture*, *Security*, *Accessibility*).

---

## Self-Healing AI Agent Loop

`compose-doctor` ships with native agent skills for Claude Code, Antigravity, OpenCode, Codex, and Cursor.

When an agent runs `./gradlew composeDoctor`:
1. It reads `score.json` (containing a pre-sorted remediation plan).
2. It targets the highest-value rule (`byRule[0]`).
3. It fixes all instances of that rule.
4. It re-runs `composeDoctor` and tests compilation to ensure no regressions occurred.

---

## Getting Started in 30 Seconds

Try it on the included playground app:

```bash
git clone git@github.com:rotemmiz/compose-doctor.git && cd compose-doctor
./gradlew -p playground composeDoctor
```

Apply the plugin to your project (`build.gradle.kts`):

```kotlin
plugins {
    id("dev.composedoctor") version "0.1.0"
}

composeDoctor {
    failBelow.set(75)
}
```

- **Website**: https://composedoctor.dev
- **GitHub**: https://github.com/rotemmiz/compose-doctor
```
