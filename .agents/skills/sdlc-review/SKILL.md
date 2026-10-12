---
name: sdlc-review
description: Independent code review of a finished change against its spec or request, plan, codebase patterns and module boundaries, written to specs/<id>/review.md. Use for the review stage of every ai-sdlc track, in a separate context from the implementer.
---

# Review the change

You review as someone who did not write the change. Use only the inputs below; do not rely on any earlier conversation.

**Inputs:** `specs/<id>/spec.md` (feature) or `request.md` + `reproduction.md` / `characterisation.md`; `plan.md`; `status.md` for `base_commit`; the diff.
**Output:** `specs/<id>/review.md` from `.ai-sdlc/templates/review.md`.

## Procedure

1. Get the change: `git diff <base_commit>` and `git status --porcelain` for new files. Read new files in full.
2. **Correctness.** Does it satisfy every AC / fix the root cause / preserve behaviour (refactor)? Trace at least the main path through the code.
3. **Tests.** Does each AC have a test that would fail without the change? For a defect, would the new test have caught the bug? For a refactor, are characterisation tests unchanged?
4. **Scope.** Anything changed that the plan did not call for?
5. **Boundaries.** New imports across modules? New dependencies in build files? Changes to `protect:` paths?
6. **Code quality in context.** Follows the module's existing patterns; error handling and transactions consistent; no dead code, debug output or commented-out code; no swallowed exceptions; resources closed; thread-safety where it matters.
7. **Run** `build.verify_module` for affected modules to confirm green.
8. Write `review.md`: verdict, findings, coverage, and a 3-line summary for the approval card.

## Classifying findings

- `must-fix`: incorrect behaviour, missing test for an AC, scope creep, boundary violation, protected path touched, resource or transaction bug, anything that would fail a careful human review.
- `consider`: naming, minor style, possible follow-ups. Keep this list short.

Each finding: `path:line`, what is wrong, what to do.

Report: verdict (`approve` | `changes required`), counts of must-fix and consider.
