# Duplicate pets can be created by adding a leading or trailing space to the name (PET-7)

## Summary

The duplicate-pet check compared the raw name (`Owner.java:150`), so `"Leo "` or `" Leo"` was not seen as a duplicate of `"Leo"` on add or edit, and the padded name was saved. The pet form now trims its text fields at binding (`StringTrimmerEditor` in `PetController.initPetBinder`), so the existing 'already exists' check fires and padded names are no longer stored.

## Details

- Spec / request: `specs/PET-7/request.md`
- Plan: `specs/PET-7/plan.md`
- Modules: owner (`PetController.java` +2 lines; `PetControllerTests.java` +79 lines). No schema, dependency or module-dependency change.

## Verification

- `./mvnw -q verify` → green (PetControllerTests: 20 test cases, 0 failures)
- Tests added, in `PetControllerTests$WhitespaceInPetNames`:
  - leading and trailing space duplicate rejected on add (2) and on edit (2); these failed before the fix
  - `savesCreationWithPaddedNonDuplicateNameTrimmed` (`" Rex "` saved as `"Rex"`)
  - `stillRequiresNameWhenCreationNameIsOnlyWhitespace` (regression guard; passes with or without the fix)

## Risks and follow-ups

- Rows already saved with padded names are not cleaned up; that needs a data migration (follow-up ticket, needs approval).
- `String.trim()` does not strip non-breaking or other Unicode spaces.
- The padded-name test was judged to fail without the fix by reading the code; it was not run against reverted code.
- Minor: new imports in `PetControllerTests.java` are not alphabetical (formatter does not enforce it).
- Review: `specs/PET-7/review.md` (0 must-fix, 3 consider).
