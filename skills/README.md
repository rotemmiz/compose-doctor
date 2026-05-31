# compose-doctor agent skill

An agent skill that teaches coding agents (Claude Code, Cursor, Codex) to run compose-doctor and
fix Jetpack Compose issues iteratively, and to write better Compose proactively.

## Install

**Claude Code (recommended) — as a plugin.** One command discovers and installs the skill plus the
`/compose-doctor` command:

```text
/plugin marketplace add rotemmiz/compose-doctor
/plugin install compose-doctor
```

**Claude Code — manual copy** (no marketplace):

```bash
# project-scoped (committed for the team)
cp -r skills/compose-doctor .claude/skills/compose-doctor

# or user-scoped (all your projects)
cp -r skills/compose-doctor ~/.claude/skills/compose-doctor
```

**Cursor / Codex** — point your agent rules at `skills/compose-doctor/SKILL.md`, or copy its
contents into your agent instructions.

## How it finds the Gradle task

The skill is **self-bootstrapping** (step 0 of its fix loop): it checks whether a `composeDoctor`
task exists, and if not, runs the bundled init script (`init/compose-doctor.init.gradle.kts`) to
apply the plugin without editing any build file — the `npx`-style zero-touch path. That init script
resolves the plugin from the Gradle Plugin Portal, so it works once compose-doctor is published;
until then the skill falls back to applying the `dev.composedoctor` plugin from source.
