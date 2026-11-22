# Personal Finance Tracker

A Kotlin and Spring Boot REST microservice backed by PostgreSQL, Hibernate, and Flyway.

## Prerequisites

- JDK 19 or later
- Docker with Docker Compose, or a PostgreSQL 16 server

## Run locally

Start the local PostgreSQL database:

```bash
docker compose up -d
```

For local testing, start the application on port 8081:

```bash
PORT=8081 ./gradlew bootRun --args='--spring.profiles.active=local'
```

> **Local testing only:** The `local` profile disables authentication and grants admin access to requests. Never enable it in staging, production, or any other shared or higher environment. Anyone who can reach the application while this profile is active can access its API without credentials.

On startup, Flyway V1 creates the schema and V2 inserts the demo client, sample accounts, and sample transactions. Hibernate validates the schema against the mapped entities. The API listens on [http://localhost:8081](http://localhost:8081).

The demo migration creates a `client-01` profile and assigns the sample accounts to it. Its phone number and email are placeholders.

The consolidated migration is intended for a **new, empty database**. The original V1–V10 migrations are preserved under `src/main/resources/db/legacy-migration` for databases that have already run them. To start the application against one of those existing databases, select that migration location:

```bash
FLYWAY_LOCATIONS=classpath:db/legacy-migration \
./gradlew bootRun --args='--spring.profiles.active=local'
```

Do not point the consolidated migration location at a database with existing Flyway history. For an existing database, keep using the legacy location so Flyway can validate its original migration versions and checksums.

The database connection defaults are configured for the included Compose service. Override them with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for another PostgreSQL instance.

> **Local database only:** The Compose configuration uses a fixed demo password. PostgreSQL is bound to localhost for local development; do not use this configuration in staging, production, or other shared environments. Use securely managed credentials and restrict network access for deployed databases.

## REST endpoints

All finance endpoints use the `/api/v1` base path:

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/accounts` | List accounts |
| `POST` | `/api/v1/clients` | Create a client profile |
| `GET` | `/api/v1/clients/{clientId}` | Get a client profile and its accounts |
| `GET` | `/api/v1/transactions` | Filter and page transactions |
| `GET` | `/api/v1/transactions/{id}` | Get a transaction by ID; returns `404` if it is not found |
| `PATCH` | `/api/v1/transactions/{id}/category` | Categorize a transaction |
| `GET` | `/api/v1/accounts/{accountId}/unusual-activities?month=YYYY-MM` | Flag unusual Nightlife/Video Games expenses |
| `GET` | `/api/v1/clients/{clientId}/monthly-expenses?month=YYYY-MM` | Get total expenses across a client's accounts for a month |
| `GET` | `/api/v1/clients/{clientId}/monthly-savings?month=YYYY-MM` | Get savings (income minus expenses) across a client's accounts |
| `GET` | `/api/v1/clients/{clientId}/spending-summary?month=YYYY-MM` | Get monthly total spending and spending grouped by category |
| `PUT` | `/api/v1/clients/{clientId}/monthly-spending-limits` | Set a category spending limit for a month |
| `POST` | `/api/v1/accounts/{accountId}/transactions/import` | Import transactions from a CSV bank statement |
| `POST` | `/api/v1/accounts/{accountId}/transactions/import/pdf` | **TODO:** Import transactions from a PDF bank statement (currently returns `501 Not Implemented`) |
| `GET` | `/api/v1/summary` | Get a summary of the accounts and transactions |
| `GET` | `/actuator/health` | Check application health |

Example requests:

```bash
curl "http://localhost:8081/api/v1/clients/1/spending-summary?month=2026-11"
curl http://localhost:8081/api/v1/accounts
curl -X POST http://localhost:8081/api/v1/clients \
  -H "Content-Type: application/json" \
  -d '{"name":"Alex Doe","phoneNumber":"+1-555-0100","email":"alex@example.com"}'
curl http://localhost:8081/api/v1/clients/1
curl "http://localhost:8081/api/v1/transactions?type=EXPENSE&startDate=2026-10-01&endDate=2026-11-30&minAmount=10&maxAmount=1000&page=0&size=20"
curl "http://localhost:8081/api/v1/accounts/checking-001/unusual-activities?month=2026-10"
curl "http://localhost:8081/api/v1/clients/1/monthly-expenses?month=2026-10"
curl "http://localhost:8081/api/v1/clients/1/monthly-savings?month=2026-10"
curl http://localhost:8081/api/v1/summary
curl http://localhost:8081/actuator/health
```

Transaction filters are optional: `type` is `INCOME`, `EXPENSE`, or `TRANSFER`. Transfers can tag card payments to prevent counting them as expenses. `startDate` and `endDate` are inclusive ISO dates, and `minAmount`/`maxAmount` filter the absolute amount. Pagination is zero-based (`page`, default `0`; `size`, default `20`, maximum `100`). Results are ordered newest first and include page and total-count metadata.

Client data is scoped to the requested client profile for profile, account, transaction, import, and monthly-total access. Attempts to access another client's profile, account, or transaction return `404`. The overall summary is scoped to the demo client while running with the `local` profile.

Unusual activity checks compare the requested month's total amount and transaction count for `NIGHTLIFE` and `VIDEO_GAMES` against twice their average monthly amount/count in the preceding three months. A category is flagged when either measure exceeds that threshold; other categories (such as transit and groceries) are not checked. Categories are assigned during CSV import.

The monthly spending limit endpoint is scoped to `{clientId}`. Its request body accepts a month (`YYYY-MM`), category, and limit amount. Limits are persisted and an existing limit for the same client, month, and category is updated.

Use `PATCH /api/v1/transactions/{id}/category` with a category name to categorize or recategorize a transaction (for example, `{"category":"groceries"}` or `{"category":"rent"}`). Category names are trimmed and stored uppercase. `GET /api/v1/clients/{clientId}/spending-summary?month=YYYY-MM` returns expense totals for the month, broken down by category; uncategorized expenses are grouped under `UNCATEGORIZED`.

The import endpoint accepts a multipart `file` containing a UTF-8, comma-separated CSV with columns `date,description,amount` and optional `type` (`INCOME`, `EXPENSE`, or `TRANSFER`) and `category` columns. A non-empty category supplied in the CSV is trimmed and stored uppercase; otherwise, the category is inferred from recognized descriptions and unknown descriptions remain uncategorized. Maintain the description keyword lists in `src/main/resources/transaction-categories.properties`. Without a type column, positive amounts are imported as income and zero or negative amounts as expenses. Dates use ISO `YYYY-MM-DD`; descriptions must be single words and quoted fields are not supported. The account ID is supplied in the URL. A successful import returns `201 Created` with the imported transactions; malformed statements return `400`, and unknown accounts return `404`.

For example, the sample statement includes an explicit `SELF_CARE` category for `Spa`; blank category cells use description-based categorization:

```bash
curl -X POST -F "file=@src/test/resources/sample-bank-statement.csv" \
  http://localhost:8081/api/v1/accounts/checking-001/transactions/import
```

## Run tests

```bash
./gradlew test
```
