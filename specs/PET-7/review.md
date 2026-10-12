# Review: PET-7 Duplicate pets can be created by adding a leading or trailing space to the name

**Verdict:** approve (re-review after G2 rework; plan step 4)
**Diff:** 2 files changed (`PetController.java`, `PetControllerTests.java`), +82 / -0 lines, against `4edca78`
**Build:** `./mvnw -q -B spring-javaformat:validate test -Djacoco.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest='org.springframework.samples.petclinic.owner.**'` -> green (re-run by reviewer, no failures). Full `./mvnw -q verify` not run by the reviewer; plan steps 2 and 3 still open.

## Summary for approval card

Trims bound `String` values on the `pet` form (`StringTrimmerEditor(false)`), so the duplicate check on add and edit compares the trimmed name, and the stored name is trimmed too.
Six tests in `WhitespaceInPetNames`: four duplicate-rejection tests (failed before the fix with 302 instead of 200), one proving a padded non-duplicate name is saved trimmed, and one guarding that a whitespace-only name still gives `required`. Owner package tests and format validation are green.
Decide: accept that rows already stored with stray spaces are not cleaned up (out of scope, would need a data migration).

## Must-fix

None.

## Consider

- `src/test/java/org/springframework/samples/petclinic/owner/PetControllerTests.java:20` and `:40` - New imports are out of alphabetical order (`org.mockito.ArgumentCaptor` sits between two `org.junit` imports; `Mockito.verify` after `BDDMockito.given`). The formatter does not enforce this, so it is cosmetic. Optional tidy-up.
- `src/main/java/org/springframework/samples/petclinic/owner/Owner.java:150` - Lookup is still untrimmed. Fine now because `getPet(String, boolean)` is only reached with bound names (`PetController.java:113`, `:154`). `StringTrimmerEditor` uses `String.trim()`, so non-breaking and other Unicode spaces are not covered. Mention in the PR as a known limit.
- `specs/PET-7/plan.md:23` - Step 1 "Done when" still names the old class `Pet7WhitespaceDuplicateNames`, and `reproduction.md` has already been updated. Plan housekeeping only, not code.

## Resolved since last review

- Missing positive-side tests (padded non-duplicate saved trimmed; whitespace-only still `required`): resolved by `savesCreationWithPaddedNonDuplicateNameTrimmed` and `stillRequiresNameWhenCreationNameIsOnlyWhitespace` (details under Coverage).
- Ticket id in nested class name `Pet7WhitespaceDuplicateNames`: resolved, now `WhitespaceInPetNames` (`PetControllerTests.java:278`). No `Pet7`/`PET-7` string remains in the diff.

## Coverage

- Root cause: the fix addresses it at its source (untrimmed bound value, `PetController.java:94-98`) and not only the lookup. The edit path is covered because the bound entity is trimmed before the check at `:154`.
- `savesCreationWithPaddedNonDuplicateNameTrimmed`: posts `" Rex "`, captures the `Owner` passed to `owners.saveAndFlush` (the add path calls it at `PetController.java:128`) and asserts the pets contain `"Rex"` and not `" Rex "`. Meaningful: without the `StringTrimmerEditor` line the bound name stays `" Rex "`, the `contains("Rex")` assertion fails, so it would fail without the fix. It is a distinct check from the duplicate tests because it asserts the stored value. Reasoned from the code, not run against a reverted fix, to avoid touching the working tree.
- `stillRequiresNameWhenCreationNameIsOnlyWhitespace`: posts `"   "`, expects 200, field error code `required` on `name`, and the form view. This is a regression guard, not a fix-detecting test: `PetValidator` already uses `hasText` (per plan), so it passes with and without the fix. It protects against a future `StringTrimmerEditor(true)` or similar change that would turn the value into null and alter the error path. It is correctly framed as "still", so this is acceptable.
- Plan assumption "the `pet` form binds no other String fields": confirmed. `Pet` has `birthDate` (`LocalDate`), `type` (`PetType`) and `visits`. `NamedEntity` adds `name`, and `BaseEntity.id` is disallowed. Only `name` is a `String`. `@InitBinder("pet")` does not touch the owner or visit binders.
- Plan assumption "other callers of `Owner.getPet`": the `(String, boolean)` overload is called only from `PetController.java:113` and `:154`. `Owner` is unchanged.
- Reproduction tests: all four duplicate tests failed before the fix (302) and pass after.
- Production scope: the production diff is exactly 1 import and 1 line (`registerCustomEditor(String.class, new StringTrimmerEditor(false))` in `initPetBinder`). No other production file changed. No new dependency, schema change or protected path; only the `owner` module is touched.
- Quality: no dead code or debug output; tests follow the file's existing MockMvc style.
