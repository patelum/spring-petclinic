---
name: sdlc-retro
description: Writes specs/<id>/retro.md for a finished ai-sdlc ticket - every rejection, retry, escalation, must-fix finding and human correction, each tagged with the skill, role, standard or policy most likely responsible. Use when the orchestrator finishes a ticket, or when "Improve the process" finds a ticket without a retro.
---

# Ticket retro

Goal: turn what went wrong (and what a human had to correct) into structured, attributable findings, so the process can improve. This is evidence gathering, not blame and not a summary of the work.

**Inputs:** everything in `specs/<id>/`: `status.md` (log, retries, approvals), the artifacts, `review.md`, `history/` snapshots if present, `feedback.md` if present.
**Output:** `specs/<id>/retro.md` from `.ai-sdlc/templates/retro.md`.

## What counts as a finding

| Kind | Source |
| --- | --- |
| `rejection` | A gate answered `n`: the reason code and the human's words in the `status.md` log |
| `human_edit` | An artifact changed between the agent's last snapshot in `history/` and the approved version. Diff them; summarise what the human corrected |
| `escalation` | Any escalation, and what the human chose |
| `retry` | A step that needed automatic fix attempts: what failed and why |
| `must_fix` | A `must-fix` review or security finding the implementer had to fix |
| `missed` | Something a human or a later stage caught that an earlier stage should have caught (e.g. reviewer rated "consider" what the human rejected for) |
| `unverified` | A claim or AC that was never verified (tests not run, environment missing) |
| `human_feedback` | Free text in `feedback.md` |
| `worked` | Something that clearly went well and should be kept (at most three) |

## Attribution

For each finding, name the component most likely responsible, as precisely as you can:

- `skill:<name>` (e.g. `skill:sdlc-review`) or `role:<name>`
- `orchestrator` (menu, gates, cards, status handling)
- `standard:<engineering|testing|security>`
- `policy:<key>` for `config.yaml` settings (gates, protect, escalate_on, build commands)
- `context:<file>` when the project context was missing or wrong
- `lessons` for a fact about this repo that agents did not know

Then decide **scope**:

- `repo` — specific to this codebase (a convention, a file layout, a build quirk). Fix goes in `lessons.md`, `standards/`, `context/` or `config.yaml`.
- `kit` — would happen in any repo (a skill instruction is wrong or missing). Fix goes upstream to the kit.
- `both` — a local workaround now, and a general fix upstream.

Write a one-line **suggested change**: what to add or change, where. Be concrete ("sdlc-review: classify user-facing raw exception text as must-fix"), not general ("review should be better").

## Rules

- Quote the human's words exactly where they exist.
- Every finding cites its evidence (`status.md` log line, `review.md` item, `history/` diff).
- Do not invent findings to fill the template. A clean ticket can have a short retro.
- Do not change any other file.

Report: count of findings by kind and scope, and the top suggested change.
