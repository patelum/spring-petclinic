# Track: defect

For a bug with a reproducible symptom. Proves the bug with a failing test before fixing it, so the fix is verified by construction.

**Fits when:** behaviour is wrong against existing intent, the fix is small (under `orchestrator.quick_fix_max_lines`), and no schema or public API change is needed. Otherwise suggest the feature track.

| # | Stage | Role | Skill | Inputs | Output |
| --- | --- | --- | --- | --- | --- |
| 1 | reproduce | tester | `sdlc-reproduce` | `request.md`, affected module context | `reproduction.md` + a failing test |
| 2 | plan | architect | `sdlc-plan` | `request.md`, `reproduction.md`, module context | `plan.md` |
| 3 | implement | implementer | `sdlc-implement` | `plan.md`, the failing test, files named in the plan | code change |
| 4 | verify | orchestrator | — | `config.build` | full build green |
| 5 | review | reviewer | `sdlc-review` | `request.md`, `reproduction.md`, `plan.md`, diff | `review.md` |
| 6 | final | orchestrator | — | `review.md`, diff stats | approval card |

Default gates (`gates.defect`): **plan**, **final**.

## Stage notes

- **reproduce** must end with a test that fails for the reported reason. If the bug cannot be reproduced, escalate: `1 provide more detail · 2 proceed without a failing test · 3 stop`.
- **plan** for a defect is usually 1–3 steps. Name the root cause in one sentence at the top.
- **implement** is done when the reproduction test passes and the module build is green.
- **review** checks that the fix addresses the root cause, not just the symptom, and that the test would have caught the original bug.
