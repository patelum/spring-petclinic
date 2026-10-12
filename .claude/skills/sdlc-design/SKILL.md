---
name: sdlc-design
description: Produces specs/<id>/design.md - how an approved spec will be implemented in this Java monolith, which modules and classes change, and any new dependencies. Use for the design stage of the ai-sdlc feature track.
---

# Write the design

Goal: decide **how**, in enough detail that the plan is mechanical and a reviewer can spot a wrong approach before code exists.

**Inputs:** `specs/<id>/spec.md`, context files for affected modules, the source files you will change.
**Output:** `specs/<id>/design.md` from `.ai-sdlc/templates/design.md`.

## Procedure

1. Find the existing code each AC touches. Read it. Cite `path:line`.
2. Look for an existing pattern to follow: how similar features are built in this module (services, repositories, controllers, DTOs, mappers, events, error handling). Follow it unless there is a stated reason not to.
3. Describe the approach: components added or changed, their responsibilities, and how data flows through them. A short list or a small text diagram, not prose.
4. **Boundaries:** list every module touched. State whether any new cross-module dependency is introduced (`module A → module B`), and if so why it cannot be avoided.
5. **Changes with weight:** new libraries, schema changes (new migration, never edit an applied one), public API changes, config changes. Each needs justification.
6. **Alternatives:** one or two options you rejected, one line each with the reason.
7. **Testing approach:** which test types prove which ACs (unit, slice, integration).
8. **Risks:** what could break elsewhere, and how the plan will guard against it.

## Rules

- Prefer the smallest design that satisfies every AC.
- Prefer existing libraries and patterns in this codebase over new ones.
- Anything in step 4 or 5 goes on the approval card's Decide line.

Return to the orchestrator: approach in one line, modules touched, new dependency/library/schema changes (or "none"), decisions for the human.
