# 3. Module Specifications

Each section lists entities, business rules, and the failure responses a client can expect.
Errors always use the body `{timestamp, status, error, message, details[]}`.

| Status | Meaning here |
|---|---|
| 400 | Validation failed or malformed JSON |
| 401 | Missing, expired or invalid token / wrong login |
| 403 | Logged in but role not allowed |
| 404 | Record not found |
| 409 | Concurrent change or uniqueness conflict; reload and retry |
| 422 | Business rule violated |

## 3.1 erp-core

| Piece | Behaviour |
|---|---|
| BaseEntity | id, version, createdAt, updatedAt |
| AuditService | Writes `audit_logs` in a new transaction; user is taken from the security context, or `system` |
| Security | `/api/auth/login` returns `{accessToken, tokenType, expiresInSeconds}`; `/api/auth/me` shows the caller |
| GlobalExceptionHandler | Maps exceptions to the statuses above; unexpected errors return a generic 500 and are logged |
| CorrelationIdFilter | Adds `X-Correlation-Id` to logs and responses |
| Events | `SalesInvoicedEvent`, `PurchaseReceivedEvent`, `PayrollProcessedEvent` |

## 3.2 erp-hr

**Employee**: first name, last name, unique email (stored lower-case), department, job title, monthly salary,
hire date (not in the future), status ACTIVE / ON_LEAVE / TERMINATED.

Rules
- Duplicate email on create or update: 422.
- Terminate sets status TERMINATED, keeps the record (ADMIN only).
- Only ACTIVE employees can check in.

**Attendance**: one row per employee per day. Check-in twice: 422. Check-out without check-in or twice: 422.
Hours worked = minutes between check-in and check-out divided by 60, two decimals.

**Payroll**: period `yyyy-MM`; anything else: 422.
- Runs for ACTIVE employees only; employees already paid for the period are skipped, so repeated runs are safe.
- Gross = monthly salary. Tax = marginal slabs (see architecture 2.4). Net = gross minus tax.
- When at least one new row is created, a `PayrollProcessedEvent` is published and a ledger entry is posted.
- ADMIN only.

**Performance review**: rating 1 to 5 (validated), free-text comments, reviewer = logged-in user.

## 3.3 erp-inventory

**Product**: unique SKU, name, category, unit price, stock quantity, reorder level, active flag, optional
warehouse and supplier. `lowStock` is true when stock is at or below the reorder level.

Rules
- Initial stock is set only at creation. Later changes go through `adjust-stock` (positive adds, negative removes).
- Adjustment of zero: 422. Result below zero: 422 (`Insufficient stock for SKU: available X, requested Y`).
- Concurrent adjustment of the same product: the second gets 409.
- Every adjustment is audited with its reason.
- Reads of a single product are cached and the cache entry is cleared on each change.

**Supplier**: unique name. **Warehouse**: unique code (stored upper-case).

**Purchase order**: supplier plus one or more lines (product, quantity at least 1, unit cost).
Status DRAFT then RECEIVED. Receiving
1. adds each line's quantity to the product's stock,
2. sets the status to RECEIVED,
3. publishes `PurchaseReceivedEvent` (ledger: inventory up, payables up).
Receiving a non-DRAFT order: 422.

## 3.4 erp-accounting

**Account**: unique code, name, type, active flag. Default chart created at start-up (idempotent):

| Code | Name | Type |
|---|---|---|
| 1000 | Cash | ASSET |
| 1100 | Accounts Receivable | ASSET |
| 1200 | Inventory | ASSET |
| 2000 | Accounts Payable | LIABILITY |
| 2100 | Taxes Payable | LIABILITY |
| 3000 | Owner's Equity | EQUITY |
| 4000 | Sales Revenue | REVENUE |
| 5000 | Cost of Goods Sold | EXPENSE |
| 5100 | Salaries Expense | EXPENSE |

**Journal entry**: date (defaults to today), description, reference, lines. Rules
- At least two lines; each line has exactly one of debit or credit, greater than zero; no negatives.
- Total debits equal total credits, otherwise 422.
- Unknown or inactive account: 404 or 422.
- Entries are append-only; there is no update or delete (corrections are made with a new reversing entry).

**Trial balance**: per account total debit and credit, grand totals, and `balanced` flag.

## 3.5 erp-sales

**Customer**: name, optional unique email, phone, company.

**Sales order**: customer plus lines (product, quantity at least 1). Created as DRAFT; unit prices are copied from
the product at that moment, line total = price times quantity, order total = sum of lines. Inactive products
cannot be ordered.

- Confirm (DRAFT only): deduct stock for every line, set CONFIRMED, publish `SalesInvoicedEvent`, ledger entry
  posted. If any line lacks stock, the whole transaction rolls back: no stock changes, order stays DRAFT, 422.
- Cancel (DRAFT only): set CANCELLED, no stock or ledger effect.
- CONFIRMED and CANCELLED orders are final.

## 3.6 erp-reporting

All endpoints are read-only and MANAGER or ADMIN.

| Report | Content |
|---|---|
| Dashboard | active employees, active products, low-stock count, inventory valuation (stock times price), draft and confirmed orders, confirmed revenue, ledger balanced |
| Sales summary | counts per status, confirmed revenue, average confirmed order value |
| Low stock | products at or below reorder level, lowest first |
| Trial balance | same as accounting |
