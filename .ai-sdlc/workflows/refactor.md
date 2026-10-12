# Track: refactor

For structural change with **no change in behaviour**. Pins current behaviour with tests first, then changes structure in small verified steps.

**Fits when:** the goal is readability, structure, removing duplication, extracting a module, or paying down debt. If any observable behaviour should change, suggest the feature track.

| # | Stage | Role | Skill | Inputs | Output |
| --- | --- | --- | --- | --- | --- |
| 1 | characterise | tester | `sdlc-characterise` | `request.md`, module context, target code | `characterisation.md` + passing characterisation tests |
| 2 | plan | architect | `sdlc-plan` | `request.md`, `characterisation.md`, module context | `plan.md` |
| 3 | implement | implementer | `sdlc-implement` | `plan.md`, files named in the plan | code change, step by step |
| 4 | verify | orchestrator | — | `config.build` | full build green, characterisation tests unchanged and green |
| 5 | review | reviewer | `sdlc-review` | `request.md`, `characterisation.md`, `plan.md`, diff | `review.md` |
| 6 | final | orchestrator | — | `review.md`, diff stats | approval card |

Default gates (`gates.refactor`): **plan**, **final**.

## Stage notes

- **characterise** comes first and is not optional. Tests must pass against the current code before any refactoring starts.
- **plan** steps must each leave the build green. Prefer many small steps (rename, extract, move) over one large one.
- **implement** must not modify characterisation tests. If one needs changing, behaviour changed: escalate as a track mismatch.
- **review** checks for accidental behaviour change (exception types, ordering, nulls, transactions, logging relied on by ops).
