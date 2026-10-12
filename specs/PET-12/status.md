---
id: PET-12
title: Record which vet a visit is with
track: feature
modules: [owner, vet]
base_commit: 4435f42
stage: final
state: done
gate: ""
blocked_reason: ""
retries: {verify-full: 1, review-1: 1}
created: 2026-10-12T18:00
updated: 2026-10-12T22:25
---

## Stages

- [x] spec (G1) approved 2026-10-12 18:15
- [x] design (G2) approved 2026-10-12 18:45
- [x] plan (G3) approved 2026-10-12 19:00
- [x] implement
- [x] test
- [x] verify
- [x] review
- [x] security
- [x] final (G4) approved 2026-10-12 22:25

## Log

- 2026-10-12 18:00 intake: track feature, modules owner, vet (likely new owner→vet dependency and schema change; both need approval)
- 2026-10-12 18:10 spec: written (12 acceptance criteria); awaiting G1 approval
- 2026-10-12 18:15 G1 spec: approved
- 2026-10-12 18:25 design: written; new owner→vet dependency, schema change (3 schema.sql, protected), no pom change; awaiting G2 approval
- 2026-10-12 18:30 G2 design: rejected (4 other) "Everything else is approved, including the owner->vet dependency and editing the three schema.sql files for this ticket. For MySQL use a documented one-time manual ALTER, not a PREPARE block; keep the MySQL schema simple."
- 2026-10-12 18:31 design: revising with MySQL decision; owner→vet dependency and schema.sql edits approved for PET-12
- 2026-10-12 18:40 design: revised (MySQL: plain column + FK in CREATE TABLE, documented one-time manual ALTER); awaiting G2 approval
- 2026-10-12 18:45 G2 design: approved
- 2026-10-12 18:55 plan: written (8 steps, 12/12 ACs covered); notes design correction (only VisitControllerTests needs @MockitoBean VetRepository); awaiting G3 approval
- 2026-10-12 19:00 G3 plan: approved
- 2026-10-12 19:10 implement: step 1/8 done (schema + seed data); ClinicServiceTests and PetClinicIntegrationTests green; retries 0
- 2026-10-12 19:20 implement: step 2/8 done (vet key in 10 locale files; messages_en.properties intentionally empty by convention, falls back to default); system module green; retries 0
- 2026-10-12 19:35 implement: step 3/8 done (Visit.vet + ClinicServiceTests case); owner module green (55 tests); caveat: detached cached Vet not fully exercised yet, re-check in step 5/8; retries 0
- 2026-10-12 19:45 implement: step 4/8 done (VetFormatter + 6 tests); owner module green; retries 0
- 2026-10-12 20:00 implement: step 5/8 done (VisitController vets list, form dropdown, 9 VisitControllerTests); owner module green (70 tests); first run had 2 failures in the new tests' own markup assertions (selected="selected"), fixed in-step
- 2026-10-12 20:10 implement: step 6/8 done (vet column on ownerDetails, 2 OwnerControllerTests); owner module green (72 tests); PetClinicIntegrationTests green
- 2026-10-12 20:15 implement: step 7/8 done (MySQL upgrade docs + README pointer)
- 2026-10-12 20:25 verify: full ./mvnw verify RED (I18nPropertiesSyncTest.checkNonInternationalizedStrings: hardcoded strings); MySql/Postgres integration tests skipped (no Docker); sending to implementer, retries verify-full 1
- 2026-10-12 20:35 implement: step 8/8 verify green after 1 fix (empty option had &nbsp; flagged by I18nPropertiesSyncTest; now <option value=""></option>); ./mvnw verify: 103 tests, 0 failures, 2 skipped (MySqlIntegrationTests, Docker unavailable; PostgresIntegrationTests ran 0); retries verify-full 1
- 2026-10-12 20:45 test: 11/12 ACs covered by passing tests, no tests added; AC-8 partial (H2 only; MySQL/Postgres containers and manual upgrade check not run, no Docker/DB)
- 2026-10-12 20:46 verify: ./mvnw verify green (103 tests, 2 skipped)
- 2026-10-12 21:00 review: 1 must-fix (AC-8 unverified on MySQL/Postgres: environment limit, no Docker; carried to final gate), 5 consider. security: 1 medium must-fix (VisitController @InitBinder lets vet.firstName/vet.lastName bind onto the shared cached Vet), 3 low consider. Security section was overwritten by concurrent review write; will re-append. Sending security must-fix to implementer, retries review-1: 1
- 2026-10-12 21:20 implement (rework): VisitController binder now also disallows vet.*; 2 tests added (red without fix, green with); owner module and full verify green
- 2026-10-12 21:40 review (re-run): 0 must-fix, 7 consider; security (re-run): 0 high/medium, 3 low; vet.* binder fix confirmed; awaiting G4 approval
- 2026-10-12 21:50 G4 final: rejected (3 missing case) "posted vet=999: form shows the raw Spring conversion message (Failed to convert property value of type java.lang.String to required type ...Vet...). Show a short, translatable message instead, e.g. 'invalid vet', using the existing messages files and the typeMismatch convention."
- 2026-10-12 22:05 implement (rework): plan step 9, typeMismatch.vet added to 10 locale files (en intentionally empty); tests assert typeMismatch code and rendered 'invalid vet'; owner/system modules and full verify green (106 tests, 2 skipped)
- 2026-10-12 22:15 review (rework): 0 must-fix, 2 consider; awaiting G4 approval
- 2026-10-12 22:25 G4 final: approved; pr.md written; state done
