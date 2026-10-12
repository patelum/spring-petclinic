---
id: <id>
track: <track>
written: <YYYY-MM-DD HH:MM from date>
processed: false               # set to true by "Improve the process" once considered
---

# Retro: <id> <title>

## Summary

- Gates: <n> · rejections: <n> · escalations: <n> · retries: <n> · must-fix: <n> · human edits: <n>
- Top suggested change: <one line>

## Findings

```yaml
findings:
  - id: F1
    kind: rejection              # rejection | human_edit | escalation | retry | must_fix | missed | unverified | human_feedback | worked
    stage: final
    what: "Unknown vet showed Spring's raw conversion message to users"
    human_words: "3 - I ran the app and posted vet=999: the form shows the raw Spring conversion message"
    evidence: "status.md log 2026-10-12 17:02; review.md consider #3"
    component: skill:sdlc-review
    scope: both                  # repo | kit | both
    suggested_change: "sdlc-review: user-facing raw exception text is must-fix; add to standards/engineering.md"
```

## Notes

<Anything that does not fit a finding. Keep it short.>
