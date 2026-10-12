# Testing standards

<!-- Team-owned after install: edit freely. Upgrades never overwrite this file. -->

## Writing tests

- Name tests and nested test classes after behaviour (`rejectsInvoiceWithNegativeTotal`), never after methods or ticket ids.
- Use the narrowest test that proves the point: unit before slice, slice before full context.
- Use the frameworks already in the module. No new test libraries.
- Mock only at boundaries (external systems, clocks, randomness), never the class under test.

## Proving changes

- A test claimed to detect a bug or prove a change must have been **seen failing** first. Otherwise call it a regression guard.
- Defects: a failing reproduction test before the fix.
- Refactors: characterisation tests, green on the original code, before any structural change; never modified during the refactor.
- Every acceptance criterion maps to at least one named test.

## Running tests

- Use the commands in `.ai-sdlc/config.yaml`. Do not pipe build commands; an unseen exit code is not green.
- Tests that cannot run here (no Docker, no external system) are reported as "not run", never as passing.

## Team additions

<!-- Add rules specific to this codebase below. -->
