# Tailor Workflow System

A full-stack tailor order management system built to simulate a real-world tailoring business workflow.

This project is designed as a portfolio project for learning enterprise Java development using Spring Boot while applying clean architecture, RESTful API design, and business workflow modeling.

The frontend is built with Vue 3, TypeScript, and Vite 8, while the backend exposes a REST API using Spring Boot 4 on Java 17.

---

## Goals

- Learn modern Java backend development.
- Practice Spring Boot in a real-world project.
- Design a business workflow instead of a simple CRUD application.
- Apply clean architecture and separation of concerns.
- Demonstrate enterprise application development skills.

---

## Project Overview

The system manages the complete lifecycle of a tailoring order.

Example workflow:

```text
Customer Order
        │
        ▼
Order Created
        │
        ▼
Assigned to Cutting Worker
        │
        ▼
Cutting Completed
        │
        ▼
Assigned to Sewing Worker
        │
        ▼
Sewing Completed
        │
        ▼
Assigned to Finishing Worker
        │
        ▼
Finishing Completed
        │
        ▼
Ready for Pickup
        │
        ▼
Order Picked Up
        │
        ▼
Payment Recorded
```

Instead of focusing on graphics or gameplay, this project focuses on modeling a real business process through a clean and maintainable backend architecture.

---

# Technology Stack

## Backend

- Java 17 (Gradle toolchain)
- Spring Boot 4.0.8
- Spring Web MVC (`spring-boot-starter-webmvc`)
- Spring Data JPA
- Spring Validation
- Spring Security
- JWT via `java-jwt` 4.4.0 (HttpOnly cookie)
- Spring Boot Actuator (`/actuator/health`)
- Springdoc OpenAPI 3.1.0 (Swagger UI)
- PostgreSQL driver
- JUnit 5
- Gradle 9.8.1 (wrapper)

Planned:

- Flyway

## Frontend

- Vue 3.5
- TypeScript 6
- Vite 8
- Vue Router 5
- Axios
- Bun (package manager)
- vue-tsc + vite-plugin-vue-devtools (development)

## Database

- PostgreSQL
- Flyway (planned)

---

# Getting Started

## Prerequisites

- JDK 17
- Bun
- PostgreSQL running locally

## Backend

Create the database:

```sql
CREATE DATABASE tailor;
```

Create your local configuration:

```sh
cd backend
cp .env.example .env
```

Set the values in `.env`:

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | PostgreSQL password |
| `ADMIN_USERNAME` | Admin login username |
| `ADMIN_PASSWORD` | Admin login password |
| `JWT_SECRET` | HMAC secret, at least 32 characters |
| `AUTH_COOKIE_SECURE` | `true` behind HTTPS; `false` for local HTTP |

Adjust `spring.datasource.*` in `src/main/resources/application.properties` if your host, port, or username differ.

Run the application:

```sh
cd backend
./gradlew bootRun
```

On Windows:

```sh
cd backend
gradlew.bat bootRun
```

The API runs at `http://localhost:8081`:

| URL | Purpose |
|---|---|
| `http://localhost:8081/api/*` | REST API |
| `http://localhost:8081/docs` | Swagger UI |
| `http://localhost:8081/v3/api-docs` | OpenAPI JSON |
| `http://localhost:8081/actuator/health` | Health check |

Notes:

- `server.port=8081` is set because port 8080 is used by another local application.
- `ddl-auto=update` is enabled for development, so tables are created automatically. This will be replaced by Flyway migrations.
- CORS for the frontend origin is set with `app.cors.allowed-origins` in `application.properties` (default `http://localhost:8082`), with credentials enabled for the auth cookie.
- All `/api/**` endpoints except `/api/auth/login` require authentication. Swagger UI and the health endpoint stay public.
- The JWT is stored in an `HttpOnly`, `SameSite=Lax` cookie named `tailor_token`; it is never exposed to JavaScript.
- `.env` is ignored by git; only `.env.example` is committed.
- CSRF protection is disabled and the auth cookie uses `SameSite=Lax`. For a cross-site deployment, enable CSRF protection and set `AUTH_COOKIE_SECURE=true` behind HTTPS.

## Frontend

Create your local configuration:

```sh
cd frontend
cp .env.example .env
```

Install and run:

```sh
cd frontend
bun install
bun dev
```

The app runs at `http://localhost:8082`. Sign in with `ADMIN_USERNAME` and `ADMIN_PASSWORD` from `backend/.env`; all other routes redirect to `/login` until authenticated.

Environment variables (`.env`):

