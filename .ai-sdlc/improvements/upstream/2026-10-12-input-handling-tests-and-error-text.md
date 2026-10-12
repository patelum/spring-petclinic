Title: [lesson] spec/reproduce/review: input-handling changes lack edge-case tests and user-visible error text
Labels: lesson, sdlc-spec, sdlc-reproduce, sdlc-review, sdlc-security

**Kit version:** b53deac
**Track / stage:** defect / reproduce, feature / spec, review, security
**Seen in:** 2 tickets in one repository

## Problem
Changes that normalise, bind or validate input were delivered with tests only for the headline case. Valid-but-unusual inputs and rejected inputs were not covered, and the text a user sees for a rejected input was not specified. Reviews did not rate raw framework or exception text shown to users as a defect, so a human caught it at the final gate.

## Evidence
- A defect fix that changed how text input is normalised had tests for the reported duplicate only. The human rejected the final gate because tests for a padded non-duplicate value and for a whitespace-only value were missing. The first review had not flagged this.
- A feature that added a new bound form field had no specified behaviour for an unknown identifier. The user saw a raw type-conversion message. The human rejected the final gate. A low-severity security note about raw input being echoed in exception messages was also left unaddressed.

## Proposed change
- `skills/sdlc-spec/SKILL.md`: for every new or changed input, require acceptance criteria for empty input, unusual-but-valid input, and each rejected input, stating the exact user-visible message.
- `skills/sdlc-reproduce/SKILL.md` (and `sdlc-plan`): when the fix changes input normalisation, require tests on the valid, empty and rejected sides, not only the reported case.
- `skills/sdlc-review/SKILL.md` and `skills/sdlc-security/SKILL.md`: rate raw framework or exception text shown to a user as `must-fix`, and treat input echoed in exception messages as at least `consider`, never silently dropped.

## Regression scenario
Ticket: "Trim whitespace from the name field" (or "Add an optional reference field to a form"). A correct run adds tests for padded valid, whitespace-only, and unknown or invalid values, asserts the displayed message for each, and review flags any raw conversion message. It must not ship with only the single reported case tested or let a framework message reach the page.
