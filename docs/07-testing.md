# 7. Testing

## 7.1 Strategy

| Level | Tool | Scope | Speed |
|---|---|---|---|
| Unit | JUnit 5 + Mockito | One service or one pure function, no Spring, no database | milliseconds |
| End-to-end | Spring Boot Test + MockMvc | Real HTTP layer, security, JPA, H2 database, all modules wired | seconds |

Rules covered most heavily are the ones where a bug costs money: tax, stock, double-entry, order confirmation.

## 7.2 How to run

```bash
mvn verify                        # everything
mvn -pl erp-hr test               # one module
mvn -pl erp-api -am test          # end-to-end tests and the modules they need
```

Reports: `*/target/surefire-reports/`.

## 7.3 Test cases

### Unit tests

| Test class | Case | Expected |
|---|---|---|
| PayrollServiceTest | Salary 20000 (first band only) | tax 1000.00 |
| | Salary 50000 (two bands) | tax 3750.00 |
| | Salary 100000 (three bands) | tax 11250.00 |
| | Salary 0 | tax 0.00 |
| ProductServiceTest | Add 5 to stock of 10 | stock 15, audit written |
| | Remove 11 from stock of 10 | InsufficientStockException, nothing saved |
| | Remove exactly 10 | stock 0, flagged low stock |
| | Adjust by 0 | BusinessRuleException |
| JournalServiceTest | Debit 100, credit 100 | accepted |
| | Debit 100.0, credits 90.00 + 10.00 | accepted (scale-insensitive compare) |
| | Debit 100, credit 99.99 | rejected |
| | One line only | rejected |
| | Line with both debit and credit | rejected |
| | Negative amount | rejected |
| SalesOrderServiceTest | Confirm a draft order | status CONFIRMED, stock deducted, invoice event published |
| | Confirm with insufficient stock | exception, order stays DRAFT, no event |
| | Confirm or cancel a confirmed order | BusinessRuleException |
| | Cancel a draft order | CANCELLED, stock untouched |

### End-to-end tests (`ErpEndToEndTest`)

| Case | Expected |
|---|---|
| GET employees without a token | 401 |
| Login with wrong password | 401 |
| USER role reads employees | 403 |
| Create employee with blank name | 400 with field details |
| Create employee twice with same email | 422 |
| Purchase order received, product stock rises to 10; receiving it again | 200 then 422 |
| Sell 4 units, stock falls to 6, order total 400 | 200, CONFIRMED |
| Sell 7 units when 6 remain | 422, stock still 6 |
| Trial balance after the whole cycle | `balanced: true` |
| Dashboard after the cycle | `ledgerBalanced: true` |
| Payroll run twice for the same month | same number of rows, ledger still balanced |
| Payroll with an invalid period | 422 |

## 7.4 Manual test script (for screenshots)

1. Log in as admin in Swagger UI and authorise.
2. `GET /api/reports/dashboard` - note low-stock count.
3. `GET /api/inventory/products/low-stock` - KEY-010 and CHR-200 appear in the demo data.
4. Create a sales order for 30 laptops (`LAP-001` has 25) and confirm it: expect 422.
5. Create an order for 2 laptops and confirm it: expect 200; stock becomes 23.
6. `GET /api/accounting/trial-balance` - revenue and receivables show the sale, `balanced: true`.
7. `POST /api/hr/payroll/2026-10/run`, then the trial balance again.
8. Log in as `user` and call `GET /api/hr/employees`: expect 403.

## 7.5 Test results

> Fill this in after running `mvn verify` on your machine. The test code was written together with the
> application, but at the time of writing this document the suite had not been executed in an environment
> with access to Maven Central, so no pass/fail numbers are claimed here.

| Date | Command | Tests run | Failures | Errors | Skipped | Notes |
|---|---|---|---|---|---|---|
| | `mvn verify` | | | | | |

Paste the Maven summary here:

```
[INFO] Tests run: ?, Failures: ?, Errors: ?, Skipped: ?
[INFO] BUILD ?
```

## 7.6 Not covered yet

- Load and performance tests.
- Concurrency test that proves the HTTP 409 on simultaneous stock changes.
- Security tests for token expiry and tampering.
- Controller-level slice tests for every endpoint.
