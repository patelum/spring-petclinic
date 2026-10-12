---
name: sdlc-reproduce
description: Reproduces a reported defect with a failing automated test and records the root cause in specs/<id>/reproduction.md. Use for the reproduce stage of the ai-sdlc defect track.
---

# Reproduce the defect

Goal: a test that fails today for the reported reason, so the fix is proven when it passes.

**Inputs:** `specs/<id>/request.md`, context files for affected modules.
**Output:** `specs/<id>/reproduction.md` from `.ai-sdlc/templates/reproduction.md`, and a failing test.

## Procedure

1. Extract from the request: expected behaviour, actual behaviour, inputs or steps, any stack trace or log.
2. Locate the code path: start from the entry point (controller, listener, job) and follow it to where actual diverges from expected. Cite `path:line`.
3. Write the smallest test that exercises that path with the reported inputs and asserts the **expected** behaviour.
4. Run it with `build.test_one`. It must fail, and the failure must match the report (wrong value, exception type). A test failing for another reason does not count.
5. Write `reproduction.md`: symptom, the failing test (class and method), the failure output (key lines), and the suspected root cause with `path:line`.

## If it will not reproduce

Stop after a reasonable attempt and report to the orchestrator: what you tried and what you need (data, environment, steps). The orchestrator will escalate.

Report: test name, failure message, root cause in one sentence.
