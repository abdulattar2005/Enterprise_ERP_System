# Compliance Notes

This project is **not certified** against any standard. This document shows how the design supports the
ideas behind ISO 27001, GDPR and SOC 2, and states honestly what is still missing. Certification needs
organisational processes (policies, training, audits) that software alone cannot provide.

## ISO 27001 (information security management)

| Control idea | Support in the project | Gap |
|---|---|---|
| Access control | Role-based access on every endpoint | No periodic access review process |
| Cryptography | BCrypt password hashes, signed tokens, encrypted DB storage in the Terraform reference | TLS must be configured at deployment |
| Logging and monitoring | Audit log, correlation ids, Prometheus metrics and alerts | No central log shipping configured |
| Secure development | Tests, code review checklist, dependency scanning in CI | No formal threat model |
| Backup | Daily automated backups in the Terraform reference (7 days) | Restore tests not scheduled |
| Supplier management | Supplier records exist as a business feature | Not an information-security supplier review |

## GDPR (personal data)

Personal data held: employee names, emails, salaries; customer names, emails, phone numbers; usernames in the audit log.

| Principle | Support | Gap |
|---|---|---|
| Lawfulness and purpose | Data is limited to what each business function needs | Legal basis must be documented by the operator |
| Data minimisation | Only required fields are stored | none known |
| Integrity and confidentiality | Roles, validation, audit trail | Field-level encryption not implemented |
| Accountability | Audit log of changes | No data-protection impact assessment |
| Right of access | Data can be read through the API by authorised staff | No self-service export |
| Right to erasure | Employees are soft-deleted (status TERMINATED) to keep payroll and accounting history | No anonymisation routine yet |
| Retention | Not automated | Define and implement retention rules |

## SOC 2 (trust service criteria)

| Criterion | Support | Gap |
|---|---|---|
| Security | See SECURITY.md | See "Before going to production" |
| Availability | Health probes, 2 replicas, autoscaler, multi-AZ database in the reference files | No tested failover or SLA measurement |
| Processing integrity | Double-entry validation, transactions, optimistic locking | none known |
| Confidentiality | Role separation, secrets from environment | Secret manager not wired in |
| Privacy | See GDPR table | See GDPR gaps |

## Service level targets

The task description mentions a 99.95% availability target and multi-region deployment. These are
**design targets for a full production rollout, not measured results**. The single-region reference
infrastructure here would need load testing, failover tests and a second region to support them.
