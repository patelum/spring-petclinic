# Design: PET-12 Record which vet a visit is with

## Approach

Add an optional many-to-one `Visit.vet` (nullable `visits.vet_id` FK to `vets.id`). The Add Visit form gets a `<select>` bound to `visit.vet`; a new `VetFormatter` (owner package, modelled on `PetTypeFormatter`) converts the submitted id string to a `Vet` using `VetRepository.findAll()`. The owner details page shows the vet name per visit.

Data flow:

- GET form: `VisitController` adds model attribute `vets` (from `VetRepository.findAll()`, cached) -> template renders empty option + one option per vet (value = vet id, label = "first last").
- POST: binder applies `VetFormatter.parse(text)`: blank -> `null` (AC-3); known id -> the `Vet`; unknown/non-numeric -> `ParseException` -> binding error on field `vet` -> existing `result.hasErrors()` path re-shows the form (AC-6). The `vets` model attribute is a `@ModelAttribute` method, so it is repopulated on re-show (AC-7); `th:field="*{vet}"` keeps the selection via `VetFormatter.print` (returns the id string).
- Success path unchanged: `owner.addVisit` + `owners.save(owner)` + redirect (AC-2) [`VisitController.java:108-111`].
- Owner details: add a column to the visit table, `th:text="${visit.vet != null} ? ${visit.vet.firstName + ' ' + visit.vet.lastName}"` (blank when null, AC-4/5).

## Existing code and patterns followed

- `PetTypeFormatter` — `@Component Formatter<PetType>` that looks up from a repository and throws `ParseException` when not found; same pattern for `VetFormatter` [`owner/PetTypeFormatter.java:36-62`]
- `Pet.type` `@ManyToOne @JoinColumn(name="type_id")` — same mapping style for `Visit.vet` [`owner/Pet.java:53-55`]
- `VisitController` `@ModelAttribute` methods supply form data (e.g. `minVisitDate`) on every request, including re-show after errors [`owner/VisitController.java:83-86`]
- `@InitBinder` `setDisallowedFields("id", "*.id")` already blocks `vet.id` binding; no change needed, field is bound as `vet`, not `vet.id` (AC-9) [`owner/VisitController.java:51-54`]
- `Visit` entity fields and column annotations [`owner/Visit.java:39-45`]
- `VetRepository.findAll()` is `@Cacheable("vets")`, read-only [`vet/VetRepository.java:44-46`]
- Owner details visit table [`templates/owners/ownerDetails.html:60-70`]; i18n header keys such as `visitDate`, `description` [`messages/messages.properties:41,46`]
- Schema: `visits` in the three scripts [`db/h2/schema.sql:58-65`, `db/postgres/schema.sql:47-53`, `db/mysql/schema.sql:50-56`]

## Modules and boundaries

| Module | Change |
| --- | --- |
| owner | `Visit` (new `vet` field, getter/setter); new `VetFormatter`; `VisitController` (inject `VetRepository`, add `vets` model attribute); templates `pets/createOrUpdateVisitForm.html`, `owners/ownerDetails.html`; new key in 11 `messages*.properties` |
| vet | none (read via existing public `VetRepository`/`Vet`; both already `public`) |
| model, system | none |
| resources/db | `schema.sql` x3 (protected; edits approved at G2), `data.sql` h2 and mysql (approved at G2), MySQL docs: comment in `mysql/schema.sql`, `mysql/petclinic_db_setup_mysql.txt`, `README.md` (see Schema) |

New cross-module dependencies: **owner -> vet** (`Visit` references `Vet`; `VisitController` and `VetFormatter` use `VetRepository`). Unavoidable for AC-1/AC-4 (the dropdown needs the vet list and the visit must point at a vet). Today none exists [`index.md` allowed dependencies; `owner.md` "Dependencies"]. **Approved by the human at G2 for PET-12** (`escalate_on: new_cross_module_dependency`). Dependency is one-way; vet does not import owner, so no cycle.

## Changes with weight

