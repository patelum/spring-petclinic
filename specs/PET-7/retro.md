---
id: PET-7
track: defect
written: 2026-10-12 17:25
processed: true
---

# Retro: PET-7 Duplicate pets can be created by adding a leading or trailing space to the name

## Summary

- Gates: 2 · rejections: 1 · escalations: 0 · retries: 0 · must-fix: 0 · human edits: 0
- Top suggested change: sdlc-reproduce / sdlc-plan: require tests for the positive side (valid input still saved, normalised) and neutral test names with no ticket id

## Findings

```yaml
findings:
  - id: F1
    kind: rejection
    stage: final
    what: "G2 rejected: the fix was correct but tests for the positive side were missing (padded non-duplicate saved trimmed; whitespace-only still gives required)"
    human_words: "3 missing case: add tests for padded non-duplicate saved trimmed and whitespace-only gives required"
    evidence: "status.md log 2026-10-12 17:00; plan.md step 4"
    component: skill:sdlc-reproduce
    scope: kit
    suggested_change: "sdlc-reproduce / sdlc-plan: when a fix changes input normalisation, require tests for the unchanged-valid and edge-input paths, not only the failing cases"
  - id: F2
    kind: rejection
    stage: final
    what: "Reproduction test class was named after the ticket (Pet7WhitespaceDuplicateNames); human asked for a rename"
    human_words: "rename nested test class to drop ticket id"
    evidence: "status.md log 2026-10-12 17:00; review.md 'Resolved since last review'"
    component: standard:testing
    scope: both
    suggested_change: "standards/testing.md: test class and method names describe behaviour, never ticket ids; sdlc-reproduce to follow it"
  - id: F3
    kind: missed
    stage: review
    what: "First review (0 must-fix, 3 consider) did not flag the missing positive-side tests that the human then rejected for. The first review text was overwritten, so the exact rating is not recoverable."
    human_words: "3 missing case"
    evidence: "status.md log 2026-10-12 16:55 vs 17:00; review.md was rewritten for the re-review"
    component: skill:sdlc-review
    scope: kit
    suggested_change: "sdlc-review: check that tests cover valid and edge inputs on the changed path; keep prior review rounds (history/ or an appended section)"
  - id: F4
    kind: unverified
    stage: plan
    what: "Plan left other callers of Owner.getPet as an ungrepped assumption, and steps 2 and 3 stayed unchecked although status.md records them done (16:50). The reviewer later confirmed the callers."
    human_words: ""
    evidence: "plan.md risks section and steps 2-3; status.md log 16:50; review.md Coverage"
    component: orchestrator
    scope: kit
    suggested_change: "sdlc-implement: tick plan steps when verify completes; sdlc-plan: grep callers rather than mark them [assumption]"
  - id: F5
    kind: worked
    stage: plan
    what: "Plan chose the fix location (trim at binding) with reasons and rejected alternatives; no retries, no must-fix, and the fix was 2 production lines"
    human_words: ""
    evidence: "plan.md 'Where to normalise'; status.md retries {}"
    component: skill:sdlc-plan
    scope: kit
    suggested_change: "keep the 'decision plus not chosen' section in sdlc-plan"
```

## Notes

- The reviewer's note that existing rows with stray spaces are not cleaned up was accepted at G2 without comment.
