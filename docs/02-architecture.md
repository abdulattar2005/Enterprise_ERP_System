# 2. Architecture

## 2.1 Style: modular monolith with clean layering

One deployable application, internally split into Maven modules with enforced dependency directions.
This was chosen over microservices because the modules share transactions (a sale must change stock and the
ledger atomically) and the team and load do not justify the operational cost of distributed systems.
The module boundaries are kept clean so a module could later be extracted into its own service.

### Module dependencies

| Module | Depends on |
|---|---|
| erp-core | nothing in the project |
| erp-hr | core |
| erp-inventory | core |
| erp-accounting | core |
| erp-sales | core, inventory |
| erp-reporting | core, hr, inventory, sales, accounting (read-only) |
| erp-api | all modules; it is the only runnable artifact |

Dependencies only point downward, so there are no cycles. Maven refuses to build a cycle.

### Layers inside a module

```
web (controllers)  ->  service (rules + transactions)  ->  repository (Spring Data JPA)  ->  database
        |                    |
   dto records          domain entities
```

## 2.2 Cross-module communication: domain events

Accounting must react to sales, purchases and payroll without depending on those modules.

```
SalesOrderService.confirm()   --publishes-->  SalesInvoicedEvent    --+
PurchaseOrderService.receive() --publishes--> PurchaseReceivedEvent --+--> LedgerEventListener (Accounting)
PayrollService.run()          --publishes-->  PayrollProcessedEvent --+       |
                                                                              v
                                                                     JournalService.postSystemEntry()
```

The event classes live in `erp-core`, so publisher and listener share only a tiny contract. Spring delivers them
synchronously inside the publisher's transaction. That gives one important guarantee: **a business action and
its ledger entry commit together or not at all.**

Automatic postings:

| Event | Debit | Credit |
|---|---|---|
| Sales invoiced | 1100 Accounts Receivable | 4000 Sales Revenue |
| Purchase received | 1200 Inventory | 2000 Accounts Payable |
| Payroll processed | 5100 Salaries Expense (gross) | 1000 Cash (net) and 2100 Taxes Payable (tax) |

## 2.3 Data model

```
employees 1---* attendance          suppliers 1---* products *---1 warehouses
employees 1---* payroll             suppliers 1---* purchase_orders 1---* purchase_order_lines *---1 products
employees 1---* performance_reviews customers 1---* sales_orders 1---* sales_order_lines (product_id, no FK)
accounts  1---* journal_lines *---1 journal_entries
audit_logs (stand-alone, append-only)
```

Notes:
- Every business table has `id`, `version` (optimistic lock), `created_at`, `updated_at` from `BaseEntity`.
- `sales_order_lines` stores `product_id`, product name and unit price as a snapshot, so old orders stay correct
  after a product is renamed or repriced, and Sales does not depend on Inventory's tables.
- Money columns are `NUMERIC(14,2)` mapped to `BigDecimal`.
- Unique constraints: employee email, product SKU, supplier name, warehouse code, account code,
  (employee, work date) in attendance, (employee, period) in payroll.

## 2.4 Algorithms and data structures

**Double-entry validation** (`JournalService.validateBalanced`): one pass over the lines; keeps two running
`BigDecimal` totals; rejects negative amounts, lines with both or neither side, fewer than two lines, and
unequal totals. Totals are compared with `compareTo`, not `equals`, so 100.0 equals 100.00. O(n).

**Trial balance**: one grouped SQL query (`sum(debit)`, `sum(credit)` per account) mapped straight into records,
instead of loading every line into memory.

**Marginal-slab tax** (`PayrollService.calculateTax`): an ordered list of bands (`Slab(upTo, rate)`). For each band,
tax the part of the salary between the previous limit and this band's limit, then move the lower bound up.
Example for a monthly gross of 100000 with bands 25000 at 5%, up to 75000 at 10%, above at 20%:
`25000*5% + 50000*10% + 25000*20% = 1250 + 5000 + 5000 = 11250.00`.

**Optimistic locking for stock** (`ProductService.adjustStock`): read product (with version), compute the new
quantity, reject if negative, `saveAndFlush`. If another transaction changed the row first, the update's
`WHERE version = ?` matches nothing and the database layer raises an optimistic-lock failure, which the API
maps to HTTP 409. No row locks are held while the user thinks, so reads stay fast.

**Caching** (`@Cacheable` / `@CacheEvict`): product lookups are cached by id in Caffeine (max 5000 entries,
5 minute expiry). Every stock change and product update evicts that id. Trade-off: eviction happens before the
transaction commits, so in a rare race another thread could re-cache the old value until it expires.

**Paging**: list endpoints take `page`, `size`, `sort` through Spring's `Pageable`.

## 2.5 Security architecture

```
client --Bearer JWT--> Spring Security filter chain
                         1. validate signature + expiry
                         2. map "roles" claim -> ROLE_* authorities
                         3. @PreAuthorize on controller method
                       --> controller --> service --> repository
```

Passwords are checked once at login against BCrypt hashes. After that the API is stateless: each request carries a
signed token. See `SECURITY.md`.

## 2.6 Observability

- **Logs**: every line includes `[correlationId]`. The id comes from the `X-Correlation-Id` request header or is
  generated, and is returned in the response header.
- **Metrics**: Actuator + Micrometer expose `/actuator/prometheus` (HTTP latency and counts, JVM, connection pool).
- **Health**: `/actuator/health/liveness` and `/readiness` for Kubernetes probes.
- **Alerts**: `monitoring/alerts.yml` (instance down, 5xx rate above 2%, p95 above 500 ms, DB pool waiting).

## 2.7 Deployment architecture (reference)

```
Internet -> Load balancer / Ingress (TLS) -> Service -> erp-system pods (2..6, autoscaled)
                                                             |
                                                  PostgreSQL (RDS, multi-AZ, encrypted)
Prometheus scrapes pods -> Grafana dashboards, alert rules
```

## 2.8 Design decisions and trade-offs

| Decision | Reason | Trade-off |
|---|---|---|
| Modular monolith | Shared transactions, simple operations | One deploy unit; scaling is all-or-nothing |
| Synchronous in-process events | Atomic business action + ledger entry | Not a message broker; no replay or cross-service delivery |
| Optimistic locking | High read concurrency, no lock waits | Clients may need to retry on 409 |
| DTO records at the API boundary | Hide internals, avoid lazy-loading problems | More small classes |
| Soft delete for employees | Keep payroll and audit history | Queries must consider status |
| Caffeine (local) cache | No extra infrastructure | Not shared between instances |
| H2 for dev, PostgreSQL for prod | Zero-setup local run | Small dialect differences; mitigated by H2 PostgreSQL mode and containerised Postgres option |

## 2.9 Scale-out roadmap (not implemented)

These come from the task's enterprise description and are the logical next steps:

1. Flyway migrations and `ddl-auto=validate`.
2. Redis as a shared L2 cache; keep Caffeine as L1.
3. Kafka (or the outbox pattern) to publish events to other systems and to extract modules into services.
4. PostgreSQL read replicas for reporting queries.
5. Elasticsearch for product and customer search.
6. OIDC identity provider replacing the demo user store.
7. Second region and tested failover for a 99.95% availability target.
