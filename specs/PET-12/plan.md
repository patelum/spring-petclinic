# Plan: PET-12 Record which vet a visit is with

Notes that apply to all steps:

- `protect:` check (done in planning, `.ai-sdlc/config.yaml:44-48`): the only pattern that matches PET-12 files is `src/main/resources/db/*/schema.sql` (the three schema files, edit approved at G2). `db/*/data.sql`, `db/mysql/petclinic_db_setup_mysql.txt`, `README.md`, `messages/*.properties`, templates and Java files are NOT matched (the other patterns cover `db/migration/**`, `db/changelog/**`, `.github/workflows/**`).
- `build.verify_module` only runs `org.springframework.samples.petclinic.{module}.**`. `ClinicServiceTests` (package `service`) and `PetClinicIntegrationTests` (root package) are not in any logical module, so steps that depend on them use `build.test_one` (fully qualified class name) in addition to the module build.
- Run `build.format` (`spring-javaformat:apply`) before each module build, since `verify_module` runs `spring-javaformat:validate`.
- Design correction: the design says all owner-package `@WebMvcTest` slices need `@MockitoBean VetRepository`. Not so. Formatters are not scanned into slices by default; `PetControllerTests` opts in via `includeFilters` for `PetTypeFormatter` [`PetControllerTests.java:54-55`]. Only `VisitControllerTests` needs the mock (because `VisitController` gains the `VetRepository` dependency) and the `includeFilters` for `VetFormatter`. `OwnerControllerTests` and `PetControllerTests` need no change; `PetControllerTests` is not touched. (Step 5 re-confirms by running the whole owner module.)

## Steps

- [x] **1. Schema and seed data (DB scripts only)**
  - Change: Add nullable `vet_id` to `visits` with FK to `vets(id)` in all three schema scripts. H2: add column, FK `fk_visits_vets`, index, and move `DROP TABLE visits` before `DROP TABLE vets` (and after nothing that references it). Postgres: `vet_id INT REFERENCES vets (id)` in `CREATE TABLE IF NOT EXISTS visits` plus an idempotent `ALTER TABLE visits ADD COLUMN IF NOT EXISTS vet_id INT REFERENCES vets (id);`. MySQL: `vet_id INT(4) UNSIGNED,` and `FOREIGN KEY (vet_id) REFERENCES vets(id)` in the `CREATE TABLE`, plus a short comment block with the one-time manual `ALTER` (no `PREPARE` block). Change the h2 and mysql `INSERT INTO visits VALUES (...)` to `INSERT INTO visits (pet_id, visit_date, description) VALUES (...)`; MySQL keeps its explicit ids, so list `(id, pet_id, visit_date, description)` there. Seeded visits get no vet. Postgres `data.sql` already uses column lists, so leave it unchanged.
  - Files: `src/main/resources/db/h2/schema.sql`, `src/main/resources/db/postgres/schema.sql`, `src/main/resources/db/mysql/schema.sql`, `src/main/resources/db/h2/data.sql`, `src/main/resources/db/mysql/data.sql`
  - Module: none (resources only); use `test_one` for `org.springframework.samples.petclinic.service.ClinicServiceTests` and `org.springframework.samples.petclinic.PetClinicIntegrationTests`
  - Done when: both named classes pass with the entity still unchanged (extra nullable column is invisible to JPA), proving the H2 script runs from scratch and seed data loads; a second app start on H2 (the integration test context) does not fail on the DROP order. Risky: edits to protected schema files (approved).

- [x] **2. i18n key `vet` in all 11 locale files**
  - Change: First grep `src/main/resources/messages/` to confirm no existing `vet` key (planning grep found none). Add `vet=Vet` to every file; non-English files use the English value as a placeholder.
  - Files: `src/main/resources/messages/messages.properties`, `messages_de.properties`, `messages_en.properties`, `messages_es.properties`, `messages_fa.properties`, `messages_hi.properties`, `messages_ja.properties`, `messages_ko.properties`, `messages_pt.properties`, `messages_ru.properties`, `messages_tr.properties`
  - Module: `system`
  - Done when: `build.verify_module` for `system` is green, including `I18nPropertiesSyncTest`.

