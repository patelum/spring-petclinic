---
name: sdlc-characterise
description: Pins the current behaviour of code about to be refactored with passing characterisation tests, recorded in specs/<id>/characterisation.md. Use for the characterise stage of the ai-sdlc refactor track.
---

# Characterise before refactoring

Goal: a safety net. Tests that describe what the code does **today**, right or wrong, so any accidental behaviour change during the refactor turns the build red.

**Inputs:** `specs/<id>/request.md`, context for the module, the target code.
**Output:** `specs/<id>/characterisation.md` from `.ai-sdlc/templates/characterisation.md`, and passing tests.

## Procedure

1. Identify the public surface of the code being refactored: public methods, endpoints, messages consumed and produced, database writes, exceptions thrown.
2. Check existing tests. Where they already pin behaviour, list them and move on.
3. For uncovered behaviour, write characterisation tests through the public surface: inputs → observed outputs, side effects and exceptions. Include odd cases (nulls, empty collections, boundary values) and assert what actually happens, even if it looks like a bug.
4. Put new tests in a clearly named class (`<Target>CharacterisationTest`) so the implementer knows not to modify them.
5. Run them with `build.verify_module`. All must pass against the current code.
6. Record in `characterisation.md`: the surface covered, the test classes, behaviours that look like bugs (flagged for later, not fixed now), and anything left uncovered with the reason.

## Rules

- Test through public interfaces, not private methods, so tests survive the refactor.
- Do not change production code.

Report: behaviours pinned, test classes, suspected bugs found, gaps.
