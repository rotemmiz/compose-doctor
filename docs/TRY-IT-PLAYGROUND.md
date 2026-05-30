# Try compose-doctor on the playground

A hands-on tour. The repo ships `playground/` — a deliberately-flawed mini "feed app" wired to the
plugin from source — so you can see the score, the machine report, and the fix loop without setting
up your own project.

## Prerequisites

- JDK 21 (`java -version` → 21). The Gradle wrapper handles the rest.
- No Android SDK needed — the playground is *analysed* (detekt PSI), not compiled.

## 1. Run it

From the repo root:

```bash
./gradlew -p playground composeDoctor
```

Expected:

```
compose-doctor — health score: 72/100  [NEEDS_WORK]
  unique error rules:   9
  unique warning rules: 19
  total findings:       34

  by dimension:
    ACCESSIBILITY      100/100
    ARCHITECTURE       82/100
    PERFORMANCE        96/100
    SECURITY           100/100
    STATE_CORRECTNESS  94/100
```

> Score = `100 − errorRules×1.5 − warningRules×0.75` → `100 − 9×1.5 − 19×0.75 = 72.25 → 72`.
> Genuine bugs (e.g. `RememberMissing`, `MutableStateParam`) count as **errors** (−1.5); design and
> naming issues count as **warnings** (−0.75). Security/Accessibility are 100 because those come
> from android-lint, which isn't wired in yet. See [RULES.md](RULES.md) for the policy.

## 2. Read the machine report

```bash
jq '.byRule[] | {ruleId, count, scoreImpactIfCleared, fixHint}' \
  playground/build/reports/compose-doctor/score.json     # the remediation plan
cat playground/build/reports/detekt/detekt.sarif         # SARIF with exact locations
```

See [AGENT-HARNESS.md](AGENT-HARNESS.md) for the full schema.

## 3. What's wrong, and where

~20 **Compose** rules + ~8 genuine-bug **detekt** rules fire (28 unique rules, 34 findings). Under
compose-doctor's curated policy ([RULES.md](RULES.md)) the pure-style detekt rules (MagicNumber,
WildcardImport, naming, …) are disabled, so what remains is Compose health + real bugs. By file:

| File | Representative issues (🔴 = error tier) |
|---|---|
| `ui/FeedScreen.kt` | ComposableNaming, ModifierMissing, 🔴ViewModelInjection, UnstableCollections, MutableParams, LongParameterList, 🔴LambdaParameterInRestartableEffect |
| `ui/components/Cards.kt` | ModifierNaming, ModifierWithoutDefault, ComposableParamOrder, ModifierReused, ModifierNotUsedAtRoot, 🔴MultipleEmitters, 🔴ContentEmitterReturningValues |
| `ui/state/Editors.kt` | 🔴MutableStateParam, 🔴RememberMissing, 🔴MutableStateAutoboxing |
| `ui/theme/Locals.kt` | CompositionLocalNaming, 🔴CompositionLocalAllowlist |
| `ui/Previews.kt` · `ui/Material2Screen.kt` | PreviewPublic · Material2 (off by default — enabled here) |
| `data/FeedRepository.kt` | 🔴SwallowedException, TooGenericExceptionCaught/Thrown, ThrowingExceptionsWithoutMessageOrCause, ComplexCondition, NestedBlockDepth, EmptyFunctionBlock |

Each composable is annotated with the rule it is meant to trip (search for `// ISSUE`).

> Note: a few rules (e.g. `ViewModelForwarding`, `ModifierComposable`, `ModifierClickableOrder`)
> need **type resolution** and are skipped here because the playground is analysed PSI-only.
> Running the plugin in a real module that compiles surfaces those too.

## 4. Watch the score move (the fix loop)

The score moves only when the **last** instance of a rule is gone. Pick a single-instance rule —
e.g. fix `RememberMissing` in `ui/state/Editors.kt`:

```kotlin
// Toggle(): wrap the state in remember
val checked = remember { mutableStateOf(false) }
```

```bash
./gradlew -p playground composeDoctor
```

Expected:

```
compose-doctor — health score: 74/100  [NEEDS_WORK]
  unique error rules:   8
  Δ vs previous run:        +2  (cleared 1, new 0)
```

The score jumps **+2**, not +1, because `RememberMissing` is an *error* (−1.5). Fixing a warning-tier
rule moves it +1 (rounded). `score.json`:

```json
"delta": { "vs": "previous run", "score": 2, "newRules": [], "fixedRules": ["RememberMissing"] }
```

Try a rule with multiple instances (e.g. `ComposableNaming`, which fires twice): fixing only one
occurrence leaves the rule — and the score — unchanged until you clear them all.

## 5. Try the gate

```kotlin
// playground/build.gradle.kts
composeDoctor { failBelow.set(80) }
```

`./gradlew -p playground composeDoctor` now exits non-zero (72 < 80) and `score.json` shows
`"status": "below_gate"`. Fix findings until the score clears the bar.

## 6. Try the agent loop

```bash
cp -r skill/compose-doctor .claude/skills/compose-doctor
```

Prompt: *"Run compose-doctor on the playground and raise the score by fixing the findings, one
rule at a time. Verify the build still compiles, and don't suppress anything."* The agent should
read `score.json`'s `byRule`, clear the highest-value rule, re-run, and watch the delta — the loop
in [AGENT-HARNESS.md](AGENT-HARNESS.md).

## 7. Reset

```bash
git checkout -- playground/                # undo any edits
rm -rf playground/.compose-doctor          # clear last-run.json / history.jsonl
```
