# Rule policy

compose-doctor wraps two engines and applies a curated default policy on top. The goal: the score
reflects **Compose health + genuine bugs**, not formatting.

## Engines

| Engine | Rules available | Role |
|---|---|---|
| [compose-rules](https://mrmans0n.github.io/compose-rules/) (`Compose`) | 32 | The core — Compose anti-patterns. |
| [detekt](https://detekt.dev/) defaults | 210 (128 active) | General Kotlin quality — we keep only the genuine-bug subset. |
| android-lint | — | Security & Accessibility. **Not wired yet** (needs the Android SDK). |

## What's enabled

**All of the `Compose` ruleset**, plus `UnstableCollections` (a real recomposition-perf issue,
which compose-rules ships off by default). `Material2` and `PreviewNaming` stay off.

**From detekt, only the bug/smell rule sets:** `potential-bugs`, `coroutines`, `performance`,
`exceptions`, `complexity`, `empty-blocks`.

**Disabled** (pure style/formatting/convention — they'd dominate and dilute a Compose score):
`style` (MagicNumber, WildcardImport, MaxLineLength, ForbiddenComment, …), `naming`, `comments`.

> `naming.FunctionNaming` is disabled anyway because PascalCase composables are correct — Compose
> naming is owned by `ComposableNaming`.

## Severity tiers

The score weights two tiers (`100 − errorRules×1.5 − warningRules×0.75`):

- **error (−1.5)** — genuine correctness / state / recomposition bugs:
  `RememberMissing`, `RememberContentMissing`, `MutableStateParam`, `MutableStateAutoboxing`,
  `LambdaParameterInRestartableEffect`, `ContentEmitterReturningValues`, `MultipleEmitters`,
  `ContentSlotReused`, `ViewModelForwarding`, `ViewModelInjection`, `CompositionLocalAllowlist`,
  all of detekt `potential-bugs`, `SleepInsteadOfDelay`, `SwallowedException`, `EmptyCatchBlock`.
- **warning (−0.75)** — API shape, naming, design, maintainability: everything else
  (`ModifierMissing`, `ComposableNaming`, `ComposableParamOrder`, `UnstableCollections`,
  `complexity/*`, …).

## Dimensions

Display buckets only (they don't weight the overall score):

| Dimension | Sources |
|---|---|
| State/Correctness | Compose state rules · detekt `potential-bugs`, `coroutines` |
| Performance | Compose perf rules · detekt `performance` |
| Architecture | Compose API/naming/modifier rules · detekt `exceptions`, `complexity`, `empty-blocks` |
| Security / Accessibility | android-lint (pending) |

## Strictness per engine

compose-doctor is a thin wrapper over the engines: they own *which rules exist* (their native
configs); the `composeDoctor { }` DSL owns *how strict the score is* per engine.

```kotlin
composeDoctor {
    detekt  = EngineLevel.ERRORS_AND_WARNINGS   // off | AS_IS | ERRORS | ERRORS_AND_WARNINGS
    compose = EngineLevel.ERRORS_AND_WARNINGS
}
```

| Level | Meaning |
|---|---|
| `OFF` | exclude this engine from the score entirely |
| `AS_IS` | trust the engine's own severities — skip compose-doctor's curated error/warning remap |
| `ERRORS` | apply the curated tiers, then count only the error tier (genuine bugs) |
| `ERRORS_AND_WARNINGS` | apply the curated tiers, count errors and warnings (default) |

"detekt" = the general detekt rule sets; "compose" = the compose-rules ruleset. They're independent,
so e.g. `compose = ERRORS_AND_WARNINGS` with `detekt = ERRORS` scores all Compose issues but only
detekt's genuine bugs. This dial is coarse; for per-rule control use the engine's own config below.

## Customizing

compose-doctor has **no config format of its own** — you customise it through the engines' own
configs, layered over the bundled policy so your settings win.

**detekt / compose-rules** — use detekt's native config at **`config/detekt/detekt.yml`** (its
conventional path, honoured automatically; compose-rules has no separate file — it's the `Compose:`
section in there). The three things you'll usually do — the same ones a React Doctor config does —
are disable a rule, change its severity, and exclude paths:

```yaml
# config/detekt/detekt.yml
Compose:
  ModifierMissing:
    active: false          # disable a rule
  RememberMissing:
    severity: warning      # downgrade error -> warning (or upgrade warning -> error)
potential-bugs:
  excludes: ['**/generated/**']   # scope a rule set away from generated code
```

Anything set on the `detekt {}` extension is honoured too. Point elsewhere with
`composeDoctor.configFile = file(...)` if you don't use the conventional path.

- **Accept existing debt:** detekt's `baseline.xml` (`detekt { baseline = file(...) }`).
- **Silence locally:** `@Suppress("RuleName")` in code.
- **android-lint** (once wired): its own `lint.xml`, the `lint { }` DSL, `lint-baseline.xml`, and
  `@SuppressLint` — compose-doctor will read lint's results, not re-configure it.
- **Own it entirely:** `composeDoctor.autoConfigureDetekt = false` — compose-doctor then only
  aggregates and scores the SARIF you produce.

> Reusing each tool's own config is deliberate — no parallel config system to learn or maintain.
