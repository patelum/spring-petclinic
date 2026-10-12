---
name: sdlc-security
description: Security review of a finished change in a Java web monolith, appended to specs/<id>/review.md. Use for the security stage of the ai-sdlc feature track, in a separate context from the implementer.
---

# Security review

**Inputs:** `specs/<id>/spec.md`, `status.md` for `base_commit`, the diff.
**Output:** a `## Security` section appended to `specs/<id>/review.md`.

## Checklist (apply what the diff touches)

- **Injection:** SQL/JPQL/HQL built by string concatenation; native queries with user input; LDAP, XPath, OS command, expression language (SpEL) injection.
- **Access control:** new endpoints or methods without the authorisation the module uses elsewhere (`@PreAuthorize`, `@Secured`, security config, manual checks); object-level checks (can user A read user B's invoice?).
- **Input handling:** missing validation on new request DTOs (`@Valid`, Bean Validation); mass assignment from request bodies onto entities; file uploads (type, size, path traversal).
- **Data exposure:** entities serialised directly to responses; secrets, tokens or personal data in logs, exceptions or responses.
- **Deserialisation and XML:** Java deserialisation of untrusted data; XML parsers without XXE protection.
- **Crypto and secrets:** hard-coded credentials or keys; weak hashing for passwords; `Random` where `SecureRandom` is needed.
- **Web:** CSRF on new state-changing endpoints where the app relies on cookies; open redirects; CORS changes.
- **Dependencies:** any new library or version change, checked for known vulnerabilities where tooling exists.

## Writing findings

Each finding: `severity (high | medium | low)`, `path:line`, the issue, a concrete fix. High = exploitable or exposes data; medium = weakens a control; low = hardening.

End the section with: `Security verdict: no issues | issues found (h/m/l counts)`.

Report: counts by severity.
