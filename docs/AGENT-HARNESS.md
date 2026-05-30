# Agent harness

How an AI agent drives compose-doctor as a closed loop. Grounded in the artifacts the tool
emits today.

## Mental model

compose-doctor is a **deterministic oracle**; the agent is the **actuator**. The tool only
diagnoses and scores — it never edits code. The agent reads the diagnosis, makes one targeted
change, and re-asks the oracle. Two properties make the loop work:

- **Determinism** — same code → same score, so "did my edit help?" is unambiguous.
- **Unique-rule scoring** — the reward is discrete: clearing *every* instance of one `ruleId`
  yields exactly `+1.5` (error) or `+0.75` (warning); partial fixes yield nothing. Remediation is
  a knapsack problem, not a fuzzy gradient.

## The output contract

What the tool emits and the role each artifact plays:

| Artifact | Path | Role |
|---|---|---|
| **score.json** | `build/reports/compose-doctor/score.json` | Primary machine contract: plan + reward. |
| **detekt.sarif** | `build/reports/detekt/detekt.sarif` | Precise locations; IDE / code-scanning interop. |
| **last-run.json** | `.compose-doctor/last-run.json` | Snapshot used to compute the next run's delta. |
| **history.jsonl** | `.compose-doctor/history.jsonl` | Append-only trend log. |
| console | stdout | Humans only — the agent ignores it. |

### score.json (schemaVersion 1)

```json
{
  "schemaVersion": 1,
  "rulesetVersion": "compose-rules 0.4.22 · detekt 1.23.7",
  "status": "ok",                       // "ok" | "below_gate"
  "score": 95,
  "label": "GREAT",                     // 75+ GREAT · 50-74 NEEDS_WORK · <50 CRITICAL
  "uniqueErrorRules": 0,
  "uniqueWarningRules": 7,
  "totalFindings": 7,
  "dimensions": { "ARCHITECTURE": 96, "STATE_CORRECTNESS": 99, "...": 100 },
  "delta": {                            // null on the first run
    "vs": "previous run", "score": 1,
    "newRules": [], "fixedRules": ["ComposableNaming"]
  },
  "byRule": [                           // the remediation plan, best score-per-fix first
    { "ruleId": "RememberMissing", "severity": "WARNING", "dimension": "STATE_CORRECTNESS",
      "count": 1, "scoreImpactIfCleared": 0.75, "autoFixable": false,
      "docsUrl": "https://mrmans0n.github.io/compose-rules/rules/#...",
      "fixHint": "Wrap the mutableStateOf(...) call in remember { }." }
  ],
  "findings": [
    { "id": "<stable fingerprint>", "ruleId": "RememberMissing", "dimension": "STATE_CORRECTNESS",
      "severity": "WARNING", "file": "src/main/kotlin/...", "line": 45, "engine": "detekt",
      "message": "..." }
  ]
}
```

Why each field exists:
- **`schemaVersion`** — the agent contract is versioned independently of the tool.
- **`status`** — read this, not the exit code (Gradle conflates failure causes). `below_gate` means
  the score is under the configured `failBelow`; the report is still written.
- **`byRule`** — the precomputed plan. `scoreImpactIfCleared` + `fixHint` + `docsUrl` mean the agent
  doesn't recompute prioritization. Ordered by `(scoreImpact desc, count asc, ruleId)`.
- **`delta`** — what this run changed vs the previous one (`fixedRules`/`newRules`/`score`). The
  agent's per-iteration feedback and the PR comment's headline.
- **finding `id`** — a stable fingerprint over `ruleId|relativePath|line`, so the agent can track a
  specific finding across runs, not just counts. `file` is relativized for portability.

## The loop

```
1. INVOKE     ./gradlew :module:composeDoctor
2. READ       score.json (always); SARIF region only for the file being fixed
3. PLAN       take byRule[0] (highest score-per-fix), or the cheapest rule you know how to fix
4. FIX        resolve EVERY instance of that ruleId (partial = zero reward)
5. VERIFY     re-run composeDoctor AND :module:compileKotlin / test
                ├─ score up AND ruleId in delta.fixedRules AND build green → keep
                ├─ score flat / delta.newRules non-empty                   → revert, next rule
                └─ build/tests broke                                       → revert (bad fix)
6. RECORD     note the fix recipe in agent memory; commit on the branch
7. LOOP       until a stop condition
8. SHIP       open a PR whose body is the delta ("74 → 88"); merge when CI is green
```

Non-negotiable: **score-up must be coupled with still-compiles/tests-pass.** Compose fixes
(adding `modifier`, hoisting state) change signatures and ripple to call sites; a score that rose
while the build broke is a regression.

## Memory model

Four stores, distinct ownership. **Tool memory = facts about current health; agent memory =
knowledge of how to improve it.**

| Layer | Owner | Lifetime | Holds |
|---|---|---|---|
| score.json, detekt.sarif | tool | per run | current snapshot |
| last-run.json, history.jsonl | tool, in-repo | persistent | delta basis + trend |
| baseline.xml, detekt.yml, `@Suppress` | repo | permanent | accepted debt & policy |
| agent memory (Claude Code memory / skill) | agent | cross-session | strategy & decisions |

Agent memory should hold: **per-rule fix recipes learned in this repo**, **policy decisions**
(target score; rules to always-fix vs permanently-suppress; rules needing human judgement),
**project conventions** (DI/state-hoisting style), and **the last `rulesetVersion`** seen — so a
score drop after a ruleset bump is attributed correctly, not mistaken for a code regression.

## Integrity policy (critical)

Because the score is the reward, the agent must **fix, not suppress**. Suppression (`@Suppress`,
baseline, disabling a rule in `detekt.yml`) is allowed **only** with a recorded, user-approved
justification — a comment at the suppression site *and* a note in agent memory. The PR diff makes
every suppression reviewable. Silencing rules to raise the score is a failure, not a win.

## Prioritization

Attack order = `byRule` order: clear the cheapest high-value rules first
(`severityWeight desc, count asc`). Errors (1.5) outrank warnings (0.75); among equals, fewer
instances first (cheaper to clear completely, and only a complete clear scores).

## Stop conditions

Halt when any holds: target score reached · all remaining rules need human judgement · a full
pass yielded no improvement · max iterations · build/tests can't be kept green for the next
candidate. Then summarize start → end score, rules cleared, rules deferred (with reasons).

## Harness modes

Same contract, three drivers:
- **Interactive** — the full loop with a human in the room (IDE / Claude Code).
- **CI gate** — score.json → sticky PR comment + threshold; reports, doesn't fix. *(Built:
  `.github/workflows/compose-doctor.yml`.)*
- **Autonomous remediation** — the agent runs the loop on a branch and opens a PR whose body is
  the delta; merges once CI re-confirms green.

## Status & follow-ups

Implemented: schemaVersion, status, byRule (with docsUrl/fixHint), finding fingerprints, delta vs
previous run.

Not yet: `delta.vs = "base branch"` in CI (compare PR head to base) · `autoFixable` is always
`false` until ktlint-style autofix rules are mapped · android-lint findings (Security/Accessibility
are mostly empty without it).