| Variable | Default | Purpose |
|---|---|---|
| `VITE_APP_PORT` | `8082` | Dev and preview server port (`strictPort`, so it fails instead of silently switching ports) |
| `VITE_API_BASE_URL` | `http://localhost:8081` | Backend base URL for API calls |

Other scripts: `bun run build` (type-check and production build), `bun run type-check`.

---

# API Reference

Base URL: `http://localhost:8081`

All endpoints below require the `tailor_token` cookie, except `/api/auth/login`.

## Authentication

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | `{ "username": "...", "password": "..." }`; sets the cookie |
| POST | `/api/auth/logout` | Clears the cookie |
| GET | `/api/auth/me` | Returns the signed-in admin |

The cookie is `HttpOnly`, `SameSite=Lax`, valid for 8 hours. `401` means the session is missing or expired.

## Workers

| Method | Path | Description |
|---|---|---|
| GET | `/api/workers?active=true&search=budi&page=0&size=20` | Paged list with optional active filter and name search |
| GET | `/api/workers/{id}` | Get one worker |
| POST | `/api/workers` | Create a worker |
| PUT | `/api/workers/{id}` | Update a worker |

List responses use the page shape:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Create request:

```json
{
  "name": "Budi",
  "specialization": "CUTTING",
  "active": true
}
```

`specialization`: `CUTTING`, `SEWING`, `FINISHING`. `active` is optional and defaults to `true`.

There is no delete endpoint; workers are deactivated with `active: false` so order history stays intact.

## Orders

| Method | Path | Description |
|---|---|---|
| GET | `/api/orders?status=CREATED&search=ayu&page=0&size=20` | Paged list with status filter and customer search |
| GET | `/api/orders/{id}` | Get one order |
| POST | `/api/orders` | Create an order (writes a `CREATED` log) |
| PUT | `/api/orders/{id}` | Update customer name and total price while the order is active |
| POST | `/api/orders/{id}/assign` | Assign an active worker and start the next stage |
| POST | `/api/orders/{id}/start` | Mark the assigned stage as started |
| POST | `/api/orders/{id}/complete` | Complete the current stage |
| POST | `/api/orders/{id}/pickup` | Mark a ready order as picked up |
| POST | `/api/orders/{id}/payment` | Record payment and complete the order |
| POST | `/api/orders/{id}/cancel` | Cancel an active order |

Create request:

```json
{
  "customerName": "Ayu",
  "totalPrice": 250000
}
```

Assign request:

```json
{
  "workerId": 1
}
```

`status`: `CREATED`, `IN_PROGRESS`, `READY_FOR_PICKUP`, `PICKED_UP`, `COMPLETED`, `CANCELLED`.
`stage`: `CUTTING`, `SEWING`, `FINISHING` (nullable).

### Workflow rules

| Action | Allowed when | Result |
|---|---|---|
| `assign` | status `CREATED`/`IN_PROGRESS`, no stage in progress, worker active, worker matches the next stage | stage set, status `IN_PROGRESS` |
| `start` | status `IN_PROGRESS`, stage assigned, not already started | `STARTED` log |
| `complete` | status `IN_PROGRESS`, stage assigned | stage cleared; `FINISHING` moves the order to `READY_FOR_PICKUP` |
| `pickup` | status `READY_FOR_PICKUP` | status `PICKED_UP` |
| `payment` | status `PICKED_UP` | status `COMPLETED` |
| `cancel` | status not `COMPLETED`/`CANCELLED` | status `CANCELLED` |

Stages must follow `CUTTING` → `SEWING` → `FINISHING`; assigning the wrong specialization returns `409`. Calling `start` twice also returns `409` without adding a log.

Every action writes a timeline entry automatically; status and logs stay consistent. There is no endpoint to set status directly.
Orders are cancelled, not deleted, so history stays intact.

### Payment

Payment request:

```json
{
  "amount": 250000,
  "method": "CASH"
}
```

`method`: `CASH`, `TRANSFER`. One payment per order, `amount` at least `0.01`. A second payment returns `409 Conflict`.
If `totalPrice` is set, the frontend pre-fills the amount.

## Finance

| Method | Path | Description |
|---|---|---|
| GET | `/api/finance/summary` | Total, monthly, and daily revenue plus payment count |
| GET | `/api/finance/report?from=2026-10-01&to=2026-10-31` | Daily revenue rows, oldest first |
| GET | `/api/finance/export?from=2026-10-01&to=2026-10-31` | Payment CSV download |
| GET | `/api/payments?from=2026-10-01&to=2026-10-31` | Payment records, newest first; both date filters are optional |

Summary response:

