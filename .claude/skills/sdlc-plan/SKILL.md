---
name: sdlc-plan
description: Breaks an approved spec and design (or a defect reproduction, or a refactor characterisation) into specs/<id>/plan.md - small, ordered, independently verifiable steps. Use for the plan stage of every ai-sdlc track.
---

# Write the plan

Goal: a list of steps small enough that each can be implemented and verified on its own, so failures are caught early and are cheap to fix.

**Inputs by track:**
- feature: `spec.md`, `design.md`
- defect: `request.md`, `reproduction.md`
- refactor: `request.md`, `characterisation.md`

**Output:** `specs/<id>/plan.md` from `.ai-sdlc/templates/plan.md`.

## Procedure

1. For a defect, start with one sentence naming the root cause, citing `path:line`.
2. Write numbered steps. Each step has:
   - **Change:** what to do, in one or two sentences
   - **Files:** the files to create or edit (exact paths)
   - **Module:** the module to pass to `build.verify_module`
   - **Done when:** an observable check (a named test passes, the module builds)
3. Order steps so the build is green after each one. Put risky or shared-code steps early so problems surface early.
4. For a feature, map every AC to the step(s) and test(s) that cover it in the **Coverage** table.
5. For a refactor, every step must keep characterisation tests green and unchanged.

## Rules

- Prefer 3–8 steps. More than 10 usually means the change should be split; say so on the card's Decide line.
- Each step touches one module where possible.
- No step may edit a `protect:` path.
- Steps describe intent precisely but do not contain full code.

Return to the orchestrator: number of steps, modules, any risky step, coverage gaps (should be none).
