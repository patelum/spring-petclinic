---
id: PET-7
title: Duplicate pets can be created by adding a leading or trailing space to the name
track: defect
modules: [owner]
base_commit: 4edca78
stage: final
state: done
gate: ""
blocked_reason: ""
retries: {}
created: 2026-10-12T16:24
updated: 2026-10-12T17:20
---

## Stages

- [x] reproduce
- [x] plan (G1)
- [x] implement
- [x] verify
- [x] review
- [x] final (G2)

## Log

- 2026-10-12 16:24 intake: track defect, modules owner
- 2026-10-12 16:30 reproduce: complete; 4 failing tests in PetControllerTests (Pet7WhitespaceDuplicateNames); root cause Owner.java:150 compares untrimmed names
- 2026-10-12 16:36 plan: written (3 steps); awaiting G1 approval
- 2026-10-12 16:40 G1 plan: approved
- 2026-10-12 16:45 implement: step 1 done (PetController +2 lines); 18/18 PetControllerTests green; retries 0
- 2026-10-12 16:50 verify: scoped owner build green; full ./mvnw -q verify green
- 2026-10-12 16:55 review: 0 must-fix, 3 consider; awaiting G2 approval
- 2026-10-12 17:00 G2 final: rejected (3 missing case): add tests for padded non-duplicate saved trimmed and whitespace-only gives required; rename nested test class to drop ticket id
- 2026-10-12 17:10 implement (rework): plan step 4 done; 2 tests added, class renamed to WhitespaceInPetNames; full ./mvnw -q verify green; retries 0
- 2026-10-12 17:15 review (re-run): 0 must-fix, 3 consider; awaiting G2 approval
- 2026-10-12 17:20 G2 final: approved; pr.md written; state done
