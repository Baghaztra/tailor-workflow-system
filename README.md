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
- Spring Boot Actuator (`/actuator/health`)
- Springdoc OpenAPI 3.1.0 (Swagger UI)
- PostgreSQL driver
- JUnit 5
- Gradle 9.8.1 (wrapper)

Planned:

- Spring Security + JWT
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

Set `DB_PASSWORD` in `.env` to your PostgreSQL password. Adjust `spring.datasource.*` in `src/main/resources/application.properties` if your host, port, or username differ.

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
- CORS for the frontend origin is set with `app.cors.allowed-origins` in `application.properties` (default `http://localhost:8082`).
- `.env` is ignored by git; only `.env.example` is committed.

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

The app runs at `http://localhost:8082`.

Environment variables (`.env`):

| Variable | Default | Purpose |
|---|---|---|
| `VITE_APP_PORT` | `8082` | Dev and preview server port (`strictPort`, so it fails instead of silently switching ports) |
| `VITE_API_BASE_URL` | `http://localhost:8081` | Backend base URL for API calls |

Other scripts: `bun run build` (type-check and production build), `bun run type-check`.

---

# API Reference

Base URL: `http://localhost:8081`

## Workers

| Method | Path | Description |
|---|---|---|
| GET | `/api/workers?active=true` | List workers, optionally filtered by active status |
| GET | `/api/workers/{id}` | Get one worker |
| POST | `/api/workers` | Create a worker |
| PUT | `/api/workers/{id}` | Update a worker |

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
| GET | `/api/orders?status=CREATED` | List orders, optionally filtered by status |
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
| `assign` | status `CREATED`/`IN_PROGRESS`, no stage in progress, worker active | stage = worker specialization, status `IN_PROGRESS` |
| `start` | status `IN_PROGRESS`, stage assigned | `STARTED` log |
| `complete` | status `IN_PROGRESS`, stage assigned | stage cleared; `FINISHING` moves the order to `READY_FOR_PICKUP` |
| `pickup` | status `READY_FOR_PICKUP` | status `PICKED_UP` |
| `payment` | status `PICKED_UP` | status `COMPLETED` |
| `cancel` | status not `COMPLETED`/`CANCELLED` | status `CANCELLED` |

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
- `404 Not Found` for unknown worker or order.
- `409 Conflict` for a transition that is not allowed in the current status.

---

# Features

## Implemented

- Worker management: list, create, edit, activate/deactivate, specialization.
- Order management: list with status filter, create, edit, cancel, detail view.
- Workflow actions: assign, start, complete, pickup, payment, cancel, with transition rules.
- Order timeline: written automatically by workflow actions and displayed chronologically.
- Finance: payment records, revenue summary (total/month/today), and a Finance page with date filters.
- Dashboard: active orders, pending pickups, active workers, total orders, recent orders, revenue.
- REST API with validation, 404/409 handling, Swagger UI, and health check.
- Service layer for worker, order, and finance business rules.
- Frontend admin UI with sidebar layout, tables, modal forms, and responsive CSS.

## Not Implemented Yet

- Authentication and role-based authorization (Admin, Worker).
- Enforcing the CUTTING → SEWING → FINISHING order when assigning a worker.
- Idempotency guards against repeated `start`/`complete` calls.
- Partial or down payments (only one full payment per order is supported).
- Finance exports and daily report pages.
- Flyway migrations.
- Pagination, search, and automated tests.
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

├── config            (CORS)
├── controller        (workers, orders, order logs, finance)
├── entity            (Worker, Order, OrderLog, Payment)
├── repository        (Spring Data JPA)
├── service           (WorkerService, OrderService, FinanceService)
├── dto               (planned)
├── security          (planned)
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
├── pages             (dashboard, workers, orders, order detail, finance)
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
| Worker management | 90% |
| Order management | 90% |
| Order timeline | 95% |
| Workflow management | 85% |
| Dashboard | 70% |
| Finance | 75% |
| Authentication | 0% |
| Infrastructure and code quality | 55% |
| **Overall** | **±70%** |

Next steps, in order:

1. Flyway migrations to replace `ddl-auto=update`.
2. JWT authentication and role-based authorization.
3. Pagination, search, and automated tests.
4. Enforce stage order and add idempotency guards.

---

# License

This project is intended for educational purposes and portfolio demonstration.
