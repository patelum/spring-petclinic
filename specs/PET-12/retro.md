---
id: PET-12
track: feature
written: 2026-10-12 18:26
processed: true
---

# Retro: PET-12 Record which vet a visit is with

## Summary

- Gates: 6 (G1, G2 x2, G3, G4 x2) · rejections: 2 · escalations: 0 · retries: 2 · must-fix: 2 · human edits: 0
- Top suggested change: sdlc-spec/sdlc-review: enumerate bad-input cases for new bound fields and treat user-visible raw framework errors as must-fix

## Findings

```yaml
findings:
  - id: F1
    kind: rejection
    stage: design
    what: "Design left MySQL upgrade approach open; human chose a documented manual ALTER and approved the owner->vet dependency and schema.sql edits"
    human_words: "For MySQL use a documented one-time manual ALTER, not a PREPARE block; keep the MySQL schema simple."
    evidence: "status.md log 2026-10-12 18:30 G2 rejected (4 other)"
    component: skill:sdlc-design
    scope: both
    suggested_change: "sdlc-design: for schema changes to protected schema files, propose the simplest upgrade path per dialect up front and ask the human to choose; record the MySQL convention in lessons.md"
  - id: F2
    kind: retry
    stage: verify
    what: "Full verify red: I18nPropertiesSyncTest flagged a hardcoded &nbsp; in the empty select option; fixed once"
    human_words: ""
    evidence: "status.md log 2026-10-12 20:25 and 20:35 (retries verify-full 1)"
    component: standard:engineering
    scope: repo
    suggested_change: "lessons.md / standards/engineering.md: I18nPropertiesSyncTest rejects hardcoded text in templates, including &nbsp;; run it in the scoped module build"
  - id: F3
    kind: must_fix
    stage: security
    what: "Medium: VisitController @InitBinder let vet.firstName/vet.lastName bind onto the shared cached Vet; fixed with vet.* disallowed fields plus 2 tests"
    human_words: ""
    evidence: "status.md log 2026-10-12 21:00 and 21:20; review.md Security fix verification"
    component: skill:sdlc-design
    scope: both
    suggested_change: "sdlc-design: when a formatter-bound entity reference comes from a shared cache, require disallowing nested properties in the binder; add to standards/security.md"
  - id: F4
    kind: unverified
    stage: test
    what: "AC-8 (schema on MySQL/PostgreSQL and the upgrade path) never run: no Docker; DDL only read by hand; 2 integration tests skipped"
    human_words: ""
    evidence: "status.md log 2026-10-12 20:45; review.md must-fix then Consider #1 (carried to final gate)"
    component: policy:build.verify
    scope: repo
    suggested_change: "config.yaml/context: note Docker is required for DB integration tests; sdlc-test should flag the AC as unverified at the spec stage when the environment lacks Docker"
  - id: F5
    kind: missed
    stage: final
    what: "Unknown vet id (vet=999) showed Spring's raw conversion message to users; AC-6 only required rejection and no save, and review rated raw text as neither must-fix nor flagged it"
    human_words: "posted vet=999: form shows the raw Spring conversion message (Failed to convert property value of type java.lang.String to required type ...Vet...). Show a short, translatable message instead, e.g. 'invalid vet', using the existing messages files and the typeMismatch convention."
    evidence: "status.md log 2026-10-12 21:50 G4 rejected (3 missing case); plan.md step 9"
    component: skill:sdlc-spec
    scope: both
    suggested_change: "sdlc-spec: for each new user-bound field require an AC for the user-visible message on invalid input; sdlc-review: raw framework exception text in the UI is must-fix; add typeMismatch.<field> convention to standards/engineering.md"
  - id: F6
    kind: missed
    stage: security
    what: "Security low: VetFormatter ParseException messages echo raw submitted text; constant message advised. Partly overlaps F5 and was left unaddressed"
    human_words: ""
    evidence: "review.md Security Findings #2"
    component: skill:sdlc-implement
    scope: kit
    suggested_change: "sdlc-implement: formatter/parse errors use constant messages without echoing input"
  - id: F7
    kind: worked
    stage: implement
    what: "Risky detached/cached Vet persistence flagged in design and checked by a ClinicServiceTests case; reworks added tests that were red without the fix"
    human_words: ""
    evidence: "design.md:86; plan.md step 3; status.md log 2026-10-12 21:20"
    component: skill:sdlc-plan
    scope: kit
    suggested_change: "Keep: design risks become explicit plan verification steps; rework tests must fail without the fix"
```

## Notes

- Review.md contained a concurrent-write overwrite of the security section (status.md 21:00); it was re-appended. Possible orchestrator issue if review and security run in parallel.
- Review "Consider" items not acted on: unsorted vet dropdown, weak AC-5 assertion, select markup duplication, untranslated `vet` label in 9 locales, no per-locale typeMismatch test.
