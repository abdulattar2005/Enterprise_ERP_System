# Contributing

## Getting started

1. Fork and clone the repository.
2. Build everything: `mvn clean verify`.
3. Run the app: `mvn -pl erp-api spring-boot:run` (profile `dev`, in-memory database).

## Branching and commits

- Branch from `main`: `feature/<short-name>`, `fix/<short-name>`, `docs/<short-name>`.
- Write commit messages in the imperative: "Add low-stock endpoint", not "Added" or "Adding".
- Keep each pull request to one purpose. Link the issue if there is one.

## Code guidelines

- Module rules: `erp-core` depends on nothing in the project. Business modules depend on `erp-core`.
  `erp-sales` may call `erp-inventory`. `erp-accounting` must not depend on other business modules;
  it reacts to events from `erp-core`. Do not add a dependency that breaks this.
- Layers: controller (HTTP only) -> service (rules, transactions) -> repository. No business logic in controllers.
- Never return JPA entities with lazy relations from a controller. Return DTO records.
- Money is always `BigDecimal`, never `double`.
- Validate input with Bean Validation annotations on request records.
- Throw `ResourceNotFoundException` (404) or `BusinessRuleException` (422); do not return error strings.
- Every state-changing service method writes an audit entry.
- Protect every new endpoint with `@PreAuthorize`. An endpoint with no rule is open to any logged-in user.

## Tests

- New business rules need a unit test. New endpoints need an end-to-end test case when they cross modules.
- Use the injected `Clock` instead of `LocalDate.now()` so tests can control time.
- `mvn verify` must pass before you open a pull request.

## Pull request checklist

- [ ] Tests added or updated and passing
- [ ] No secrets or passwords in the diff
- [ ] Documentation updated (`docs/`, README, CHANGELOG)
- [ ] New endpoints have role rules and are listed in `docs/04-api-reference.md`
