# Spec: PET-12 Record which vet a visit is with

## Problem

When booking a visit, staff cannot say which vet it is with. They want to optionally pick one of the clinic's vets on the Add Visit form, and see the vet's name beside each visit on the owner details page. Existing visits have no vet and must keep working. Vets stay read-only.

## Current behaviour

- A `Visit` has only `date` and `description`; no vet reference. [verified: owner/Visit.java:39-45]
- The `visits` table has `id`, `pet_id`, `visit_date`, `description`. [verified: db/h2/schema.sql:58-63; mysql/postgres copies not opened, [assumption] same shape]
- The Add Visit form is `GET/POST /owners/{ownerId}/pets/{petId}/visits/new`; the model carries `pet`, `owner`, `visit`, `minVisitDate`. No vet list is supplied. [verified: owner/VisitController.java:63-97]
- Date must be after today, else error `typeMismatch.visitDate`; on success the owner is saved and the user is redirected to `/owners/{ownerId}` with a flash message. [verified: VisitController.java:100-111]
- Owner details lists each pet's visits with date and description columns. [verified: templates/owners/ownerDetails.html:63-69]
- The Add Visit page also lists previous visits (date, description). [verified: templates/pets/createOrUpdateVisitForm.html:45-53]
- Vets are read via `VetRepository` (`findAll`, cached as `vets`); no owner-to-vet dependency exists today. [verified: vet.md; index.md "Allowed module dependencies"]

## Acceptance criteria

- **AC-1:** Given at least one vet exists, when the Add Visit form is opened (GET), then it contains a vet dropdown whose options are all vets (displayed by name) plus an empty "no vet" option selected by default.
- **AC-2:** Given a valid date and description and a vet selected, when the form is submitted, then the visit is saved with that vet and the user is redirected to `/owners/{ownerId}` with the existing success message.
- **AC-3:** Given a valid date and description and the empty option selected (or the field omitted), when the form is submitted, then the visit is saved with no vet and the submission succeeds exactly as today.
- **AC-4:** Given a visit with a vet, when the owner details page is rendered, then that visit's row shows the vet's name (first and last name) next to its date and description.
- **AC-5:** Given a visit with no vet (including every pre-existing visit), when the owner details page is rendered, then the vet cell is blank and the page renders without error.
- **AC-6:** Given the submitted vet identifier does not match any existing vet, when the form is submitted, then the visit is not saved and the form is re-shown with an error (no server error page).
- **AC-7:** Given validation fails (e.g. blank description or date not after today), when the form is re-shown, then the vet dropdown is still populated and keeps the user's chosen vet.
- **AC-8:** Given an existing database with visits, when the application starts with the updated schema scripts, then existing visits are unchanged and have no vet, on H2, MySQL and PostgreSQL.
- **AC-9:** Given a request that tries to bind the vet's own `id` fields (e.g. `vet.id`), when submitted, then the binding is ignored, consistent with the existing disallowed-fields rule. [verified rule: VisitController.java:51-54]
- **AC-10:** Given the new column header is added to the owner details visit table, when the app runs in any of the 11 supported locales, then the label resolves in each locale and `I18nPropertiesSyncTest` passes.
- **AC-11:** Given the vet list is empty, when the Add Visit form is opened, then the dropdown shows only the empty option and a visit can still be booked.
- **AC-12:** No new vet create/update/delete capability is exposed; the `/vets.html` and `/vets` behaviour is unchanged.

## Out of scope

- Vet management (create, edit, delete) and any change to `/vets` pages or JSON output.
- Editing or changing the vet of an already-booked visit.
- Backfilling vets onto existing visits.
- Showing the vet on the "Previous Visits" table of the Add Visit page (only owner details is requested).
- Checking vet availability, specialty matching, or double-booking.
- Filtering or searching visits by vet.

## Assumptions

- Changing the schema is needed (a vet reference on visits) in all three `schema.sql` files, plus possibly `data.sql`; this needs explicit human approval per project rule 5 and the `schema_migration` escalation. The spec states only the outcome.
- Owner and vet modules are currently independent; showing vets in the owner flow implies a new owner-to-vet dependency, which needs human approval. Whether it is accepted is a design matter.
- The dropdown lists all vets sorted by name or by id, unspecified; the vet name is shown as "first last".
- The "previous visits" list on the Add Visit page need not show the vet (see out of scope).
- A submitted vet that does not exist is a validation error (AC-6), not silently treated as "no vet".
- New UI labels require a message key in all 11 locale files; non-English values may be English placeholders until translated.
- Sample data in `data.sql` need not assign vets to the seeded visits.
- The vet cache (`vets`) may be used for reading the dropdown list. [assumption]

## Open questions

None blocking.
