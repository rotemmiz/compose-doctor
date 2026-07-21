# compose-doctor — Community Launch & Outreach Kit

This document contains ready-to-use copy and templates for launching `compose-doctor` across developer newsletters, social media, community forums, and awesome-list repositories.

---

## 📰 1. Newsletter Submissions

### 1.1 Android Weekly
- **Submission Portal**: [androidweekly.net/submit](https://androidweekly.net/submit)
- **Title**: `compose-doctor: React Doctor for Android Jetpack Compose`
- **URL**: `https://github.com/rotemmiz/compose-doctor`
- **Category**: `Tools / Open Source`
- **Description**:
  > `compose-doctor` brings the React Doctor concept to Android Jetpack Compose. It runs `detekt` + `compose-rules` static analysis, produces a deterministic 0–100 health score (`score.json`), gates pull requests with a sticky PR comment, and includes a self-healing fix loop for AI coding agents (Claude Code, Antigravity, OpenCode, Codex, Cursor).

---

### 1.2 Kotlin Weekly
- **Submission Portal**: [kotlinweekly.net](https://kotlinweekly.net)
- **Title**: `compose-doctor — Deterministic Health Check for Jetpack Compose`
- **URL**: `https://github.com/rotemmiz/compose-doctor`
- **Description**:
  > A Gradle plugin offering deterministic 0–100 health scoring and SARIF aggregation for Android Jetpack Compose codebases, built on detekt and compose-rules with seamless agent harness integration.

---

### 1.3 Jetc.dev (Jetpack Compose Newsletter)
- **Title**: `compose-doctor: Automated Health Scoring & PR Gates for Compose`
- **URL**: `https://composedoctor.dev`
- **Description**:
  > One-command health check for Jetpack Compose codebases (`./gradlew composeDoctor`). Aggregate rule findings into a 0–100 score, gate PRs, and allow AI agents to iteratively clean up Compose code.

---

## 🐦 2. X (Twitter) Launch Thread

### Tweet 1 (Hook + Demo)
> 🚀 Introducing **compose-doctor**: React Doctor for Android Jetpack Compose!
>
> Get a deterministic 0–100 health score, detekt + compose-rules scoring, sticky PR comments, and a self-healing AI agent loop for your Compose codebase.
>
> 🌐 https://composedoctor.dev
> 📦 https://github.com/rotemmiz/compose-doctor

### Tweet 2 (The Problem & Score Formula)
> Why compose-doctor? Compose static analysis rules exist, but they were unbundled.
> `compose-doctor` turns findings into a single score:
> `score = 100 - (uniqueErrorRules * 1.5) - (uniqueWarningRules * 0.75)`
>
> Clearing unique rules raises your score! 📈

### Tweet 3 (CI PR Gate)
> Drop one line into GitHub Actions. On every PR, `compose-doctor` posts a sticky health report with exact line numbers, fix hints, and per-dimension breakdowns (State, Performance, Architecture). 🛡️

### Tweet 4 (AI Agent Harness)
> Paired with Claude Code, Antigravity, OpenCode, Codex, or Cursor, your agent runs `composeDoctor`, reads `score.json`, fixes code iteratively, and verifies build integrity. Zero setup (`AGENTS.md` included). 🤖

### Tweet 5 (Try it in 30 Seconds)
> Try it right now on the built-in playground feed app:
> `git clone https://github.com/rotemmiz/compose-doctor && ./gradlew -p playground composeDoctor`
>
> Star the repo ⭐️ and let us know what you think!

---

## 💬 3. Reddit Community Posts

### 3.1 `r/androiddev`
- **Title**: `Showcase: compose-doctor — React Doctor for Android Jetpack Compose (Gradle Plugin + Agent Harness + PR Gate)`
- **Post Body**:
  > Hi `r/androiddev`!
  >
  > I created **compose-doctor**, a deterministic health check for Android Jetpack Compose inspired by [React Doctor](https://www.react.doctor/).
  >
  > **The problem:**
  > Compose linting rules exist (`detekt` + `compose-rules`), but they are unbundled — scattered output, no shared score, and no standardized way to gate PRs or let coding agents iteratively fix violations.
  >
  > **What `compose-doctor` does:**
  > 1. **Deterministic 0–100 Score**: Runs `detekt` + `compose-rules` under the hood and turns findings into a single score: `100 - (uniqueErrorRules * 1.5) - (uniqueWarningRules * 0.75)`.
  > 2. **CI / PR Gate**: Reusable GitHub Action posts a sticky PR comment with health breakdown, top findings, line numbers, and score deltas.
  > 3. **Agent Harness**: Emits structured `score.json` + `detekt.sarif` and ships bundled skills (`AGENTS.md`, Claude Code plugin, Gemini extension, OpenCode command) so AI agents can auto-fix rules without breaking builds.
  >
  > **Try it:**
  > ```bash
  > git clone https://github.com/rotemmiz/compose-doctor && ./gradlew -p playground composeDoctor
  > ```
  >
  > Check it out at [composedoctor.dev](https://composedoctor.dev) or [GitHub](https://github.com/rotemmiz/compose-doctor). Feedback and contributions welcome!

---

## 🛠️ 4. "Awesome Lists" PR Submissions

### 4.1 `android/awesome-android`
- **Section**: `Static Analysis & Code Quality`
- **Markdown**:
  ```markdown
  - [compose-doctor](https://github.com/rotemmiz/compose-doctor) - Deterministic 0-100 health score, detekt + compose-rules analysis, sticky PR comment gate, and AI agent harness for Jetpack Compose.
  ```

### 4.2 `pfetfre/awesome-kotlin`
- **Section**: `Build Tools / Code Quality`
- **Markdown**:
  ```markdown
  - [rotemmiz/compose-doctor](https://github.com/rotemmiz/compose-doctor) - A deterministic health check and scoring plugin for Android Jetpack Compose.
  ```

### 4.3 `heshed/awesome-claude-code`
- **Section**: `Plugins & Extensions`
- **Markdown**:
  ```markdown
  - [compose-doctor](https://github.com/rotemmiz/compose-doctor) - Claude Code plugin for automated Jetpack Compose health checks and iterative fixes.
  ```

---

## 💬 5. Kotlinlang Slack Messages

- **Target Channels**: `#compose`, `#detekt`, `#tooling`, `#ai`
- **Message**:
  > Hey everyone! 👋 I launched **compose-doctor** — a deterministic health check for Android Jetpack Compose (inspired by React Doctor).
  >
  > It wraps `detekt` + `compose-rules`, computes a 0–100 health score, posts sticky PR comments in CI, and provides an agent loop for Claude Code / Antigravity / OpenCode / Cursor to auto-fix Compose code.
  >
  > 🔗 https://composedoctor.dev | GitHub: https://github.com/rotemmiz/compose-doctor
