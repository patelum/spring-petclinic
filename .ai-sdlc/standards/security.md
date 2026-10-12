# Security standards

<!-- Team-owned after install: edit freely. Upgrades never overwrite this file.
     The security review applies the checklist in the sdlc-security skill plus everything here. -->

## Baseline

- New endpoints and handlers use the same authorisation mechanism as their neighbours; object-level access is checked.
- Request binding never reaches entity identifiers or nested objects the user should not control (disallowed fields, DTOs).
- No secrets, tokens or personal data in code, logs, exceptions or responses.
- New or upgraded libraries are checked for known vulnerabilities where tooling exists.

## Severity policy

- `high`: exploitable, or exposes data. Blocks the final gate until fixed or explicitly accepted by a human.
- `medium`: weakens a control. Fixed in the ticket unless a human defers it.
- `low`: hardening. Listed for the human.

## Team additions

<!-- Add rules specific to this codebase below. -->

- When a form field binds an entity from a shared or cached source (for example a formatter-resolved Vet), the controller's @InitBinder must disallow nested properties of that field (for example setDisallowedFields("vet.*")), with a test that posting them leaves the entity unchanged. (source: PET-12)
