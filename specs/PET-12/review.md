# Review: PET-12 Record which vet a visit is with

**Verdict:** approve (second pass, after the binder fix)
**Diff:** 23 modified tracked files plus 2 new Java files (`VetFormatter`, `VetFormatterTests`), against `4435f42`
**Build:** not re-run by reviewer (read-only). Plan.md Coverage records a green `build.verify` on H2 only; MySQL/PostgreSQL container tests were not run (no Docker).

## Summary for approval card

Adds an optional vet to a visit: nullable `visits.vet_id` with FK in the three schemas, `Visit.vet` (`@ManyToOne`), a `VetFormatter`, a vet dropdown on Add Visit, a vet column on owner details, and i18n key `vet`. The binder now also blocks nested `vet.*` so a request cannot mutate the cached `Vet`.
Verified by slice, formatter and `ClinicServiceTests` tests plus H2 integration. The DDL was read through by hand for all three dialects and no defect was found.
Human to decide at the final gate: accept AC-8 as unrun on MySQL/PostgreSQL (or require a CI/Docker run), and accept the manual MySQL `ALTER` upgrade step.

## Must-fix

None.

## Consider

- `src/main/resources/db/mysql/schema.sql:50-64`, `src/main/resources/db/postgres/schema.sql:47-56`, `plan.md:71` — AC-8 is untested on MySQL and PostgreSQL (`MySqlIntegrationTests` and `PostgresIntegrationTests` need Docker, which is not available here; the upgrade path was not exercised). Reading the DDL found no defect.
  - FK syntax is valid in each dialect.
  - `vet_id` matches `vets.id` in each dialect: MySQL `INT(4) UNSIGNED` against `INT(4) UNSIGNED` (`mysql/schema.sql:2`), PostgreSQL `INT` against `INT` (`postgres/schema.sql:2`), H2 `INTEGER` against `INTEGER` (`h2/schema.sql:11`).
  - H2 drops `visits` before `vets` (`h2/schema.sql:2-3`), and `vets` is created before `visits`.
  - The Postgres `ADD COLUMN IF NOT EXISTS` is a no-op, including the FK, when the column already exists, so it is idempotent. It also does nothing on a fresh create.
  - The MySQL `ALTER` text is the same in the schema.sql comment (`:50-54`) and in `petclinic_db_setup_mysql.txt` step 4, and the README points to "step 4".
  - Recommend a CI or Docker run before merge, and update the Coverage table.
- `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java:59` — The binder fix is sound. The `vet.*` pattern blocks `vet.firstName` and `vet.lastName` but not plain `vet`, so the formatter still binds it. The cached shared `Vet` cannot be mutated through nested binding. `VisitControllerTests.java:253-278` covers this: nested properties are ignored with a vet present (identity check on the shared instance), and a nested property without `vet` does not fail. No action.
- `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java:94-97` — The vet list is unsorted (the spec leaves the order open). Sorting by last name, first name would give a stable dropdown. A sort must not mutate the cached collection.
- `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java:318-325` — `showOwnerRendersVisitWithoutVet` asserts `not(containsString("null"))` over the whole page, which is brittle. It never asserts the vet cell is blank (AC-5). Assert on the row instead.
- `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java:241-250` — The AC-9 test only checks `getVet()` is null. This is still a weak signal on its own, but the new nested-binding tests strengthen it. A case with `vet=7&vet.id=9` would be tighter.
- `src/main/resources/templates/pets/createOrUpdateVisitForm.html:34-47` — The select block duplicates the markup of `fragments/inputField` and has no `form-control` class, so it is inconsistent with the sibling inputs.
- `src/main/java/org/springframework/samples/petclinic/owner/VetFormatter.java:58-66` — `parse` scans `findAll()` linearly on every bind. That is fine for the cached list, but it relies on the `vets` cache staying enabled.
- `src/main/resources/messages/messages_en.properties` — Deliberately unchanged (the file is empty and falls back to the default bundle). This deviates from the plan's list of 11 files but is correct, and `I18nPropertiesSyncTest` guards it. No action.

## Coverage

- AC-1, 2, 3, 6, 7, 9, 11: `VisitControllerTests` (new cases, including the nested-binding tests) and `VetFormatterTests`. Adequate.
- AC-4, 5: `OwnerControllerTests#showOwnerRendersVetNameForVisitWithVet` and `#showOwnerRendersVisitWithoutVet` (the latter is weak, see Consider). The seeded-visit render is covered by `PetClinicIntegrationTests`.
- Persistence: `ClinicServiceTests#shouldPersistVisitWithVetAndWithoutVet` covers the detached and cached `Vet` risk.
- AC-8: H2 passes. MySQL and PostgreSQL are not run (Consider above). DDL checked by reading.
- AC-10: `I18nPropertiesSyncTest`. The `vet` key is present in 10 files, and the English bundle falls back.
- AC-12: no changes under the vet package. `VetControllerTests` is unchanged.
- Boundaries: the only new dependency is owner -> vet (`Visit.java:22`, `VisitController.java:22-23`, `VetFormatter.java:19-20`), as approved. Protected paths: only the approved `schema.sql` files were changed. Patterns match `PetTypeFormatter` and `Pet.type`.

## Security

Second pass. Scope: the diff against `4435f42` plus untracked files (`VetFormatter.java`, `VetFormatterTests.java`).

### Fix verification (first-pass medium: nested `vet.*` binding)

Closed. `VisitController.java:59` now has `setDisallowedFields("id", "*.id", "vet.*")`. This is reasoned from Spring's `DataBinder` behaviour; I did not run the variants below.

