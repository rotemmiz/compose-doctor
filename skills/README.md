# compose-doctor agent skill

An agent skill that teaches coding agents (Claude Code, Codex, OpenCode, Google Antigravity, Cursor)
to run compose-doctor and fix Jetpack Compose issues iteratively, and to write better Compose
proactively.

`SKILL.md` is the single source of truth; the per-agent entry points below are thin wrappers around
it (see [`AGENTS.md`](../AGENTS.md), which mirrors the essential loop for every non-Claude agent).

## Install

**Claude Code (recommended) — as a plugin.** One command discovers and installs the skill plus the
`/compose-doctor` command:

```text
/plugin marketplace add rotemmiz/compose-doctor
/plugin install compose-doctor@compose-doctor
```

**Claude Code — manual copy** (no marketplace):

```bash
# project-scoped (committed for the team)
cp -r skills/compose-doctor .claude/skills/compose-doctor

# or user-scoped (all your projects)
cp -r skills/compose-doctor ~/.claude/skills/compose-doctor
```

**Codex / Cursor** — no install needed. Both auto-read the root [`AGENTS.md`](../AGENTS.md) when you
open the repo. (Optional: copy the loop into a user-scoped Codex prompt at `~/.codex/prompts/`.)

**OpenCode** — auto-reads the root [`AGENTS.md`](../AGENTS.md). The `/compose-doctor` slash command
ships in-repo at [`.opencode/commands/compose-doctor.md`](../.opencode/commands/compose-doctor.md) —
clone and it's there; commit it to share with your team.

**Google Antigravity** — auto-reads the root [`AGENTS.md`](../AGENTS.md) (cross-tool context).

**Gemini CLI** — install as an [extension](https://google-gemini.github.io/gemini-cli/docs/extensions/),
which bundles the `/compose-doctor` command (`commands/compose-doctor.toml`) plus `AGENTS.md` context:

```bash
gemini extensions install https://github.com/rotemmiz/compose-doctor
```

**Any other agent** — point your agent rules at `skills/compose-doctor/SKILL.md` (or `AGENTS.md`), or
copy its contents into your agent instructions.

## How it finds the Gradle task

The skill is **self-bootstrapping** (step 0 of its fix loop): it checks whether a `composeDoctor`
task exists, and if not, runs the bundled init script (`init/compose-doctor.init.gradle.kts`) to
apply the plugin without editing any build file — the `npx`-style zero-touch path. That init script
resolves the plugin from the Gradle Plugin Portal, where it's published as `dev.composedoctor`. (To
run against an unpublished local build, the skill can instead apply the plugin from source.)
