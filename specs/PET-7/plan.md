# Plan: PET-7 Duplicate pets can be created by adding leading/trailing space

**Root cause:** pet names are bound untrimmed and compared with `equalsIgnoreCase` (`src/main/java/org/springframework/samples/petclinic/owner/Owner.java:150`), so `"Leo "` does not match `"Leo"`; both duplicate checks pass the raw bound name (`PetController.java:111` add, `PetController.java:152` edit) and `initPetBinder` registers no trimming (`PetController.java:94-98`).

## Where to normalise (decision)

Chosen: trim at binding, in `PetController.initPetBinder` (`PetController.java:94-98`), by registering `new StringTrimmerEditor(false)` for `String` (whitespace-only becomes `""`, not null; `false` keeps null/empty semantics unchanged).

Why binding rather than `Owner.getPet(String, boolean)`:
- Trimming at binding fixes the stored value too. Both POST handlers save the bound `Pet` (`PetController.java:125-126`, `updatePetDetails` at `:168`). If only `getPet` trimmed the lookup, `"Leo "` would still be persisted untrimmed, leaving dirty data and a display/uniqueness inconsistency (DB unique key `unique_owner_pet_name (owner_id, name)` compares the stored string, `db/h2/schema.sql:39`; the DB cannot catch it, see reproduction.md).
- One registration covers add (`:111`), edit (`:152`) and `PetValidator` (`PetValidator.java:41-47`, which already uses `hasText`, so whitespace-only still gives `required`; a 31-char name padded with spaces is now measured after trimming, which is the desirable behaviour).
- `Owner` is a JPA aggregate shared by other callers of `getPet(String, boolean)` (`OwnerController`/templates [assumption: not grepped]); changing its semantics widens the blast radius for no gain. It also would not fix edit renames, where the bound entity is mutated before the check.
- Cost: `@InitBinder("pet")` applies only to the `pet` attribute, so owner/visit forms are unaffected (no scope creep).

Not chosen: trimming in `Owner.getPet` (fixes lookup only, see above); adding a second trim inside `processCreationForm`/`processUpdateForm` (duplicates logic in two places and mutates after binding/validation).

## Steps

- [x] **1. Trim pet name at binding**
  - Change: in `initPetBinder`, add `dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(false));` and the import `org.springframework.beans.propertyeditors.StringTrimmerEditor`. About 2 lines of production code.
  - Files: `src/main/java/org/springframework/samples/petclinic/owner/PetController.java`
  - Module: `owner`
  - Done when: the four duplicate-name tests in `PetControllerTests$WhitespaceInPetNames` pass and the 14 existing `PetControllerTests` stay green, via
    `./mvnw -q -B test -Djacoco.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest='org.springframework.samples.petclinic.owner.PetControllerTests*'`

- [ ] **2. Module check and format**
  - Change: no code change expected. Run the scoped build (includes `spring-javaformat:validate`); if it fails on formatting, run `build.format` and re-run.
  - Files: none (formatter may touch `PetController.java` only)
  - Module: `owner`
  - Done when: `./mvnw -q -B spring-javaformat:validate test -Djacoco.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest='org.springframework.samples.petclinic.owner.**'` is green.

- [ ] **3. Full verification before final gate**
  - Change: none.
  - Files: none
  - Module: `owner` (whole build)
  - Done when: `./mvnw -q verify` is green.

- [x] **4. Extra tests and rename (added after G2 rejection, human-directed)**
  - Change: in `PetControllerTests`, (a) rename nested class `Pet7WhitespaceDuplicateNames` to a name without the ticket id (e.g. `WhitespaceInPetNames`); (b) add a test that a padded non-duplicate name (e.g. `" Rex "`) is saved trimmed on add; (c) add a test that a whitespace-only name still gives a `required` error on `name`. Tests only, no production change.
  - Files: `src/test/java/org/springframework/samples/petclinic/owner/PetControllerTests.java`
  - Module: `owner`
  - Done when: scoped owner build (step 2 command) green, then `./mvnw -q verify` green.

## Constraints check

- Schema change: none. New dependency: none (`StringTrimmerEditor` is in spring-beans, already on the classpath). Protected paths: none touched (no `schema.sql`, workflows or migrations).
- Size: about 2-3 lines of production code, well under the 50-line limit (`quick_fix_max_lines`, `config.yaml:33`). No track switch needed.
- Existing tests already added for the defect are in `PetControllerTests` (reproduction.md); no new test files required.

## Risks and notes

- Existing rows already saved with a trailing/leading space are not cleaned up; that would need a data migration (schema/data change, needs approval) and is out of scope.
- Pet name is the only String field bound on the `pet` form besides none others [assumption: `Pet` binds name, birthDate, type only], so trimming all Strings on this binder is equivalent to trimming the name.
