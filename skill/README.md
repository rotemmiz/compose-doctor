# compose-doctor agent skill

An agent skill that teaches coding agents (Claude Code, Cursor, Codex) to run compose-doctor and
fix Jetpack Compose issues iteratively, and to write better Compose proactively.

## Install

**Claude Code** — copy the skill into a skills directory:

```bash
# project-scoped (committed for the team)
cp -r skill/compose-doctor .claude/skills/compose-doctor

# or user-scoped (all your projects)
cp -r skill/compose-doctor ~/.claude/skills/compose-doctor
```

**Cursor / Codex** — point your agent rules at `skill/compose-doctor/SKILL.md`, or copy its
contents into your agent instructions.

The skill assumes the `dev.composedoctor` Gradle plugin is applied so `./gradlew composeDoctor`
is available.
