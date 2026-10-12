# Approval card

Every gate uses this card, so the human always knows where to look. It must fit on one screen and be answerable without opening a file. The full artifact is one path away for anyone who wants more.

```text
GATE <n>/<total> · <Stage> · <id> <title>
What:     <one line: what this stage produced or proposes>
Why:      <one line: the requirement or problem it answers>
Touches:  <modules and rough size> · <"no new module dependency" or the new dependency>
Evidence: <checks run and results, or what the artifact was based on>
Decide:   <the judgement calls that belong to a human, one per line; or "nothing beyond approval">
Full:     specs/<id>/<artifact>.md

Approve? [y / n]
```

## Writing the card

- **What** and **Why** are one line each. No adjectives, no restating the title.
- **Touches** names modules from `.ai-sdlc/context/index.md`. On the final gate, give files changed and lines added/removed.
- **Evidence** is facts: `module build green · 14 tests added · review: 0 must-fix, 2 consider`. On spec and design gates, say what was read: `request.md, context/modules/billing.md, InvoiceService.java`.
- **Decide** is the most important line. List only real choices a human should own: a library, a trade-off, an assumption about behaviour, a scope cut. If the agent made an assumption to fill a gap in the request, it goes here. Never pad this line.
- Keep the card under 12 lines. If more is needed, the artifact is too big; say so on the Decide line.

## Per-gate notes

| Gate | Artifact | Decide line usually covers |
| --- | --- | --- |
| spec | `spec.md` | Assumptions about behaviour, edge cases left out of scope |
| design | `design.md` | Approach, libraries, new dependencies, data changes |
| plan | `plan.md` | Step order, risky steps, anything that touches shared code |
| final | `review.md` + diff | `consider` findings, untested paths, follow-up work |

## Escalation card

Escalations use the same shape with a different header:

```text
NEEDS YOU · <reason> · <id> <title>
What:     <what happened>
Tried:    <attempts so far, if any>
Options:  1 <option>   2 <option>   3 <option>
```

Escalations take a number, not y/n.
