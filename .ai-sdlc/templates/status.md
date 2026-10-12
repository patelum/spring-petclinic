---
id: <id>
title: <title>
track: <defect | feature | refactor>
modules: []
base_commit: <git rev-parse --short HEAD at intake>
stage: <first stage>
state: in_progress
gate: ""
blocked_reason: ""
retries: {}
created: <YYYY-MM-DDTHH:MM>
updated: <YYYY-MM-DDTHH:MM>
---

## Stages

<!-- One line per stage of the track, in order. Gated stages end with their gate number.
     defect:   reproduce, plan (G1), implement, verify, review, final (G2)
     feature:  spec (G1), design (G2), plan (G3), implement, test, verify, review, security, final (G4)
     refactor: characterise, plan (G1), implement, verify, review, final (G2)
     Gate numbers follow gates.<track> in .ai-sdlc/config.yaml if it has been changed. -->

- [ ] <stage>

## Log

- <YYYY-MM-DD HH:MM> intake: track <track>, modules <modules>
