# PET-7 Reproduction

## Root cause
Pet names are bound and compared untrimmed: `Owner.getPet(String, boolean)` uses `compName.equalsIgnoreCase(name)` (`src/main/java/org/springframework/samples/petclinic/owner/Owner.java:150`), so `"Leo "` != `"Leo"`. Both duplicate checks call it with the raw bound name (`PetController.java:111` on add, `PetController.java:152` on edit). No trimming happens at binding (`PetController.java:94-98`, no `StringTrimmerEditor`) or in `PetValidator`. The DB unique constraint does not catch it either, because `"Leo "` differs from `"Leo"` as a stored value (assumption: H2 case-insensitive column does not ignore trailing spaces; the mocked test does not exercise the DB).

## Failing tests
File: `src/test/java/org/springframework/samples/petclinic/owner/PetControllerTests.java`, nested class `WhitespaceInPetNames`:
- `rejectsCreationWithTrailingSpaceDuplicateName`
- `rejectsCreationWithLeadingSpaceDuplicateName`
- `rejectsUpdateWithTrailingSpaceDuplicateName`
- `rejectsUpdateWithLeadingSpaceDuplicateName`

Each posts name `"petty "` or `" petty"` (existing pet is `petty`) and expects 200 with a `duplicate` field error on `name`.

## Command
```
./mvnw -q -B test -Djacoco.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest='org.springframework.samples.petclinic.owner.PetControllerTests*'
```

## Result (before fix)
Tests run: 18, Failures: 4. All four new tests fail with `Status expected:<200> but was:<302>`, meaning the form accepted the pet and redirected instead of rejecting. The 14 existing tests pass.
