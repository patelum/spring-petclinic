---
name: sdlc-spec
description: Turns a feature request into specs/<id>/spec.md with testable acceptance criteria, scope and assumptions. Use for the spec stage of the ai-sdlc feature track.
---

# Write the spec

Goal: agree **what** to build, in terms a tester can check, before anyone decides how.

**Inputs:** `specs/<id>/request.md`, `.ai-sdlc/context/index.md`, context files for affected modules. Open source only to confirm current behaviour.
**Output:** `specs/<id>/spec.md` from `.ai-sdlc/templates/spec.md`.

## Procedure

1. Restate the problem in two or three sentences, in the user's terms.
2. Describe current behaviour relevant to the request, citing `path:line`.
3. Write acceptance criteria as numbered, testable statements: `AC-1: Given …, when …, then …`. Cover the main path, the important edge cases and the error cases. Each must be checkable by an automated test.
4. List what is **out of scope** explicitly.
5. List **assumptions** you made to fill gaps in the request. These become the Decide line on the approval card.
6. List **open questions** only if they block a reasonable spec. Prefer a stated assumption over a question.

## Rules

- No design here: no class names for new code, no libraries, no endpoints unless the request dictates them.
- Keep it short. Most specs fit in under 60 lines.
- If revising after a rejection, address the stated reason first, and note what changed at the top under `Revision`.

Return to the orchestrator: number of ACs, assumptions (one line each), out-of-scope items.
