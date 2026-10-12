# Record which vet a visit is with (PET-12)

## Summary

The Add Visit form has an optional vet dropdown, and each pet's visit list on the owner details page shows the vet's name (blank if none was chosen). Visits get a nullable `visits.vet_id` with a foreign key to `vets`, so existing visits simply have no vet. Vets stay read-only, and an unknown vet id shows a short translatable "invalid vet" message (`typeMismatch.vet`) instead of Spring's raw conversion text.

## Details

- Spec / request: `specs/PET-12/spec.md` (request in `specs/PET-12/request.md`)
- Design: `specs/PET-12/design.md`
- Plan: `specs/PET-12/plan.md` (9 steps; step 9 added after the final gate was rejected)
- Review: `specs/PET-12/review.md`
- Modules: owner (`Visit`, `VisitController`, new `VetFormatter`, two templates), vet (read only), system (messages). New one-way dependency owner -> vet, approved at G2.
- Schema: nullable `vet_id` plus FK in the H2, MySQL and PostgreSQL `schema.sql`. The H2 drop order is now `visits` before `vets`. The h2 and mysql `data.sql` visit inserts now list their columns.
- The binder also blocks nested `vet.*`, so a request cannot rename the shared cached `Vet`.
- Docs: the one-time MySQL `ALTER` is in `petclinic_db_setup_mysql.txt` (step 4), in a comment in `db/mysql/schema.sql`, and in a README pointer.

## Verification

- `./mvnw -q verify` -> green (106 tests, 0 failures, 2 skipped: `MySqlIntegrationTests`, no Docker)
- Tests added:
  - `VetFormatterTests` (6)
  - `VisitControllerTests`: dropdown, saving with and without a vet, unknown or non-numeric id, validation re-show, `vet.id` ignored, nested `vet.*` ignored, empty vet list, rendered "invalid vet" message
  - `OwnerControllerTests`: vet name shown, visit without a vet renders
  - `ClinicServiceTests#shouldPersistVisitWithVetAndWithoutVet`
- Acceptance criteria: 11 of 12 covered by passing tests. AC-8 passed on H2 only.

## Risks and follow-ups

- AC-8 is untested on MySQL and PostgreSQL: the container tests need Docker, and the manual upgrade check was not run. The DDL was reviewed by reading only. Run `MySqlIntegrationTests` and `PostgresIntegrationTests` in CI or with Docker before merging.
- Existing MySQL databases need the documented one-time `ALTER`. Without it, loading visits fails with a SQL error. PostgreSQL upgrades itself through `ADD COLUMN IF NOT EXISTS`.
- Translations for `typeMismatch.vet` were written by the agent and need a native-speaker check. The `vet` label is still English in every non-English locale. `messages_en.properties` is intentionally empty and falls back to the default.
- `messages_de.properties` lines 7-8 already contained corrupted characters before this ticket. They were left alone.
- Consider findings left open:
  - The vet dropdown is unsorted.
  - `VetFormatter.parse` scans the cached vet list on every bind and depends on the `vets` cache staying enabled.
  - `VetFormatter` parse errors echo the raw input.
  - The select markup lacks `form-control` and duplicates the `inputField` fragment.
  - No bracket-form (`vet[firstName]`) binding test.
  - No non-default-locale test for the new message.
  - The AC-5 and AC-9 tests are weak or brittle.
