# 4. API Reference

Base URL (local): `http://localhost:8080`. Interactive docs: `/swagger-ui.html`. Machine-readable: `/v3/api-docs`
(both disabled in the `prod` profile).

All endpoints except login, health and metrics need the header `Authorization: Bearer <token>`.
Lists accept `?page=0&size=20&sort=id,desc`.

## Authentication

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | /api/auth/login | public | Body `{"username","password"}` returns `{"accessToken","tokenType":"Bearer","expiresInSeconds"}` |
| GET | /api/auth/me | any | Current username and roles |

## HR

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | /api/hr/employees | MANAGER | Create |
| GET | /api/hr/employees | MANAGER | List, filter `?department=` |
| GET | /api/hr/employees/{id} | MANAGER | |
| PUT | /api/hr/employees/{id} | MANAGER | |
| DELETE | /api/hr/employees/{id} | ADMIN | Terminates (soft delete) |
| POST | /api/hr/attendance/{employeeId}/check-in | MANAGER | |
| POST | /api/hr/attendance/{employeeId}/check-out | MANAGER | |
| GET | /api/hr/attendance/{employeeId}?from=2026-10-01&to=2026-10-31 | MANAGER | |
| POST | /api/hr/payroll/{period}/run | ADMIN | `period` like `2026-10`; idempotent |
| GET | /api/hr/payroll/{period} | ADMIN | |
| POST | /api/hr/reviews | MANAGER | Body `{employeeId, period, rating 1-5, comments}` |
| GET | /api/hr/reviews/employee/{employeeId} | MANAGER | |

Create employee example:

```json
{
  "firstName": "Asha", "lastName": "Patil", "email": "asha.patil@example.com",
  "department": "Engineering", "jobTitle": "Backend Developer",
  "monthlySalary": 60000, "hireDate": "2025-04-01"
}
```

## Inventory

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | /api/inventory/products | MANAGER | |
| GET | /api/inventory/products | USER | `?category=` filter |
| GET | /api/inventory/products/low-stock | USER | |
| GET | /api/inventory/products/{id} | USER | Cached |
| PUT | /api/inventory/products/{id} | MANAGER | Does not change stock |
| POST | /api/inventory/products/{id}/adjust-stock | MANAGER | Body `{"delta": -3, "reason": "damaged"}` |
| POST, GET | /api/inventory/suppliers, /{id} | MANAGER | |
| POST, GET | /api/inventory/warehouses, /{id} | MANAGER | |
| POST | /api/inventory/purchase-orders | MANAGER | See example |
| POST | /api/inventory/purchase-orders/{id}/receive | MANAGER | Adds stock, posts ledger |
| GET | /api/inventory/purchase-orders, /{id} | MANAGER | |

Create purchase order:

```json
{ "supplierId": 1, "lines": [ { "productId": 3, "quantity": 20, "unitCost": 800.00 } ] }
```

## Sales

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | /api/sales/customers | MANAGER | |
| GET | /api/sales/customers, /{id} | MANAGER | |
| POST | /api/sales/orders | MANAGER | Draft, priced from products |
| POST | /api/sales/orders/{id}/confirm | MANAGER | Stock + invoice + ledger, atomic |
| POST | /api/sales/orders/{id}/cancel | MANAGER | Draft only |
| GET | /api/sales/orders, /{id} | MANAGER | |

Create sales order:

```json
{ "customerId": 1, "lines": [ { "productId": 1, "quantity": 2 } ] }
```

## Accounting

| Method | Path | Role | Notes |
|---|---|---|---|
| GET | /api/accounting/accounts | MANAGER | |
| POST | /api/accounting/accounts | ADMIN | |
| POST | /api/accounting/journal-entries | MANAGER | Must balance |
| GET | /api/accounting/journal-entries | MANAGER | |
| GET | /api/accounting/trial-balance | MANAGER | |

Manual journal entry:

```json
{
  "description": "Owner investment", "reference": "MAN-1",
  "lines": [
    { "accountCode": "1000", "debit": 100000 },
    { "accountCode": "3000", "credit": 100000 }
  ]
}
```

## Reports

| Method | Path | Role |
|---|---|---|
| GET | /api/reports/dashboard | MANAGER |
| GET | /api/reports/sales-summary | MANAGER |
| GET | /api/reports/low-stock | MANAGER |
| GET | /api/reports/trial-balance | MANAGER |

## Audit

| Method | Path | Role | Notes |
|---|---|---|---|
| GET | /api/audit | ADMIN | Newest first; `?page=&size=` (max 200) |

## Operations endpoints

| Path | Access | Notes |
|---|---|---|
| /actuator/health, /health/liveness, /health/readiness | public | Probes |
| /actuator/prometheus | public in this build | Restrict to the cluster network in production |

## Error example

```json
{
  "timestamp": "2026-10-27T10:15:30Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Insufficient stock for LAP-001: available 25, requested 30",
  "details": []
}
```
