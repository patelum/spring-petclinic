Title: [lesson] sdlc-design: bound entity references from a shared cache need nested properties disallowed
Labels: lesson, sdlc-design, security

**Kit version:** b53deac
**Track / stage:** feature / design (found at security)
**Seen in:** 1 ticket in one repository

## Problem
A form field bound to an entity resolved by a formatter from a shared, cached collection also accepted nested property paths on that field. A crafted request could overwrite fields of the shared cached object. The design did not name the risk; the security review found it.

## Evidence
A new form field referenced an existing shared entity by identifier. The controller's data binder did not restrict nested properties of that field, so posted values for the entity's own attributes were bound onto the cached instance. It was rated medium and fixed during review with a disallowed-fields rule and two tests.

## Proposed change
- `skills/sdlc-design/SKILL.md`: when a design binds an entity reference that comes from a shared or cached source, require the design to state that nested properties of the field are disallowed in the binder, and to list the test that proves it.
- `standards/security.md` (shipped default): add "A form field that binds an entity from a shared or cached source must disallow its nested properties in the binder, with a test."

## Regression scenario
Ticket: "Let users pick a related record from a dropdown on a form", where the record list is cached. A correct run designs the binder restriction and tests that posting nested attribute values leaves the cached record unchanged. It must not rely on the security review to find this.
