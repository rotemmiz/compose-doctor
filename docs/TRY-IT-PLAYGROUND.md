# Try compose-doctor on the playground

A 5-minute, hands-on tour. The repo ships `playground/` — a deliberately-broken Compose module
wired to the plugin from source — so you can see the score, the machine report, and the fix loop
without setting up your own project.

## Prerequisites

- JDK 21 (`java -version` → 21). Nothing else; the Gradle wrapper handles the rest.
- No Android SDK needed — the playground is *analysed* (detekt PSI), not compiled.

## 1. Run it

From the repo root:

```bash
./gradlew -p playground composeDoctor
```

Expected console output:

```
compose-doctor — health score: 95/100  [GREAT]
  unique error rules:   0
  unique warning rules: 7
  total findings:       7

  by dimension:
    ACCESSIBILITY      100/100
    ARCHITECTURE       96/100
    PERFORMANCE        100/100
    SECURITY           100/100
    STATE_CORRECTNESS  99/100

  next fixes (most score per fix first):
    +0.75 ComposableNaming (×1) — Rename the UI-emitting composable to PascalCase.
    ...
```

> Score = `100 − errorRules×1.5 − warningRules×0.75`. Here: 7 warning rules → `100 − 7×0.75 = 94.75
> → 95`. Security/Accessibility are 100 because those come from android-lint, which isn't wired yet.

## 2. Look at the machine report

This is what an agent and CI read:

```bash
cat playground/build/reports/compose-doctor/score.json   # score, byRule plan, findings, delta
cat playground/build/reports/detekt/detekt.sarif         # SARIF with exact locations
```

`byRule` is the remediation plan, ordered by score-per-fix; each row has a `fixHint` and `docsUrl`.
See [AGENT-HARNESS.md](AGENT-HARNESS.md) for the full schema.

## 3. The 7 deliberate issues

All in [`playground/src/main/kotlin/dev/composedoctor/playground/BadComposables.kt`](../playground/src/main/kotlin/dev/composedoctor/playground/BadComposables.kt):

| Function | Rule(s) | The fix |
|---|---|---|
| `myScreen()` | **ComposableNaming** + **ModifierMissing** | Rename to `MyScreen`; add `modifier: Modifier = Modifier` and pass it to the root. |
| `Header(modifier: Modifier)` | **ModifierWithoutDefault** | `modifier: Modifier = Modifier`. |
| `Profile(modifier = …, name)` | **ComposableParamOrder** | Put `name` first, then `modifier`. |
| `Editor(state: MutableState<String>)` | **MutableStateParam** | Pass `value: String` + `onValueChange: (String) -> Unit`. |
| `Toggle()` | **RememberMissing** | `var checked by remember { mutableStateOf(false) }`. |
| `Container(viewModel) → Detail(viewModel)` | **ViewModelForwarding** | Pass the concrete state/callbacks Detail needs, not the ViewModel. |

(`myScreen` trips two rules, so 6 functions → 7 findings.)

## 4. Watch the score move (the fix loop)

Fix one rule completely and re-run. For example, rename the lowercase composable:

```bash
# in BadComposables.kt, change `fun myScreen()` to `fun MyScreen()`
./gradlew -p playground composeDoctor
```

Expected:

```
compose-doctor — health score: 96/100  [GREAT]
  unique warning rules: 6
  Δ vs previous run:        +1  (cleared 1, new 0)
```

and `score.json` now shows:

```json
"delta": { "vs": "previous run", "score": 1, "newRules": [], "fixedRules": ["ComposableNaming"] }
```

Key behaviour: the score only moves when the **last** instance of a rule is gone. Keep clearing
rules one at a time and the score climbs toward 100. (`ModifierMissing` on the same function stays
until you also add the `modifier` parameter.)

## 5. Try the gate

Make it fail a build below a threshold:

```bash
# playground/build.gradle.kts
composeDoctor { failBelow.set(96) }
```

`./gradlew -p playground composeDoctor` now exits non-zero, and `score.json` shows
`"status": "below_gate"`. Raise the score above the threshold (or lower it) to pass.

## 6. Try the agent loop

Install the skill, then ask your agent to improve the score:

```bash
cp -r skill/compose-doctor .claude/skills/compose-doctor
```

Prompt: *"Run compose-doctor on the playground and raise the score by fixing the findings, one
rule at a time. Don't suppress anything."* The agent should run the task, read `score.json`'s
`byRule`, fix a rule, re-run, and watch the delta — exactly the loop in
[AGENT-HARNESS.md](AGENT-HARNESS.md).

## 7. Reset

```bash
git checkout -- playground/                # undo any edits
rm -rf playground/.compose-doctor          # clear last-run.json / history.jsonl
```
