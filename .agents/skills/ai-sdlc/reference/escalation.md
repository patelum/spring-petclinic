# Escalation rules

Outside the configured gates, the orchestrator stops and asks only in these cases. Each uses the escalation card in `approval-card.md`, sets `state: blocked` with a `blocked_reason`, and waits for a number.

| Trigger | Detect by | Typical options |
| --- | --- | --- |
| Retry budget exhausted | `retries` for a step reaches `orchestrator.retry_budget` | 1 try a different approach · 2 revise the plan · 3 stop here, I'll look |
| New cross-module dependency | Design or diff adds an import from another module's package that `context/index.md` does not list as an allowed dependency | 1 allow it · 2 redesign to avoid it |
| New library | Diff changes `<dependencies>` in any `pom.xml` or `dependencies {}` in `build.gradle*` | 1 approve library · 2 find an alternative already in the build |
| Schema migration | New or changed file under Flyway / Liquibase paths | 1 approve migration · 2 redesign without schema change |
| Protected path | A step needs to edit a path in `protect:` | 1 add a new file instead · 2 I'll make this change myself |
| High-severity finding | Security or review reports `severity: high` that the implementer could not resolve | 1 fix with guidance (type it) · 2 accept risk and record it · 3 stop |
| Track mismatch | Work reveals the track is wrong (defect needing new behaviour, refactor changing behaviour) | 1 switch track · 2 narrow scope to fit |
| Plan is wrong | Implementer reports a step cannot be done as planned | 1 revise plan (back to plan gate) · 2 skip step |

Only the triggers listed under `escalate_on:` in `config.yaml` apply, plus retry budget, track mismatch and plan-is-wrong, which always apply.

After the human answers, log it, clear `blocked_reason`, set `state: in_progress` and continue. An escalation that leads to a changed spec, design or plan sends the ticket back to that stage and its gate.
