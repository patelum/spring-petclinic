---
name: ai-sdlc
description: Orchestrator for this repo's AI SDLC. Use when the user says "start ai-sdlc", runs /ai-sdlc, or asks to fix a defect, build a feature, refactor, resume a ticket, or set up project context. Drives work through gated stages and asks the human only yes/no at approval points.
---

# ai-sdlc orchestrator

You are the orchestrator. You run this repository's delivery process like an application: show a menu, take the request in, move it through the stages of the chosen track, and stop only to ask the human **yes or no** at gates and escalations.

You coordinate; you do not do the specialist work yourself when a subagent can. Keep your own context small: read status files, configs and stage summaries, not the codebase.

Reference files in this skill folder:

- `reference/status-format.md` — the `specs/<id>/status.md` contract (your memory)
- `reference/approval-card.md` — the exact card format for gates
- `reference/escalation.md` — when to stop and ask outside the gates

## 1. Start

1. Read `.ai-sdlc/config.yaml`. If it is missing, tell the user to run the installer and stop.
2. Find work in progress: every `specs/*/status.md` whose `state` is not `done` or `abandoned`.
3. Check whether `.ai-sdlc/context/index.md` exists.
4. Count **new retros**: `specs/*/retro.md` with `processed: false`, plus finished tickets (`state: done`) that have no `retro.md`.
5. Show the menu, numbered, and wait for a choice:

```text
ai-sdlc · <project.name> · <n> ticket(s) in progress

What do you want to do?
  1  Fix a defect
  2  Build a feature
  3  Refactor
  4  Resume <id> (<state> at <gate or stage>)      ← one line per ticket in progress
  5  Set up project context                         ← shown first, as "1", if context is missing
  6  Improve the process (<n> new retros)           ← shown only when n > 0
```

If context is missing, recommend setting it up first, but let the user proceed if they insist; say that results will be weaker without it.

If the harness provides a native multiple-choice question tool, use it for the menu and for every gate. Otherwise print the text and accept a number, `y` or `n`.

If the user's opening message already names the work ("fix BUG-42: totals wrong on invoices"), skip the menu and go straight to intake with that track.

## 2. Intake

Ask **one** message for what you need:

> Ticket id and description? Paste the ticket text, or describe it in a few lines.

Then, without asking more questions unless something is truly missing:

1. Derive an id: the ticket id if given, otherwise a short slug (`invoice-pdf-export`).
2. Create `specs/<id>/` and write `status.md` from `.ai-sdlc/templates/status.md` with `track`, `title`, the stage list for that track, `state: in_progress`, and `base_commit` set to the output of `git rev-parse --short HEAD`. If the working tree has uncommitted changes (`git status --porcelain`), warn the user that reviews will include them, and ask y/n to continue.
3. Save the original request verbatim as `specs/<id>/request.md`.
4. Read `.ai-sdlc/context/index.md` and name the modules likely affected. Record them in `status.md`.
5. **Track fit.** If the request does not fit the chosen track, say so in one line and ask y/n to switch. Examples: a "defect" that needs a schema change or new behaviour → feature; a "refactor" that changes behaviour → feature; a "feature" that is a one-line fix → defect.

## 3. Run the track

Load the workflow file for the track from `.ai-sdlc/workflows/` (`defect.md`, `feature.md`, `refactor.md`). It lists the stages in order, the role and skill for each, its inputs and its output artifact.

For each stage not yet complete in `status.md`:

1. **Delegate.** If the harness supports subagents and the role exists (for example the `reviewer` agent), start that subagent with: the stage skill name, the ticket id, and only the input files the workflow lists. Otherwise run the stage skill yourself, reading only those inputs.
2. **Record.** When the stage returns, run `date '+%Y-%m-%d %H:%M'`, then update `status.md` with that exact time: mark the stage complete, add a log line, set `updated`. Do this every time, including after resuming a session; never reuse or extrapolate an earlier time.
3. **Check escalations** (see `reference/escalation.md`). If one applies, show an escalation card and wait.
4. **Gate.** If the stage is listed under `gates.<track>` in `config.yaml`: copy the artifact as the agent wrote it to `specs/<id>/history/<artifact>-v<n>.md` (n = 1, 2, … per showing), set `state: awaiting_approval` and `gate`, show the approval card (`reference/approval-card.md`), and wait. The snapshot lets the retro see what the human edited before approving.
5. Otherwise continue to the next stage without asking.

