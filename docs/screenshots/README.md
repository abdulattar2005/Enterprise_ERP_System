# Visual Documentation

Screenshots demonstrate that the system works. Run the app (`mvn -pl erp-api spring-boot:run`), open
http://localhost:8080/swagger-ui.html and capture the following. Save them in this folder with these names, then
they appear in the README and the project documentation.

| File name | What to capture |
|---|---|
| 01-swagger-overview.png | Swagger UI home showing all endpoint groups |
| 02-login-token.png | `POST /api/auth/login` response with a token (blur the token if you publish it) |
| 03-dashboard.png | `GET /api/reports/dashboard` response |
| 04-low-stock.png | `GET /api/inventory/products/low-stock` response |
| 05-order-rejected-422.png | Confirming an order for more stock than available: the 422 message |
| 06-order-confirmed.png | Confirming a valid order: status CONFIRMED |
| 07-trial-balance.png | `GET /api/accounting/trial-balance` with `balanced: true` |
| 08-payroll.png | Payroll run response with gross, tax and net |
| 09-forbidden-403.png | Logged in as `user`, calling an HR endpoint: 403 |
| 10-audit-log.png | `GET /api/audit` as admin showing recorded actions |
| 11-tests-passing.png | Terminal showing `mvn verify` with BUILD SUCCESS |
| 12-docker-compose.png | `docker compose ps` with all services up (optional) |
| 13-grafana-prometheus.png | Prometheus targets page or a Grafana panel (optional) |

Embed in Markdown like this:

```markdown
![Dashboard](docs/screenshots/03-dashboard.png)
```
