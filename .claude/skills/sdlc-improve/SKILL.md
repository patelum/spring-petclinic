---
name: sdlc-improve
description: Improves this repo's AI SDLC from evidence - reads ticket retros, groups recurring problems, proposes concrete changes to lessons, standards, context and config (applied locally after a human y) and drafts general fixes as issues for the kit maintainers. Use for "Improve the process" in the ai-sdlc menu.
---

# Improve the process

Goal: every rejection, correction and miss makes the next ticket better. Locally, so this team benefits today, and upstream, so every team using the kit benefits after a maintainer reviews it.

**Inputs:** `specs/*/retro.md`, `specs/*/status.md`, `.ai-sdlc/lessons.md`, `.ai-sdlc/standards/*.md`, `.ai-sdlc/context/`, `.ai-sdlc/config.yaml`, `.ai-sdlc/improvements/log.md`, `.ai-sdlc/VERSION`.
**Outputs:** approved local edits, upstream drafts in `.ai-sdlc/improvements/upstream/`, a decision log in `.ai-sdlc/improvements/log.md`, and retros marked `processed: true`.

If `config.yaml` has no `feedback:` section (older installs), use the defaults: `upstream_repo: patelum/Autonomous-SDLC-Java`, `upstream: draft`, `max_proposals: 5`.

## 1. Gather

1. For every finished ticket (`state: done`) with no `retro.md`, run the `sdlc-retro` skill first.
2. Read every retro with `processed: false`. Collect its findings except `worked`.
3. Read `.ai-sdlc/improvements/log.md`. Drop findings that match a proposal already **rejected** there, unless there is new evidence since.

## 2. Group and rank

1. Group findings that share a component and a cause, across tickets ("review under-rated user-facing error text" in two tickets is one group).
2. Drop groups already handled: the fix is already in `lessons.md`, `standards/`, `context/` or `config.yaml`, or the installed skill text already contains the rule (the kit was fixed since).
3. Rank by: number of tickets affected, then kind (`missed` and `rejection` > `must_fix` and `escalation` > `retry` and `human_edit` > `unverified`), then cost of the miss.
4. Take at most `feedback.max_proposals` (default 5) proposals per session. Say how many more remain.

## 3. Propose, one at a time

For each group, decide where the fix belongs:

| Fix type | Local target (this repo) | Upstream (kit) |
| --- | --- | --- |
| Fact about this codebase | `.ai-sdlc/lessons.md` | — |
| Team rule | `.ai-sdlc/standards/<file>.md`, under **Team additions** | if it would help any team: a default for the shipped standard |
| Wrong or missing context | `.ai-sdlc/context/...` | — |
| Policy (gates, protect, escalate_on, build commands) | `.ai-sdlc/config.yaml` | if the default is wrong for most repos |
| Skill, role, workflow, template or orchestrator instruction | a workaround rule in `standards/` or `lessons.md` | **the real fix**, as an upstream draft |

Never edit installed skills, roles, workflows or templates in this repo: upgrades overwrite them, so the lesson would be lost. Put the local workaround in `standards/` or `lessons.md`, and the real fix upstream.

Show one card per proposal and wait:

```text
PROPOSAL <n>/<total> · <component> · scope <repo | kit | both>
Problem:  <one line>
Evidence: <ticket F-id (kind, stage)>, <ticket F-id …>  · <k> ticket(s)
Local:    <file>: <exact text to add or change, or "none">
Upstream: <one-line summary of the kit change, or "none">
Apply? [y / n]
```

- **y** — make the local edit exactly as shown (append lessons and team additions with `(source: <ticket ids>)`), write the upstream draft if any, log the decision.
- **n** — log it as rejected with any reason the human gives, so it is not proposed again without new evidence.
- Every **Local** line is the exact text, so `y` approves a concrete change. For `config.yaml`, show the before and after lines.

## 4. Upstream drafts

For each approved proposal with an upstream part, write `.ai-sdlc/improvements/upstream/<YYYY-MM-DD>-<slug>.md`, ready to paste as an issue on the kit repository (`feedback.upstream_repo` in config, default `patelum/Autonomous-SDLC-Java`):

```markdown
Title: [lesson] <component>: <one-line problem>
Labels: lesson, <component>

**Kit version:** <contents of .ai-sdlc/VERSION>
**Track / stage:** <track> / <stage>
**Seen in:** <k> ticket(s) in one repository

## Problem
<What went wrong, in general terms.>

## Evidence
<Paraphrased and anonymised: no source code, no internal names, hosts, people or ticket text. Describe the situation generically.>

## Proposed change
<Exact wording to add or change in the named skill / role / standard, with the file path in the kit.>

## Regression scenario
<A small generic ticket that would expose the problem, what a correct run must do, and what it must not do.>
```

Privacy is the human's call: drafts are files only. Never post them anywhere unless `feedback.upstream` is `file` **and** the human answers `y` to a separate question naming the repository; then use `gh issue create -R <repo> --title … --body-file …` if `gh` is available.

## 5. Record

1. Append each decision to `.ai-sdlc/improvements/log.md`: date (from `date`), proposal, decision, files changed, upstream draft path.
2. Set `processed: true` in every retro you read.
3. Report in a few lines: proposals applied and rejected, files changed, upstream drafts written, and how many groups remain. Remind the human to commit the changes and to send or file the upstream drafts.
