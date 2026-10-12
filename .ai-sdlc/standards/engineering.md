# Engineering standards

<!-- Team-owned after install: edit freely. Upgrades never overwrite this file.
     The "Improve the process" option may propose changes here; each one needs your y. -->

Agents apply these when designing, implementing and reviewing. A rule here overrides a general habit; it never overrides an explicit instruction in the approved spec, design or plan.

## Design and structure

- Follow the module's existing patterns (layering, naming, error handling, transactions) before inventing new ones.
- Respect module boundaries recorded in `.ai-sdlc/context/index.md`. A new cross-module dependency needs approval.
- No new library without approval. Prefer what the build already has.
- Schema changes are additive and backwards-compatible by default (nullable columns, new tables). Never edit an applied migration.

## Code

- Keep changes inside the plan. No drive-by refactors in a defect or feature ticket.
- Errors: never swallow exceptions; log once, at the boundary that handles them.
- User-facing text goes through the module's message/i18n mechanism, never hard-coded.
- Never show users internal detail: raw exception or conversion messages, class names, stack traces, SQL.
- No dead code, debug output or commented-out code in a finished change.

## Team additions

<!-- Add rules specific to this codebase below. Approved improvement proposals are appended here with their source. -->
