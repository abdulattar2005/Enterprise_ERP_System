# Changelog

All notable changes are listed here. Format based on Keep a Changelog; versions follow Semantic Versioning.

## [1.0.0] - 2026-10-27

### Added
- Multi-module Maven project: core, hr, inventory, accounting, sales, reporting, api.
- HR: employees, attendance, payroll with progressive tax bands, performance reviews.
- Inventory: products, suppliers, warehouses, purchase orders, low-stock report, optimistic-locked stock changes.
- Accounting: chart of accounts, double-entry journal with balance validation, trial balance.
- Sales: customers and sales orders with confirm and cancel flow.
- Reporting: dashboard, sales summary, low-stock, trial balance.
- Event-driven postings: sales, purchases and payroll create ledger entries automatically.
- JWT authentication, role-based access (ADMIN, MANAGER, USER), audit log, correlation ids.
- Caffeine cache for product reads.
- OpenAPI / Swagger UI, Actuator health and Prometheus metrics.
- Dockerfile, docker-compose, Jenkinsfile, GitHub Actions workflow.
- Kubernetes manifests, Terraform reference, Prometheus alert rules.
- Unit and end-to-end tests; full project documentation.

### Known limitations
- See "Known limitations" in README.md.
