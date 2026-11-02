# 8. Analysis Questions

Answers to the five questions in the task, using this project as the worked example.

## 1. What architectural patterns are most effective for enterprise systems?

There is no single best pattern; the right one depends on team size, load and how tightly the business
processes are connected. The patterns that pay off most often:

- **Layered architecture** (controller, service, repository) keeps HTTP, business rules and storage separate,
  so each can change and be tested alone. Used in every module here.
- **Modular monolith** gives clear boundaries with one deployment. It fits when business actions span
  modules and must be atomic, as a sale does here (stock, invoice, ledger). It is cheaper to run than
  microservices and can be split later.
- **Event-driven integration** decouples modules. Accounting reacts to events instead of being called by Sales,
  HR and Inventory. In this project the events are in-process; a broker such as Kafka is the next step.
- **Domain-driven design ideas**: each module owns its own concepts (an HR employee is not a Sales customer)
  and exposes a small API to the others.
- **Microservices** become worthwhile when teams need to deploy independently or parts must scale separately,
  but they bring distributed transactions, network failures and heavy operations. Start modular, split when
  there is a real reason.

## 2. How can modular design improve maintainability?

- **Enforced boundaries.** Separate Maven modules mean Accounting cannot import Sales classes: the build fails.
  The rules are checked by the compiler rather than by memory and code review.
- **Smaller blast radius.** A change inside HR cannot silently break Inventory, because HR is not visible to it.
- **Clear ownership and easier onboarding.** A new developer can understand one module in isolation.
- **Independent testing.** Module unit tests run without starting the whole app.
- **Replaceability.** Because modules talk through services and events, a module can later become its own service.
- **Cost:** more files and some up-front thought about where code belongs.

## 3. What security considerations are unique to enterprise applications?

- **Many roles and least privilege.** Different people may see different data (salaries are ADMIN or MANAGER only).
  Here every endpoint carries a `@PreAuthorize` rule.
- **Auditability.** Enterprises need to prove who changed what and when. The append-only audit log is written in its
  own transaction so it survives rollbacks.
- **Financial and personal data.** Money uses exact decimals, ledgers are append-only, and personal data brings
  privacy laws (see COMPLIANCE.md).
- **Integrity under concurrency.** Optimistic locking and transactions prevent lost updates and half-done actions.
- **Identity integration.** Real companies use single sign-on (OIDC, SAML). This project uses a demo user store and says so.
- **Secrets and supply chain.** Secrets come from the environment; dependency scanning runs in the pipeline.
- **Compliance.** Standards such as ISO 27001, GDPR and SOC 2 require processes as well as code.

## 4. How does caching strategy affect system performance?

Caching trades freshness and simplicity for speed.

- **Benefit.** Product lookups are read far more than written. A Caffeine cache answers from memory
  instead of the database, which lowers latency and database load.
- **Invalidation is the hard part.** Here every stock change evicts the product's entry, and entries expire after
  5 minutes as a safety net.
- **Known trade-offs in this project.** The cache is per instance, so with several pods another pod may serve a
  slightly old stock figure for up to 5 minutes. Eviction also happens just before commit, so a rare race can
  re-cache an old value. For stock-critical decisions the confirm-order path always re-reads the row inside the
  transaction and relies on the version check, not on the cache.
- **Scaling up.** A two-level design (local cache in front of a shared Redis) keeps speed while sharing state.
  Hit and miss ratios should be monitored; a cache that rarely hits is just overhead.
- **What not to cache.** Money balances and anything where being stale causes a wrong decision.

## 5. What monitoring is essential for production systems?

Cover the "golden signals" plus business health:

- **Availability**: health probes and an alert when an instance is down.
- **Latency**: p95 / p99 response time (alert above 500 ms in `alerts.yml`).
- **Traffic**: requests per second.
- **Errors**: 5xx ratio (alert above 2%).
- **Saturation**: CPU, memory, JVM heap, database connection pool (alert when requests wait for a connection).
- **Logs with correlation ids** to follow one request across the system.
- **Business checks**: for an ERP, the most valuable one is that the trial balance stays balanced; the dashboard
  exposes `ledgerBalanced` and it can be polled and alerted on.
- **Audit review** for suspicious behaviour, and **dependency scanning** for known vulnerabilities.
- Good alerts are few, actionable and tied to a runbook (see the operations manual).
