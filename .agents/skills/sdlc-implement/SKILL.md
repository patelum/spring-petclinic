---
name: sdlc-implement
description: Implements one step of an approved specs/<id>/plan.md in this Java monolith and verifies it with the scoped module build. Use for the implement stage of every ai-sdlc track, one step per invocation.
---

# Implement a plan step

**Inputs:** `specs/<id>/plan.md` (and `design.md` for features), the step number, the files the step names. On a retry: the failing output or review finding.
**Output:** the code change for that step, verified.

## Procedure

1. Read the step. Read the files it names and their immediate collaborators. Nothing else unless needed.
2. Make the change. Follow the patterns, naming, logging and error handling of the surrounding code.
3. Add or update tests the step calls for. Keep production and test changes in the same step. When a new test is meant to prove the change, write and run it **before** the production change so it is seen failing; name tests after behaviour, never after the ticket id.
4. Run `build.format` if configured.
5. Run `build.verify_module` with the step's module (or `build.test_one` for a quick check first, then the module build).
6. If green: tick the step in `plan.md` (`- [x]`), and report.
7. If red: read the error, fix, and re-run. Report each attempt to the orchestrator; it tracks the retry budget.

## On a retry

You receive the failure output or a `must-fix` finding. Fix the cause, not the symptom: do not weaken assertions, add `@Disabled`, catch-and-ignore exceptions, or widen visibility just to make a check pass. If the right fix is outside the step's scope, stop and report that the plan needs revising.

## Never

- Edit `protect:` paths, characterisation tests, or build/CI files unless the step says so.
- Add dependencies.
- Commit, push or change branches.
- Start the next step.

Report: step number, files changed, command run, result (with key error lines if red).