- `vet.firstName`, `vet.lastName`, `vet.specialties[0].name`, `vet.id`: all match the `vet.*` prefix pattern and are dropped before binding.
- Case variants such as `VET.firstName`: `DataBinder` matches disallowed patterns case-insensitively. Bean property names are case-sensitive, so a differently-cased name would not reach `Vet` anyway.
- `vet[firstName]`, `vet['firstName']`: these do not match `vet.*`, but `Vet` is not a Map, List or array. `BeanWrapper` raises `InvalidPropertyException`, which is recorded as a binding error with no mutation. When `vet` is null, auto-grow cannot create an indexed value either.
- Plain `vet` is deliberately allowed and goes through `VetFormatter`, which returns the cached instance and never creates a new one.
- Test gap, see Findings 1: `VisitControllerTests.java:259-261, 275` cover `vet.firstName`, `vet.lastName` and `vet.id`. They do not cover the bracket form or a case variant.

### Findings

1. low / consider - `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java:253-278`: no regression test for the bracket form (`vet[firstName]`) or a case variant (`VET.firstName`). Fix: add one `.param("vet[firstName]", "Hacked")` case and assert the shared `Vet` is unchanged. It may return a binding error, so assert the vet state rather than a 302.
2. low / consider - `src/main/java/org/springframework/samples/petclinic/owner/VetFormatter.java:54,66`: the `ParseException` messages echo the raw submitted text. Spring wraps them in a `typeMismatch` field error. Thymeleaf escapes `th:errors` and `th:field` (`createOrUpdateVisitForm.html:34-47`), so there is no XSS. The text can appear in logs and exception chains. Fix: use a constant message such as "invalid vet" without the input.
3. low / consider - `src/main/resources/db/mysql/petclinic_db_setup_mysql.txt:33-44`, `src/main/resources/db/mysql/schema.sql:50-54`: the upgrade needs a manual `ALTER`, and an unupgraded database fails with a SQL error at runtime. This is an operational risk, not a vulnerability. The DDL is static, so there is no injection. The documentation is adequate.

### Re-assessment of the other checklist items (no findings)

- XSS: `ownerDetails.html:71` and `createOrUpdateVisitForm.html:41` use `th:text` and `th:value`, which escape. Vet names come from the vets table, which is read-only in this app.
- SQL injection: no new queries. `Visit.vet` is a JPA `@ManyToOne` (`Visit.java:50-52`), and the schema and data scripts are static DDL and DML. The `data.sql` changes only add explicit column lists.
- IDOR and access control: no new endpoint. `loadPetWithVisit` (`VisitController.java:73-89`) still scopes the pet to the owner. The vet list and names are already public through `/vets`. The app has no authentication, so there is no new gap.
- Mass assignment: `Visit` binds `date`, `description` and `vet` only. `id` and `vet.*` are disallowed. A nonexistent vet id is rejected (AC-6) and does not cause a server error.
- Information exposure: the response shows only vet first and last name, and the id in `option value`. These are already public in `/vets`. No new dependencies. No CSRF change, because the form relies on the existing Thymeleaf-managed behaviour.

Security verdict: issues found (0 high / 0 medium / 3 low). No must-fix items.


## Rework review (G4)

Scope: plan step 9 (`typeMismatch.vet`), diff against 4435f42. Read-only review; tests were not re-run by the reviewer (status.md records the full verify as green).

Verdict: approve. 0 must-fix, 2 consider.

Checks that passed
- Coverage: `typeMismatch.vet` exists in messages.properties:10 and in de, es, fa, hi, ja, ko, pt, ru and tr. `messages_en.properties` is 110 bytes and has no `typeMismatch.vet`, so lookups fall back to the base file ("invalid vet"). That matches the intent. Placeholders: none are needed, and none are used.
- Encoding: `application.properties` sets only `spring.messages.basename=messages/messages` (line 16) and no `spring.messages.encoding`. Spring Boot's default is UTF-8, so every bundle is read as UTF-8. The new de line 10 is valid UTF-8 (`ü` is C3 BC), the same as the other umlaut lines (de:18, 22, 28, 41, 45, 50). The premise about ISO-8859-1 does not hold. de:7-8 contain EF BF BD (U+FFFD, already-corrupted text), not ISO-8859-1 bytes. That is pre-existing and was not touched. hi uses `\uXXXX` escapes and decodes to "अमान्य पशुचिकित्सक", which is correct.
- Translations are plausible and consistent in register with each file's neighbouring `typeMismatch.*` lines: de "ungültiger Tierarzt", es/pt without accents like the existing "invalida", ru "неправильный ветеринар", tr "geçersiz veteriner", ja "無効な獣医です", ko "잘못된 수의사입니다", fa "دامپزشک نامعتبر".
- Tests: `VisitControllerTests.java:201` `invalidVetIdRendersFriendlyMessageNotRawConversionError` posts `9999` and `abc` through MockMvc. It asserts status 200, the form view, body contains "invalid vet" and body does not contain "Failed to convert". This goes through the real MessageSource and Thymeleaf `th:errors` rendering, not just the error code. The two reject tests also assert the `typeMismatch` code and `verify(owners, never()).save`. AC-6 is covered: nothing is saved, the form is re-shown, and no server error occurs.

Findings
1. consider - `src/main/resources/messages/messages_{de,es,fa,hi,ja,ko,pt,ru,tr}.properties` (the new `vet=Vet` line, e.g. messages_de.properties:35): the label is left as the English "Vet" in every locale. Consistent with an acceptable placeholder, but translators should follow up. This is outside the G4 rework.
2. consider - `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java:201`: the render test runs only in the default locale, so the per-locale keys are unverified by any test. A key typo in a non-default file would go unnoticed. A cheap follow-up is a test with `Accept-Language: de` that asserts "ungültiger Tierarzt". Not required for AC-6.