```json
{
  "totalRevenue": 250000,
  "monthRevenue": 250000,
  "todayRevenue": 250000,
  "paymentCount": 1
}
```

## Order Logs (Timeline)

| Method | Path | Description |
|---|---|---|
| GET | `/api/orders/{orderId}/logs` | Get the order timeline, oldest first |

Logs are append-only and created by workflow actions. Actions: `CREATED`, `ASSIGNED`, `STARTED`, `COMPLETED`, `READY_FOR_PICKUP`, `PICKED_UP`, `PAYMENT_RECORDED`, `CANCELLED`.

## Validation and Errors

- `400 Bad Request` for invalid bodies (blank names, unknown enum values, negative price) or an inactive worker.
- `401 Unauthorized` for a missing or expired session.
- `404 Not Found` for unknown worker or order.
- `409 Conflict` for a transition that is not allowed in the current status.

---

# Features

## Implemented

- Worker management: paginated list with name search, create, edit, activate/deactivate, specialization.
- Order management: paginated list with status filter and customer search, create, edit, cancel, detail view.
- Workflow actions: assign, start, complete, pickup, payment, cancel, with transition rules and enforced stage order.
- Order timeline: written automatically by workflow actions and displayed chronologically.
- Finance: payment records, revenue summary (total/month/today), daily report, CSV export, and a Finance page with date filters.
- Dashboard: active orders, pending pickups, active workers, total orders, recent orders, revenue.
- Admin authentication: JWT in an HttpOnly cookie, login page, route guard, and protected API.
- REST API with validation, 401/404/409 handling, Swagger UI, and health check.
- Service layer for worker, order, and finance business rules.
- Frontend admin UI with sidebar layout, tables, modal forms, and responsive CSS.

## Not Implemented Yet

- Worker login and multi-user roles (only a single admin account can sign in).
- Partial or down payments (only one full payment per order is supported).
- Refresh tokens, token revocation, and login rate limiting.
- Pagination for payments (the date filter and daily report are used instead).
- Flyway migrations (deferred by choice).
- Automated tests (deferred by choice).
- Hard delete for workers and orders (intentionally replaced by deactivate/cancel).

---

# Architecture

```text
Browser
      │
      ▼
Vue 3 + TypeScript + Vite 8
      │
 REST API (axios)
      │
      ▼
Spring Boot 4 (Java 17)
├── Controllers
├── Services (workflow rules, transactions)
├── Repositories
└── Entities
      │
      ▼
PostgreSQL
```

---

# Backend Structure

```text
backend/

src/main/java/com/baghaztra/tailorworkflow

├── config            (CORS, Security)
├── controller        (auth, workers, orders, order logs, finance)
├── dto               (PageResponse)
├── entity            (Worker, Order, OrderLog, Payment)
├── repository        (Spring Data JPA)
├── security          (JWT, cookie, admin credentials)
├── service           (WorkerService, OrderService, FinanceService)
└── exception         (planned)
```

---

# Frontend Structure

```text
frontend/

src

├── assets            (global CSS)
├── components        (sidebar, base modal)
├── layouts           (main layout)
├── pages             (login, dashboard, workers, orders, order detail, finance)
├── router
├── services          (axios client, API modules)
├── utils             (date and currency formatting)
└── types.ts          (shared types and enums)
```

---

# Database Schema

| Table | Columns |
|---|---|
| `workers` | `id`, `name`, `specialization`, `active`, `created_at` |
| `orders` | `id`, `customer_name`, `status`, `stage`, `total_price`, `created_at`, `updated_at` |
| `order_logs` | `id`, `order_id`, `worker_id`, `stage`, `action`, `note`, `created_at` |
| `payments` | `id`, `order_id` (unique), `amount`, `method`, `paid_at` |

Enums are stored as strings. `order_logs.order_id` is indexed, `payments.order_id` is unique, and relations are loaded lazily.

---

# Project Status

This project is under active development. Progress against the planned features:

| Module | Progress |
|---|---|
| Worker management | 95% |
| Order management | 95% |
| Order timeline | 95% |
| Workflow management | 95% |
| Dashboard | 75% |
| Finance | 90% |
| Authentication | 90% |
| Infrastructure and code quality | 70% |
| **Overall** | **±85%** |

Deferred by choice: Flyway, automated tests, and partial payments. Optional next steps:

1. Flyway migrations to replace `ddl-auto=update`.
2. Automated tests for workflow and auth.
3. Refresh token, logout revocation, and login rate limiting.
4. Pagination for payments and dashboard stats endpoints.

---

# License

This project is intended for educational purposes and portfolio demonstration.