- [x] **3. `Visit.vet` mapping and persistence test (owner -> vet dependency, shared entity)**
  - Change: Add `@ManyToOne @JoinColumn(name = "vet_id") private Vet vet;` with getter/setter to `Visit`, no cascade, modelled on `Pet.type` [`Pet.java:53-55`]. Add a `ClinicServiceTests` case that loads a vet via `VetRepository`, adds a visit with that vet to a pet, saves the owner, flushes, clears and reloads, and asserts the vet id; also assert a visit without a vet still loads with `getVet() == null`. This is the risky detached/cached `Vet` check from the design; if it fails, stop and report (fallback needs a new vet-module method, which needs approval).
  - Files: `src/main/java/org/springframework/samples/petclinic/owner/Visit.java`, `src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`
  - Module: `owner`
  - Done when: `build.verify_module` for `owner` is green and `test_one` `org.springframework.samples.petclinic.service.ClinicServiceTests` passes with the new case; `PetClinicIntegrationTests` still passes.

- [x] **4. `VetFormatter`**
  - Change: New `@Component` `Formatter<Vet>` in the owner package, modelled on `PetTypeFormatter` [`PetTypeFormatter.java:36-62`]. `print` returns the id as a string (null-safe). `parse`: blank/null text returns `null`; otherwise find the vet with the matching id in `VetRepository.findAll()`; non-numeric or unknown id throws `ParseException`. Add `VetFormatterTests` mirroring `PetTypeFormatterTests` (print, parse known, parse blank returns null, parse unknown, parse non-numeric throw `ParseException`).
  - Files: `src/main/java/org/springframework/samples/petclinic/owner/VetFormatter.java`, `src/test/java/org/springframework/samples/petclinic/owner/VetFormatterTests.java`
  - Module: `owner`
  - Done when: `build.verify_module` for `owner` is green, including `VetFormatterTests`. Nothing else yet loads the formatter, so existing slices are unaffected.

- [x] **5. Add Visit form: controller, template, and slice tests together**
  - Change: Inject `VetRepository` into `VisitController`; add `@ModelAttribute("vets")` method returning `vets.findAll()` (repopulated on every request, so also on re-show). In `createOrUpdateVisitForm.html` add a `<select th:field="*{vet}">` with an empty option (value `""`, selected by default) and `th:each` options (value = vet id, label = first + last name), labelled with the `vet` key. Leave the "Previous Visits" table unchanged. Do not touch `@InitBinder` (field is bound as `vet`, not `vet.id`). In `VisitControllerTests` add `@MockitoBean VetRepository`, add `includeFilters` for `VetFormatter` to the `@WebMvcTest` (same form as `PetControllerTests`), stub `findAll` in `@BeforeEach`, and add tests for AC-1, 2, 3 (both `vet=` and field omitted), 6 (`vet=9999` and `vet=abc`: view `pets/createOrUpdateVisitForm`, field error on `vet`, `owners.save` never called), 7 (blank description with a chosen vet: `vets` in model, option selected), 9 (`vet.id=1` ignored), 11 (empty list: only empty option, POST succeeds). Existing tests in the class must stay green.
  - Files: `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java`, `src/main/resources/templates/pets/createOrUpdateVisitForm.html`, `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java`
  - Module: `owner`
  - Done when: `build.verify_module` for `owner` is green (this also runs `OwnerControllerTests` and `PetControllerTests` unmodified, confirming they need no `VetRepository` mock). Risky: the `th:field="*{vet}"` selection and error re-show depend on `VetFormatter.print`/binding behaving as the design expects; fix in the template/formatter, not by loosening tests.

