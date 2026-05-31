---
description: Health-check this Jetpack Compose codebase with compose-doctor and fix the findings.
argument-hint: "[optional gradle module path, e.g. :feature:home]"
---

Use the **compose-doctor** skill to run the health check and improve the score.

1. Run compose-doctor (follow the skill's step 0 to find or bootstrap the Gradle task). If the user
   passed a module path in `$ARGUMENTS`, scope the run to it (e.g. `./gradlew $ARGUMENTS:composeDoctor`).
2. Read `build/reports/compose-doctor/score.json` and report the current score + label.
3. Work the `byRule` plan top-down: fix **every instance** of the highest-impact rule, re-run, and
   confirm via `delta` (the rule is in `fixedRules`, score went up, `newRules` is empty).
4. Repeat until the score is satisfactory (75+) or the remaining findings need human judgement.

Integrity: fix the code, do not suppress findings or edit config to game the score.
