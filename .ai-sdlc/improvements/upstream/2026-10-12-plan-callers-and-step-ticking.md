Title: [lesson] sdlc-plan / sdlc-implement: callers left as assumptions and completed steps not ticked
Labels: lesson, sdlc-plan, sdlc-implement

**Kit version:** b53deac
**Track / stage:** defect / plan, implement
**Seen in:** 1 ticket in one repository

## Problem
A plan listed the other callers of a changed method as an unchecked assumption instead of finding them. During implementation, steps stayed unticked in the plan file although the status log recorded them as done, so the plan and the status disagreed.

## Evidence
A small defect fix changed a method used from more than one place. The plan marked the other callers as assumed. The reviewer later confirmed them, so nothing broke, but the plan was not evidence. Two plan steps stayed unchecked after their verify passed.

## Proposed change
- `skills/sdlc-plan/SKILL.md`: when a step changes a method or class used elsewhere, find the callers with grep, list them with file and line, and do not record them as an assumption.
- `skills/sdlc-implement/SKILL.md` and `skills/ai-sdlc/SKILL.md` (implementation loop): tick the step in `plan.md` when its scoped verify is green, before starting the next step.

## Regression scenario
Ticket: "Fix trimming in a shared helper". A correct run lists the real callers with file and line in the plan, and `plan.md` shows each step ticked when its verify is green. It must not leave callers as an assumption or finish with unticked steps that the status log records as done.
