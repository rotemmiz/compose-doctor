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

## Customizing

The policy is a detekt-format config layered on detekt's defaults, so you customise it with detekt's
own mechanisms — no parallel config system:

- **Accept existing debt:** a detekt `baseline.xml`.
- **Silence locally:** `@Suppress("RuleName")`.
- **Change a rule / severity / exclude paths:** your own detekt config (disable a rule with
  `active: false`, downgrade with `severity: warning`, scope with `excludes`).

> The dedicated `composeDoctor { configFile = … }` hook for layering your overrides on top of the
> bundled policy is tracked separately; until then, set `composeDoctor.autoConfigureDetekt = false`
> to fully own the detekt configuration yourself.
