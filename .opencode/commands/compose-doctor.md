---
description: Health-check this Jetpack Compose codebase with compose-doctor and fix the findings.
---

Use compose-doctor to run the health check and improve the score. Full instructions are in
@AGENTS.md (and skills/compose-doctor/SKILL.md).

1. Find or bootstrap the Gradle task — task present?: !`./gradlew tasks --all 2>/dev/null | grep -q composeDoctor && echo applied || echo missing`
   If missing, bootstrap without editing any build file:
   `./gradlew --init-script ./init/compose-doctor.init.gradle.kts composeDoctor`
2. Run it. If a module path was passed in `$ARGUMENTS`, scope the run to it
   (e.g. `./gradlew $ARGUMENTS:composeDoctor`); otherwise `./gradlew composeDoctor`.
3. Read `build/reports/compose-doctor/score.json` and report the current score + label.
4. Work the `byRule` plan top-down: fix **every instance** of the highest-impact rule, re-run, and
   confirm via `delta` (the rule is in `fixedRules`, score went up, `newRules` is empty). Also run
   `:module:compileKotlin` / tests — a higher score with a broken build is a regression.
5. Repeat until the score is satisfactory (75+) or the remaining findings need human judgement.

Integrity: fix the code, do not suppress findings or edit config to game the score.