- Libraries: none. **No pom.xml change.**
- Schema: **approved by the human at G2 for PET-12** (`escalate_on: schema_migration`; `protect:` lists `src/main/resources/db/*/schema.sql` [`.ai-sdlc/config.yaml:48`]; the human explicitly authorised editing the three `schema.sql` files for this ticket only, including the H2 DROP reorder; the same approval covers the `data.sql` explicit-column edits below). The project has no Flyway/Liquibase; schema is hand-written SQL run at startup [`application.properties:3-4`]. Required changes:
  - `db/h2/schema.sql`: add `vet_id INTEGER` to `visits`, plus `ALTER TABLE visits ADD CONSTRAINT fk_visits_vets FOREIGN KEY (vet_id) REFERENCES vets (id);` and an index. **Also reorder the DROPs**: `visits` must be dropped before `vets` (currently `vets` is dropped at line 2, `visits` at line 4) or the new FK blocks the drop on restart.
  - `db/postgres/schema.sql`: add `vet_id INT REFERENCES vets (id)` to `visits` in `CREATE TABLE IF NOT EXISTS`, and, for existing databases, `ALTER TABLE visits ADD COLUMN IF NOT EXISTS vet_id INT REFERENCES vets (id);` (Postgres supports it, idempotent). `CREATE TABLE IF NOT EXISTS` alone does NOT alter an existing table, so the ALTER is required for AC-8.
  - `db/mysql/schema.sql` (**decided at G2: simple schema, documented one-time manual ALTER; no `PREPARE`/`information_schema` block**): only add `vet_id INT(4) UNSIGNED,` and `FOREIGN KEY (vet_id) REFERENCES vets(id)` to the existing `CREATE TABLE IF NOT EXISTS visits` (`vets` is created first, at `db/mysql/schema.sql:1`, so the FK resolves). `CREATE TABLE IF NOT EXISTS` does not alter an existing table, so existing MySQL databases need this one-time statement, run once by the operator before starting the new version:
    ```sql
    ALTER TABLE visits
      ADD COLUMN vet_id INT(4) UNSIGNED NULL,
      ADD CONSTRAINT fk_visits_vets FOREIGN KEY (vet_id) REFERENCES vets(id);
    ```
    Not idempotent (MySQL has no `ADD COLUMN IF NOT EXISTS`); a second run fails with "Duplicate column name", which is harmless. New MySQL databases need no manual step. Where it is documented: (a) a short comment block above the `visits` table in `db/mysql/schema.sql` containing the statement above; (b) a new "Upgrading an existing database" step in `db/mysql/petclinic_db_setup_mysql.txt`, the MySQL doc the README links to [`README.md:82`]; (c) a one-line pointer in the README MySQL section [`README.md:68-82`]. Doc files are not in the protect list [`.ai-sdlc/config.yaml:48`; assumption, verify in planning].
  - `data.sql` (not in the protect list but affected; edit approved at G2): h2 and mysql insert visits with positional `VALUES` of 4 columns [`db/h2/data.sql:50-53`, `db/mysql/data.sql:50-53`]; with a 5th column they fail on column-count mismatch. Change them to explicit column lists `(pet_id, visit_date, description)` (mysql keeps explicit ids). Postgres already uses column lists [`db/postgres/data.sql:50-53`]. Seeded visits get no vet.
  - Column must be nullable with no default, so existing rows are unchanged (AC-8).
- Public API: HTML form gets one optional field `vet` (id) on POST `/owners/{ownerId}/pets/{petId}/visits/new`; `/vets` and `/vets.html` unchanged (AC-12). No JSON contract change.
- Configuration: none.
- i18n: one new key (e.g. `vet=Vet`) in all 11 files under `messages/`; English placeholder for non-English locales is acceptable per spec, but must exist for `I18nPropertiesSyncTest` (AC-10). The same key serves the dropdown label and the column header. [assumption: no existing `vet` key; verify with grep during planning]

## Alternatives considered

- Store only `Integer vetId` on `Visit` (no entity reference) — removes the entity-level import but the controller/template still need `VetRepository` for the list and for name lookup, so the owner -> vet dependency remains and names need extra lookups.
- Put a new "list vets for selection" API in the vet package (e.g. a `VetSelectionService`) — rejected as more surface for no gain; `VetRepository` is already public and read-only.
- Bind with `vet.id` (`th:field="*{vet.id}"`) — rejected: conflicts with the disallowed `*.id` rule (AC-9) and a missing id would create a transient `Vet`.
- Validate the vet id in the controller via a manual `request param` instead of a Formatter — works, but diverges from the `PetTypeFormatter` pattern and loses automatic re-show/keep-selection.
- Duplicate the schema column via `ddl-auto` — rejected: `ddl-auto=none` and hand-written SQL is the project convention [`application.properties:10`].