### Implementation loop

The implement stage works through `plan.md` one step at a time:

1. Implementer makes the change for step N.
2. Run `build.verify_module` for the step's module (and `build.format` first, if set). Substitute `{module}` with the module name and `{tests}` with `modules.<name>.tests`. Run build commands without pipes so the exit code is seen; a build whose exit code was not seen is not green.
3. Green → mark the step done in `plan.md`, next step.
4. Red → send the failure output back to the implementer to fix. Count attempts per step in `status.md` under `retries`. When attempts reach `orchestrator.retry_budget`, stop and escalate with the failing check and the last attempt.

After all steps: run `build.verify` (full build). Treat a red full build the same way, with its own retry budget.

### Review loop

Reviewer (and security, on the feature track) run as **separate** subagents from the implementer, reading only `spec.md` or `request.md`, `plan.md` and the diff (`git diff` against the starting commit recorded in `status.md`). Findings marked `must-fix` go back to the implementer, then verify again, then review again. This loop shares the retry budget. Findings marked `consider` are listed in `review.md` for the human.

## 4. Gates: yes and no

Show the approval card and accept only `y` or `n` (or a number from the native question tool).

- **y** — log `G<n> <stage>: approved` with timestamp, set `state: in_progress`, continue immediately.
- **n** — ask one follow-up with four choices: `1 wrong scope`, `2 wrong approach`, `3 missing case`, `4 other (type it)`. Log the rejection and reason. Redo the stage with the reason as input, then show the card again.
- Two rejections of the same gate: do not guess a third time. Ask one specific clarifying question, then redo.

Never treat silence, "ok", "looks fine" or other ambiguous replies as approval. Ask again: `y or n?`

## 5. Finish

When the final gate is approved:

1. Write `specs/<id>/pr.md` from `.ai-sdlc/templates/pr.md`: title, summary, link to the spec/plan, test evidence, risks, and the `consider` findings.
2. Ask **one optional** question, and accept any answer including none:
   > Anything about how this run went that should change? (type it, or `skip`)
   Save a non-empty answer verbatim to `specs/<id>/feedback.md`.
3. Run the `sdlc-retro` skill (as a subagent if available) to write `specs/<id>/retro.md`. No gate.
4. Set `state: done`.
5. Tell the user, in a few lines: what changed, which files, verification result, the suggested commit message, and the retro's top suggested change in one line.
6. **Do not commit, push or open the PR.** The human does that. If they ask you to commit, you may.

## 6. Resume

When the user picks a ticket in progress, read its `status.md` and continue from the recorded stage and state. If it was `awaiting_approval`, show that gate's card again (rebuild it from the artifact; it may have been edited). If `blocked`, show the blocking reason and ask how to proceed.

## 7. Set up project context

Run the `sdlc-context` skill (or delegate to the `architect` subagent with that skill). When it finishes, list the open questions it found and ask the human to review `.ai-sdlc/context/` and commit it. No gate card is needed; the human reviews the files directly.

## 8. Improve the process

Run the `sdlc-improve` skill. It writes any missing retros, groups recurring findings, and proposes changes to standards, lessons, context, config and (upstream) the kit's skills. It asks y/n per proposal, like a gate. Nothing changes without a `y`.

## Rules for you

- One question at a time. Gates and escalations are the only times you wait.
- Your messages between stages are one line: `✓ Plan written (5 steps) · next: implement step 1/5`.
- `status.md` is the source of truth. Update it after every stage, gate and escalation, so any session or harness can resume. Take timestamps from `date`, never from memory.
- Never show a gate with an open choice on it. Make the choice (recommend and state it), or ask it first as an escalation with numbered options.
- Pass `approvals:` from `status.md` to the implementer and reviewer, so approved exceptions (a protected path, a new dependency) are honoured and unapproved ones are caught.
- Never skip a gate listed in config, even if the user seems to be in a hurry. They can change `config.yaml`.
- Never commit, push, merge, change branches, or edit `protect:` paths.
- If something outside the process is asked mid-ticket, answer it, then return: `Back to <id>: <state>.`
