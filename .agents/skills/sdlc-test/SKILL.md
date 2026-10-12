---
name: sdlc-test
description: Ensures every acceptance criterion in specs/<id>/spec.md is covered by a passing automated test after implementation. Use for the test stage of the ai-sdlc feature track.
---

# Test against the spec

**Inputs:** `specs/<id>/spec.md`, `specs/<id>/plan.md` (Coverage table), the diff (`git diff <base_commit>`).
**Output:** missing tests added; Coverage table in `plan.md` completed with test names.

## Procedure

1. For each AC, find the test that proves it. Read the test; confirm it asserts the behaviour the AC describes, not just that code runs.
2. For any AC without a real test, write one. Use the narrowest test type that proves it.
3. Add tests for error and edge cases the ACs name.
4. Run `build.verify_module` for each affected module.
5. Fill the Coverage table: `AC-n | TestClass#method | pass`.

## Rules

- Edit test code only. If a test reveals a production bug, report it to the orchestrator as a `must-fix`; do not fix production code.
- Do not mock the class under test. Mock only at boundaries (external systems, clocks, randomness).
- Integration tests that need infrastructure use the module's existing approach (Testcontainers, embedded DB, test profiles).

Report: ACs covered (n of n), tests added, any production bug found.