## Testing approach

| AC | Test type | Notes |
| --- | --- | --- |
| AC-1 | slice (`VisitControllerTests`, `@WebMvcTest`) | Add `@MockitoBean VetRepository`; assert model `vets` and `<option>` markup incl. empty option |
| AC-2 | slice | POST with `vet=<id>`; verify saved `Owner`'s visit has that vet; redirect |
| AC-3 | slice | POST with `vet=` and with no `vet`; vet null; redirect |
| AC-4, AC-5 | slice (`OwnerControllerTests` view render) or integration (`PetClinicIntegrationTests`) | Rendered row shows "First Last" or blank; legacy visit renders |
| AC-6 | slice | POST `vet=9999` / `vet=abc`; view `pets/createOrUpdateVisitForm`, field error on `vet`, nothing saved |
| AC-7 | slice | Blank description + chosen vet -> `vets` still in model and option selected |
| AC-8 | integration (H2 via `PetClinicIntegrationTests`; MySQL/Postgres via existing Testcontainers tests where runnable) + manual upgrade check on Postgres (auto ALTER) and MySQL (run the documented manual ALTER first) | Startup succeeds, seeded visits have null vet |
| AC-9 | slice | POST with `vet.id=1` does not bind |
| AC-10 | existing `I18nPropertiesSyncTest` | New key in all 11 files |
| AC-11 | slice | `findAll` returns empty; only the empty option; POST succeeds |
| AC-12 | existing `VetControllerTests` | Unchanged and green |
| Persistence | `ClinicServiceTests` (`@DataJpaTest`) | Add a case saving a visit with a vet and reloading it |

Existing tests needing updates: `@WebMvcTest` slices that load `VetFormatter` (all `@WebMvcTest` tests in the owner package, `OwnerControllerTests`, `PetControllerTests`, `VisitControllerTests`) will need a `@MockitoBean VetRepository`, since formatters are component-scanned into slices (as `PetTypeFormatter` is today, which is why those tests already mock `PetTypeRepository`) [`PetControllerTests.java:67,70`].

## Risks

- Schema script breaks existing MySQL/Postgres DBs or H2 restart (FK drop order) — reorder DROPs in H2; idempotent ALTER for Postgres; documented one-time manual ALTER for MySQL (an operator who skips it gets a runtime SQL error on visit load, so the docs must be prominent); run the DB integration tests.
- Positional `INSERT ... VALUES` in h2/mysql `data.sql` fail after the column is added — change to explicit column lists (called out above).
- `Vet` objects come from the `vets` cache (serialised, detached) [`vet/VetRepository.java:45`; `PetClinicRuntimeHints.java:35`] and are assigned to a `Visit` being saved with `CascadeType.ALL` from `Pet` [`Pet.java:56`]. Cascade is from Pet to Visit only, not Visit to Vet (no cascade on `Visit.vet`), so Hibernate only writes the FK from the detached id. Verify with the `ClinicServiceTests` case; fallback is `getReferenceById`-style lookup, which would need a new vet-module method (extra cross-module surface, so not planned unless the test fails).
- Cached vet list is stale if vets are ever added at runtime — acceptable now (vets are read-only, AC-12; `vet.md` gotchas).
- `Visit.vet` is eagerly loaded by default for `@ManyToOne` and `Owner` already loads pets/visits eagerly [`Pet.java:56`, `owner.md` gotchas] — adds a join per visit; negligible for this app, `default_batch_fetch_size=16` mitigates N+1.
- Unknown vet id on POST must not become a 500 — handled by `ParseException` path; covered by AC-6 test.
- Native image/AOT: `Visit` now references `Vet`; `Vet` already has a reflection hint [`PetClinicRuntimeHints.java:35`]; no new hint expected [assumption].
