# status.md format

`specs/<id>/status.md` is the orchestrator's memory. Any session, in any harness, must be able to resume a ticket from this file alone. Keep the front matter exact; keep the log append-only.

```markdown
---
id: JIRA-123
title: Add invoice PDF export
track: feature                  # defect | feature | refactor
modules: [billing, web]
base_commit: 3f9c2e1            # HEAD when the ticket started; reviews diff against this
stage: design                   # current or next stage
state: awaiting_approval        # in_progress | awaiting_approval | blocked | done | abandoned
gate: G2                        # set only while awaiting_approval
blocked_reason: ""              # set only while blocked
retries:                        # automatic fix attempts, keyed by step
  implement-3: 2
  verify-full: 0
approvals:                      # exceptions a human approved for this ticket only
  - new_cross_module_dependency: owner -> vet      # G2
  - protected_path: src/main/resources/db/*/schema.sql   # G2
created: 2026-10-12T14:02
updated: 2026-10-12T15:40
---

## Stages

- [x] spec — G1 approved 2026-10-12 14:20
- [~] design — awaiting G2
- [ ] plan — G3
- [ ] implement
- [ ] test
- [ ] verify
- [ ] review
- [ ] security
- [ ] final — G4

## Log

- 2026-10-12 14:02 intake: track feature, modules billing, web
- 2026-10-12 14:15 spec: written (4 acceptance criteria)
- 2026-10-12 14:20 G1 spec: approved
- 2026-10-12 14:31 design: written
- 2026-10-12 14:33 G2 design: rejected (2 wrong approach) "reuse ReportService for rendering"
- 2026-10-12 15:40 design: revised
```

## Rules

- Stage markers: `[ ]` not started, `[~]` in progress or awaiting, `[x]` complete, `[-]` skipped (with reason).
- Gates are numbered in the order they occur in the track, starting at G1. The final gate is always the last.
- `state: blocked` is for escalations waiting on the human. Record what is needed in `blocked_reason`.
- Times are local, `YYYY-MM-DD HH:MM`, and **must come from the clock**: run `date '+%Y-%m-%d %H:%M'` before writing a log line or `updated`. Never estimate or increment times yourself.
- Rejection log lines include the reason number and any text the human typed, in quotes.
- Never delete log lines. Correct mistakes with a new line.
