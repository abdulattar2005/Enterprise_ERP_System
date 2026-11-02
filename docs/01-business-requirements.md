# 1. Business Requirements

## 1.1 Purpose

A mid-size company runs HR, stock, sales and accounting in separate spreadsheets or tools. Numbers disagree,
work is repeated, and nobody can see who changed what. This ERP puts the four areas on one data model so
that one business event updates everything it should, and reports come from one source of truth.

## 1.2 Users and roles

| Role | Typical person | Can do |
|---|---|---|
| ADMIN | System owner, finance lead | Everything: payroll, accounts, audit log, terminate employees |
| MANAGER | Department manager | Manage employees, attendance, reviews, products, purchase orders, customers, sales orders, reports |
| USER | Staff member | Read the product catalogue and low-stock list |

## 1.3 Functional requirements

### HR
- FR-HR-1 Create, read, update employees; list with paging and department filter.
- FR-HR-2 Employees cannot share an email address.
- FR-HR-3 Terminate an employee without deleting history (ADMIN only).
- FR-HR-4 Record daily check-in and check-out; at most one record per employee per day; show hours worked.
- FR-HR-5 Run payroll for a month: gross pay, tax, net pay per active employee; running it twice must not duplicate.
- FR-HR-6 Record performance reviews with a 1 to 5 rating.

### Inventory
- FR-INV-1 Manage products with SKU (unique), price, stock quantity, reorder level, warehouse and supplier.
- FR-INV-2 Stock can only change through audited adjustments; it can never go below zero.
- FR-INV-3 Report products at or below their reorder level.
- FR-INV-4 Manage suppliers and warehouses.
- FR-INV-5 Create purchase orders with several lines; receiving one increases stock for every line, once only.

### Accounting
- FR-ACC-1 Maintain a chart of accounts (asset, liability, equity, revenue, expense).
- FR-ACC-2 Post journal entries; every entry must balance (debits equal credits).
- FR-ACC-3 Produce a trial balance that shows whether the ledger is balanced.
- FR-ACC-4 Create entries automatically for confirmed sales, received purchases and processed payroll.

### Sales
- FR-SAL-1 Manage customers.
- FR-SAL-2 Create a sales order priced from current product prices (draft).
- FR-SAL-3 Confirm an order: deduct stock, invoice, post to ledger, all or nothing.
- FR-SAL-4 Cancel only draft orders.

### Reporting
- FR-REP-1 Dashboard with headcount, product count, low-stock count, inventory value, order counts, revenue, ledger status.
- FR-REP-2 Sales summary including average confirmed order value.

### Platform
- FR-PLT-1 Log in and receive a time-limited token.
- FR-PLT-2 Every state change is recorded in an audit log readable by ADMIN.
- FR-PLT-3 Clear, uniform error responses.

## 1.4 Non-functional requirements

| ID | Requirement | How it is addressed |
|---|---|---|
| NFR-1 Security | Authenticated access, least privilege, no secrets in code | JWT, roles, environment-based secrets |
| NFR-2 Integrity | No lost updates, no half-finished business actions | Transactions, optimistic locking |
| NFR-3 Traceability | Know who did what and follow a request through logs | Audit log, correlation id |
| NFR-4 Performance | Fast catalogue reads | Caffeine cache, paging, indexes on unique columns |
| NFR-5 Operability | Health checks, metrics, alerts | Actuator, Prometheus rules |
| NFR-6 Portability | Runs the same locally and in the cloud | Docker, 12-factor configuration |
| NFR-7 Maintainability | Clear boundaries, tests | Maven modules, layered code, test suite |

## 1.5 Assumptions and out of scope

- Single currency, single company, no tax law beyond demo bands.
- No front-end is included; the REST API and Swagger UI are the interface.
- Not included: multi-currency, fixed assets, leave management, returns/credit notes, e-invoicing.

## 1.6 Data used

Business data as listed in the task: employee records, inventory items, financial transactions, sales data.
Sample data is created by `DemoDataLoader` in the `dev` profile (3 employees, 4 products, 1 supplier,
1 warehouse, 1 customer).
