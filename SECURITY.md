# Security Policy

## Reporting a vulnerability

Do not open a public issue for a security problem. Email the maintainer (see the GitHub profile) with a
description, steps to reproduce and the affected version. You will get an acknowledgement and a fix plan.

## Supported versions

Only the latest release receives security fixes.

## Security controls in this project

| Area | Control | Where |
|---|---|---|
| Authentication | Login with username and password; passwords stored as BCrypt hashes; short-lived signed JWT (HS256, default 60 minutes) | `erp-core/.../security` |
| Authorization | Roles ADMIN, MANAGER, USER enforced with `@PreAuthorize` on every controller | each `web` package |
| Stateless API | No server session, CSRF disabled because no cookies are used | `SecurityConfig` |
| Input validation | Bean Validation on all request bodies; unreadable or invalid input returns 400 | `dto` records, `GlobalExceptionHandler` |
| Injection | Spring Data and JPQL with bound parameters only; no string-built queries | repositories |
| Error handling | Uniform error body; no stack traces or internal messages sent to clients | `GlobalExceptionHandler`, `application.yml` |
| Audit trail | Who did what, written in a separate transaction so it survives rollbacks | `AuditService` |
| Data integrity | Optimistic locking, unique constraints, database transactions | entities, services |
| Secrets | No secrets in source for production; `prod` profile requires environment variables | `application-prod.yml` |
| Container | Multi-stage build, non-root user, dropped Linux capabilities in Kubernetes | `Dockerfile`, `kubernetes/` |
| Dependencies | OWASP dependency-check stage in the Jenkins pipeline | `ci-cd/Jenkinsfile` |
| Transport | TLS must be terminated at the ingress / load balancer (see deployment guide) | deployment |
| Database | Terraform reference enables storage encryption, backups and private subnets | `infrastructure/` |

## Before going to production

These are known gaps. Close them before real use.

1. Replace the in-memory demo users with a database or OIDC identity provider.
2. Change every default password and generate a long random `ERP_JWT_SECRET`.
3. Add login rate limiting and lockout (for example at the API gateway).
4. Terminate TLS in front of the app and restrict `/actuator/prometheus` to the cluster network.
5. Add Flyway migrations and switch Hibernate to `validate`.
6. Add refresh tokens or token revocation if you need logout to invalidate tokens.
7. Rotate secrets on a schedule and store them in a secret manager.
