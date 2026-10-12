# petclinic

<!-- ai-sdlc:begin (managed by Autonomous-SDLC-Java; edit outside this block) -->
## AI SDLC

This repository uses **ai-sdlc**, a gated, agentic delivery process for this Java monolith.

**Start here:** run the `ai-sdlc` skill (`/ai-sdlc`, or say "start ai-sdlc"). It asks what you want to do and runs the right track. Do not start large changes outside it.

**Where things are**

- `.ai-sdlc/config.yaml` — build commands, gates, protected paths. Use these commands; do not invent others.
- `.ai-sdlc/context/index.md` — system overview and module map. Read it first, then only the `modules/*.md` files the task touches.
- `.ai-sdlc/workflows/` — the three tracks: `defect.md`, `feature.md`, `refactor.md`.
- `.ai-sdlc/standards/` — engineering, testing and security standards. `.ai-sdlc/lessons.md` — facts and corrections learned in this repo. Apply both.
- `specs/<ticket>/` — artifacts and `status.md` for each change in progress.

**Rules that always apply**

1. Never `git commit`, `git push`, merge, or change branches unless the human asks in this session.
2. Never edit paths listed under `protect:` in `config.yaml`. Add a new migration instead of changing an applied one.
3. Use the scoped `build.verify_module` command while working; run `build.verify` before calling any change done.
4. Claims about this codebase must be checkable: cite `path:line`, or say it is an assumption.
5. A new cross-module dependency, new library or schema change needs explicit human approval.
6. Keep changes inside the plan. If the plan is wrong, stop and say so rather than improvising.
<!-- ai-sdlc:end -->
