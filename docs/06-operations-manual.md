# 6. Operations Manual

## 6.1 Health and monitoring

| What | Where |
|---|---|
| Liveness | `/actuator/health/liveness` (restart the pod if this fails) |
| Readiness | `/actuator/health/readiness` (remove from load balancing if this fails) |
| Metrics | `/actuator/prometheus` |
| Dashboards | Grafana, Prometheus datasource pre-provisioned |
| Alerts | `monitoring/alerts.yml` |

Useful queries:

| Question | PromQL |
|---|---|
| Requests per second | `sum(rate(http_server_requests_seconds_count[1m]))` |
| Error ratio | `sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))` |
| p95 latency | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))` |
| DB connections in use | `hikaricp_connections_active` |
| JVM heap used | `jvm_memory_used_bytes{area="heap"}` |

Histogram buckets for latency are not enabled by default in Spring Boot. To use the p95 alert, add
`management.metrics.distribution.percentiles-histogram.http.server.requests: true` to `application.yml`.

## 6.2 Logs

Each line has a correlation id: `2026-10-27 10:15:30.123 INFO  [3f2c...] c.e.s.SalesOrderService - ...`.
To trace one request, take the `X-Correlation-Id` response header and search the logs for it.
Clients may send their own `X-Correlation-Id` (up to 64 characters).

## 6.3 Alert response (runbooks)

| Alert | Likely cause | First steps |
|---|---|---|
| ErpInstanceDown | Crash, bad deploy, DB unreachable | `kubectl -n erp get pods`, `kubectl -n erp logs <pod>`, check readiness, roll back if a deploy just happened |
| ErpHighErrorRate | Code bug, DB trouble, dependency failure | Look for 500 logs by correlation id, check DB health, roll back the last release if correlated |
| ErpSlowRequests | Slow queries, CPU saturation, pool exhaustion | Check CPU and DB pool panels, look for large unpaged queries, scale out |
| ErpDbPoolExhausted | Long transactions or too small a pool | Check slow queries; raise `DB_POOL_SIZE` only if the database has capacity |

## 6.4 Incident process

1. Detect: alert or user report. Note the time.
2. Assess: user impact, since when, which endpoints.
3. Mitigate first: roll back, scale out, or disable the failing feature. Fix root cause afterwards.
4. Communicate: tell affected users what is wrong and when the next update comes.
5. Review: write down timeline, root cause and prevention actions within a few days.

## 6.5 Routine tasks

| Frequency | Task |
|---|---|
| Daily | Check dashboards and alert history; confirm backups succeeded |
| Weekly | Review audit log for unusual activity (`GET /api/audit`); check dependency-scan results |
| Monthly | Run payroll (`POST /api/hr/payroll/{yyyy-MM}/run`); confirm trial balance is balanced; review low-stock report |
| Quarterly | Test a database restore; review user access; rotate secrets |

## 6.6 Backup and recovery

- Database: use automated daily backups with point-in-time recovery (the Terraform reference keeps 7 days).
- Test the restore quarterly; an untested backup is not a backup.
- Recovery steps: restore the database to a new instance, update `DB_URL` in the secret, restart the deployment,
  verify with the smoke test and the trial balance.
- Application is stateless and rebuilt from the container image.

## 6.7 Common problems

| Symptom | Cause | Fix |
|---|---|---|
| App fails on start: `jwt-secret must be set` | Missing or short `ERP_JWT_SECRET` | Set a value of 32+ characters |
| 401 on every call | Token expired or missing `Bearer ` prefix | Log in again |
| 403 | Role too low for the endpoint | Use a user with the required role |
| 409 on stock change | Two changes at once | Reload the product and retry |
| 422 `Insufficient stock` | Not enough quantity | Receive a purchase order or adjust stock |
| 422 `Entry is not balanced` | Debits differ from credits | Fix the amounts |
| Stock looks old on one instance | Local cache, up to 5 minutes | Wait, or adopt a shared cache (Redis) |

## 6.8 Capacity notes

Starting point from the manifests: 2 pods at 250m CPU and 512 MiB each, scaling to 6. These are untested
estimates; run a load test (for example with k6 or JMeter) against staging before relying on them.
