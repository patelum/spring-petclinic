# Track: feature

For new or changed behaviour. Agrees what to build before deciding how, and how before writing code.

| # | Stage | Role | Skill | Inputs | Output |
| --- | --- | --- | --- | --- | --- |
| 1 | spec | architect | `sdlc-spec` | `request.md`, `context/index.md`, affected module context | `spec.md` |
| 2 | design | architect | `sdlc-design` | `spec.md`, affected module context, relevant source | `design.md` |
| 3 | plan | architect | `sdlc-plan` | `spec.md`, `design.md` | `plan.md` |
| 4 | implement | implementer | `sdlc-implement` | `plan.md`, `design.md`, files named in the plan | code change, step by step |
| 5 | test | tester | `sdlc-test` | `spec.md`, diff | tests covering every acceptance criterion |
| 6 | verify | orchestrator | — | `config.build` | full build green |
| 7 | review | reviewer | `sdlc-review` | `spec.md`, `plan.md`, diff | `review.md` |
| 8 | security | security | `sdlc-security` | `spec.md`, diff | appended to `review.md` |
| 9 | final | orchestrator | — | `review.md`, diff stats | approval card |

Default gates (`gates.feature`): **spec**, **design**, **plan**, **final**.

## Stage notes

- **spec** states acceptance criteria as testable statements (AC-1, AC-2, …). Assumptions the agent made go on the card's Decide line.
- **design** names every module touched and any new cross-module dependency, library or schema change. Each of those triggers an escalation unless already approved at this gate.
- **plan** steps are small and independently verifiable; each names its module for `build.verify_module`.
- **test** maps each acceptance criterion to at least one test, by name, in `plan.md` or `review.md`.
- **review** and **security** run in separate subagents from the implementer and do not see the implementer's reasoning.
