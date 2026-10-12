---
name: sdlc-context
description: Builds or refreshes .ai-sdlc/context/ for this Java monolith - a system overview and one file per module, with every claim marked verified or inferred. Use when setting up ai-sdlc in a repo or when the context is stale.
---

# Set up project context

Goal: give future agents a small, accurate map of this codebase so each task loads only the modules it touches. Accuracy beats coverage. A short file with verified facts is worth more than a long one with guesses.

## Output

- `.ai-sdlc/context/index.md` from `.ai-sdlc/templates/context-index.md`
- `.ai-sdlc/context/modules/<module>.md` from `.ai-sdlc/templates/context-module.md`, one per module
- `modules:` in `.ai-sdlc/config.yaml`, if logical modules had to be inferred
- Corrections to `build:` in `.ai-sdlc/config.yaml` if the commands there are wrong

## Procedure

1. **Build facts.** Read the root `pom.xml` / `settings.gradle*` and every module's build file. Record: Java version, framework (Spring Boot version, Jakarta EE, plain), packaging (jar, war), modules, notable dependencies (persistence, messaging, web, testing). Check the wrapper exists and the `build:` commands in config match the build tool.
2. **Modules.** For a multi-module build, each build module is a module. For a single-module build, propose logical modules from top-level packages under the base package (e.g. `com.acme.billing`, `com.acme.orders`); mark this `[inferred]` and record them under `modules:` in config.
3. **Per module, find:**
   - Entry points: classes with `@RestController`, `@Controller`, `@Scheduled`, `@KafkaListener`, `@JmsListener`, `@RabbitListener`, `@EventListener`, servlets/filters, `main` methods, EJBs.
   - Data: `@Entity` classes and their tables; migrations under Flyway (`db/migration`) or Liquibase.
   - Dependencies: packages from other modules this module imports (`grep -r "^import com.acme.<other>"`), in and out.
   - Tests: test source roots, frameworks in use, rough count of test classes, integration test setup.
   - Gotchas: anything a newcomer would trip on (transaction boundaries, static state, generated code, legacy packages, feature flags).
4. **Hotspots (optional, quick).** `git log --since=1.year --name-only --pretty=format: | sort | uniq -c | sort -rn | head -20` for frequently changed files; note them in the index.
5. **Write the files.** Use the templates. Keep each module file under ~120 lines. Mark every claim:
   - `[verified: path:line]` or `[verified: path]` when you read it in the code
   - `[inferred]` when you concluded it without direct evidence
6. **Open questions.** Every `[inferred]` claim that matters also goes in the file's **Open questions** section, phrased so a human can answer it quickly.
7. **Report** to the orchestrator: modules found, files written, count of open questions, and any build-command corrections.

## Rules

- Do not read every file. Use search (`grep`, `find`) to locate, then read only what you need to confirm.
- Do not describe code you have not looked at.
- Do not copy large code blocks into context files. Name classes and paths instead.
- Generated, vendored and build output directories (`target/`, `build/`, `generated-sources/`) are out of scope.