- [x] **6. Show vet name on owner details**
  - Change: Add a header cell (`#{vet}`) and a body cell to the visit table in `ownerDetails.html`: `th:text="${visit.vet != null} ? ${visit.vet.firstName + ' ' + visit.vet.lastName}"`, blank when null. Add `OwnerControllerTests` cases that render the details page for an owner whose pet has one visit with a vet (row contains "First Last") and one visit without a vet (page renders, status 200, no vet text); build the `Vet` in the test, no repository mock needed.
  - Files: `src/main/resources/templates/owners/ownerDetails.html`, `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
  - Module: `owner`
  - Done when: `build.verify_module` for `owner` is green with the new render tests; `test_one` `org.springframework.samples.petclinic.PetClinicIntegrationTests` passes (legacy seeded visits render with a blank vet cell).

- [x] **7. MySQL upgrade documentation**
  - Change: Add an "Upgrading an existing database" step containing the one-time `ALTER TABLE visits ADD COLUMN vet_id ... ADD CONSTRAINT fk_visits_vets ...` statement to the MySQL doc, stating it is not idempotent (a repeated run fails with "Duplicate column name", harmless) and that skipping it causes a runtime SQL error on visit load. Add a one-line pointer in the README MySQL section [`README.md:68-82`]. The comment block in `db/mysql/schema.sql` is already done in step 1; confirm its statement matches the doc text exactly.
  - Files: `src/main/resources/db/mysql/petclinic_db_setup_mysql.txt`, `README.md`
  - Module: none (docs only); confirm with `git diff --stat` that only these two files changed in this step
  - Done when: both documents contain the identical statement, and a read-through confirms neither path matches any `protect:` glob (already verified above).

- [ ] **8. Full verification**
  - Change: Run `build.format`, then `build.verify` (whole project: unit, slice, `I18nPropertiesSyncTest`, `VetControllerTests` for AC-12, H2 integration tests). Run `MySqlIntegrationTests` and `PostgresIntegrationTests` if Docker/Testcontainers is available; otherwise record them as not run. Manual AC-8 upgrade check (if a Postgres and a MySQL instance are available): start the previous schema with visits, apply the new version (Postgres: automatic `ALTER`; MySQL: run the documented `ALTER` first), confirm existing visits are intact with null `vet_id`. Fill the Coverage table's Test and Result columns.
  - Files: `specs/PET-12/plan.md` (Coverage table only)
  - Module: whole project (`build.verify`)
  - Done when: `build.verify` is green; DB-container tests and manual upgrade checks are either passed or explicitly recorded as not run.

- [x] **9. Friendly invalid-vet message (G4 rework)**
  - Change: Add `typeMismatch.vet=` (short "invalid vet" text) after `typeMismatch.visitDate` in every locale file except `messages_en.properties`. In `VisitControllerTests` assert error code `typeMismatch` for `vet=9999` and `vet=abc`, and add a render test that the POST response body contains "invalid vet" and not "Failed to convert". No Java or template change expected.
  - Files: `src/main/resources/messages/messages*.properties` (not `_en`), `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java`
  - Module: `owner`, `system`
  - Done when: `build.verify_module` for `owner` and `system` and full `verify` are green.

## Coverage

| AC | Step(s) | Test | Result |
| --- | --- | --- | --- |
| AC-1 | 5 | `VisitControllerTests#initNewVisitFormListsVetsWithEmptyOption` | pass |
| AC-2 | 3, 4, 5 | `VisitControllerTests#processNewVisitFormSavesChosenVet`; `ClinicServiceTests#shouldPersistVisitWithVetAndWithoutVet` | pass |
| AC-3 | 4, 5 | `VisitControllerTests#processNewVisitFormWithBlankVetSavesNoVet`, `#processNewVisitFormWithoutVetFieldSavesNoVet`; `VetFormatterTests#shouldParseBlankAsNull` | pass |
| AC-4 | 3, 6 | `OwnerControllerTests#showOwnerRendersVetNameForVisitWithVet` | pass |
| AC-5 | 1, 3, 6 | `OwnerControllerTests#showOwnerRendersVisitWithoutVet`; `PetClinicIntegrationTests#ownerDetails` (seeded visits) | pass |
| AC-6 | 4, 5 | `VisitControllerTests#processNewVisitFormRejectsUnknownVetId`, `#processNewVisitFormRejectsNonNumericVetId`; `VetFormatterTests#shouldThrowParseExceptionForUnknownId`, `#shouldThrowParseExceptionForNonNumericId`; G4 rework: error code `typeMismatch` asserted in both tests, and `VisitControllerTests#invalidVetIdRendersFriendlyMessageNotRawConversionError` checks the rendered body shows "invalid vet" (key `typeMismatch.vet`) and not "Failed to convert" | pass |
| AC-7 | 5 | `VisitControllerTests#processNewVisitFormWithErrorsKeepsVetsAndSelection` | pass |
| AC-8 | 1, 7, 8 | `PetClinicIntegrationTests` (H2 from scratch, seed data); `MySqlIntegrationTests`, `PostgresIntegrationTests`; manual upgrade check | partial: H2 pass; MySQL/Postgres container tests skipped (no Docker) and manual upgrade check not run |
| AC-9 | 5 | `VisitControllerTests#processNewVisitFormIgnoresVetIdParameter` | pass |
| AC-10 | 2 | `I18nPropertiesSyncTest` | pass |
| AC-11 | 5 | `VisitControllerTests#formWithNoVetsOffersOnlyEmptyOptionAndSaves` | pass |
| AC-12 | 8 | `VetControllerTests` (unchanged, green in full verify) | pass |
