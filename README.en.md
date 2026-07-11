<div align="center">

# 🏺 Gốm Sứ Vũ Gia — E-commerce Backend

**A production-grade Spring Boot REST API for the Gốm Sứ Vũ Gia ceramics store — catalog, cart, orders, coupons, CMS content, admin dashboard, JWT + RBAC, MinIO media and Flyway auto-migrate/seed.**

<br/>

<!-- 🌐 Language switcher / Nút chuyển ngôn ngữ -->
<a href="README.md"><img src="https://img.shields.io/badge/🇻🇳_Tiếng_Việt-555?style=for-the-badge" alt="Tiếng Việt"/></a>
<a href="README.en.md"><img src="https://img.shields.io/badge/🇬🇧_English-2C5BFF?style=for-the-badge" alt="English"/></a>

<br/><br/>

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Security-JWT%20%2B%20RBAC-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrate%20%2B%20Seed-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-C72E49?style=for-the-badge&logo=minio&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

</div>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Features & Modules](#-features--modules)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [Security Model](#-security-model)
- [Response Format](#-response-format)
- [License](#-license)

---

## 🧭 Overview

Backend REST API powering the **Gốm Sứ Vũ Gia** ceramics storefront. It covers the full commerce flow — product catalog, shopping cart, order placement with coupons, plus CMS-style content (news, pages, banners, gallery, showrooms, FAQ) and an admin analytics dashboard.

The codebase follows a strict **layered architecture** and **SOLID** principles: every service is coded against an interface, filtering is done with the JPA Specification API, DTO ↔ Entity mapping is compile-time (MapStruct), and cross-cutting concerns (security, errors, auditing) live in their own packages. Schema and demo data are applied automatically on startup via **Flyway**.

---

## 🛠 Tech Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Java | 21 (LTS) | Records, pattern matching, text blocks |
| **Framework** | Spring Boot | 3.5.10 | Auto-configuration & DI |
| **Web** | Spring Web (MVC) | — | REST controllers, JSON |
| **Persistence** | Spring Data JPA + Hibernate | — | ORM, repositories, Specification API |
| **Database** | MySQL | 8.x | Relational DB (`mysql-connector-j`) |
| **Migrations** | Flyway (+ `flyway-mysql`) | — | Auto migrate schema + seed data on startup |
| **Security** | Spring Security | — | Auth, method security (`@PreAuthorize`) |
| **Token** | JJWT | 0.12.6 | JWT access tokens (HS512) + refresh rotation |
| **OAuth** | Google Identity | — | "Login with Google" (ID-token verification) |
| **Object storage** | MinIO | 8.5.x | Product/asset images (public buckets) |
| **Email** | Spring Mail + Thymeleaf | — | Async **HTML** transactional emails |
| **Mapping** | MapStruct | 1.6.3 | Compile-time DTO ↔ Entity mapping |
| **Validation** | Jakarta Bean Validation | — | `@Valid` request payloads |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | — | Interactive docs at `/swagger-ui.html` |
| **Monitoring** | Spring Boot Actuator | — | `/actuator/health`, `/actuator/info` |
| **Boilerplate** | Lombok | — | Getters/setters/builders |
| **Build** | Maven (`mvnw` wrapper) | — | Dependency management & packaging |
| **Test** | JUnit 5 + Mockito + Spring Security Test | — | Service & controller tests |

---

## ✨ Features & Modules

### Core platform
- 🔐 **JWT auth + refresh rotation** — stateless HS512 access tokens; DB-backed refresh tokens (real logout/revocation). Login by **username or email**, plus **Google login**.
- 👥 **RBAC** — single-role model (`Role` enum: `SUPERADMIN` / `ADMIN` / `CUSTOMER`); endpoints guarded with `@PreAuthorize`.
- 🧾 **Unified envelope** — every response is `{ code, message, data, timestamp }`; `code=1000` = success.
- 🧯 **Global exception handling** — one `@RestControllerAdvice` maps exceptions to stable business error codes.
- 🕵️ **JPA auditing** — `createdAt / updatedAt / createdBy / updatedBy` populated automatically.
- 🔎 **Safe search & pagination** — JPA Specification + whitelisted sorting (1-based paging).
- 🌱 **Auto migrate + seed** — Flyway runs schema (`db/migration`) then demo data (`db/seed`) on startup; idempotent admin bootstrap.
- 🖼 **MinIO media** — uploads to public buckets (`assets`, `products`), buckets auto-created & made public-read on startup.

### Business modules
| Domain | Highlights |
|---|---|
| **Auth / User** | register · login · refresh · logout · me · Google login · change password · admin user management (list, create, change role, reset password) |
| **Products** | catalog + categories + images (MinIO), status/featured toggles, **SEO lookup by slug** |
| **Content / CMS** | News + categories (by slug), **Pages** (by key), Banners, Showrooms, Gallery, FAQ, Redirects |
| **Marketing** | **Coupons** (PERCENT / FIXED / FREE_SHIP, validate + conditions), Newsletter subscribe, Contact form |
| **Cart** | per-user cart, accumulate quantity, live totals |
| **Orders** | **idempotent checkout**, price snapshot, **atomic coupon claim** (race-free), cart deduction, `sold_count` bumped only on **COMPLETED**, **async HTML confirmation email**, admin order search |
| **Dashboard** | admin KPIs (revenue/orders/customers), daily revenue series, top-selling products |

> Full endpoint reference per module lives in [`docs/`](docs).

---

## 🏗 Architecture

One-way dependency flow; the web layer never touches persistence directly, and services depend on **interfaces**.

```text
HTTP ─▶ Controller ─▶ Service (interface → impl) ─▶ Repository (+ Specification) ─▶ MySQL
          │                     │
          │ ApiResponse<T>      │ MapStruct mapper
          ▼                     ▼
  GlobalExceptionHandler ◀─ AppException(ErrorCode)
          ▲
  Security filter chain (JWT) ─▶ 401 EntryPoint / 403 AccessDeniedHandler

  Order committed ─▶ @TransactionalEventListener(AFTER_COMMIT) ─▶ @Async HTML email (Thymeleaf)
```

---

## 📂 Project Structure

```text
src/main/java/vn/springboot
├── Application.java
├── common
│   ├── entity/BaseEntity.java            # id + audit fields
│   ├── exception/                        # ErrorCode, AppException, GlobalExceptionHandler
│   └── response/ApiResponse.java         # unified envelope
├── config                                # Async, Jpa auditing, MinIO, bucket init, data seeding
├── controller                            # 20 REST controllers (per module)
├── dto/{request,response}                # per-domain request/response DTOs
├── entity                                # 19 JPA entities (product, order, cart, news, page, …)
├── event                                 # OrderPlacedEvent + async OrderEmailListener
├── mapper                                # MapStruct mappers
├── repository (+ specification)          # Spring Data repos + dynamic filters
├── security                              # SecurityConfig, JWT, CustomUserDetails, handlers
└── service (+ impl)                      # business logic behind interfaces

src/main/resources
├── application.yaml
├── db/migration/V1__init_db.sql          # schema (Flyway)
├── db/seed/V2__seed_db.sql               # demo data (Flyway)
└── templates/email/order-confirmation.html   # HTML email (Thymeleaf)
```

---

## 🚦 Getting Started

### Option A — Docker (recommended, one command)

Brings up **MySQL + MinIO + app**; the app auto-migrates the schema and seeds demo data on boot.

```bash
docker compose up -d --build
docker compose logs -f app        # watch: "Successfully applied 2 migrations"
```

- API → **http://localhost:8080** (Swagger: `/swagger-ui.html`)
- MinIO Console → **http://localhost:9001** (`minioadmin` / `minioadmin123`)

### Option B — Local

**Prerequisites:** JDK 21, MySQL 8 running locally (MinIO optional, only needed to serve images).

```bash
# Fresh DB: MySQL auto-creates it via the URL flag; Flyway builds schema + seeds
DB_URL="jdbc:mysql://localhost:3306/dev_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false" \
  ./mvnw spring-boot:run

# Build a runnable jar
./mvnw clean package && java -jar target/spring-boot-0.0.1-SNAPSHOT.jar
```

### Default admin account

| Username | Password | Email |
|---|---|---|
| `admin` | `admin123` | `admin@gmail.com` |

> ⚠️ Change the password before deploying.

### Images (MinIO)

The app auto-creates the `assets` and `products` buckets as public-read. Upload the `assets/images/` folder into the **`assets`** bucket via the MinIO console — see **[docs/RUN_AND_SEED.md](docs/RUN_AND_SEED.md)** for the full flow. The DB stores **relative** image paths (e.g. `assets/images/gallery/gallery-1.jpg`); the frontend prepends the MinIO base URL.

---

## ⚙️ Configuration

Everything in `src/main/resources/application.yaml` is overridable via environment variables.

| Env variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/dev_db` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `rootpassword` | DB credentials |
| `APP_JWT_SECRET` | *(dev default)* | Base64 512-bit HS512 key — **must override in prod** |
| `MINIO_URL` | `http://localhost:9000` | MinIO endpoint (server-side) |
| `MINIO_PUBLIC_URL` | `http://localhost:9000` | Public base for image URLs (browser-facing / CDN) |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | `minioadmin` / `minioadmin123` | MinIO credentials |
| `MINIO_BUCKET_ASSET` / `MINIO_BUCKET_PRODUCT` | `assets` / `products` | Bucket names |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | *(empty)* | SMTP credentials (required to actually send email) |
| `APP_MAIL_FROM` | *(empty)* | "From" address for outgoing mail |
| `GOOGLE_CLIENT_ID` | *(empty)* | Google OAuth Web client ID (empty disables the check — dev only) |
| `VIETQR_BANK` / `VIETQR_ACCOUNT` / `VIETQR_HOLDER` | `MBBank` / `686804076868` / `NGUYEN DUY DAT` | Receiving account for the VietQR image (ONL orders) |
| `SEPAY_WEBHOOK_SECRET` | *(empty)* | Secret to verify the SePay webhook signature — **empty = reject all webhooks** (fail-closed) |
| `app.init.enabled` | `true` | Toggle startup admin seeding |

> 🔒 **Production:** generate a fresh `APP_JWT_SECRET`, set real SMTP + MinIO credentials, and point `MINIO_PUBLIC_URL` at your CDN/domain.

---

## 🔌 API Reference

Base path **`/api`** · Swagger UI **`/swagger-ui.html`** · Health **`/actuator/health`**.

Per-module reference (request/response, error codes, curl-for-Postman):

| Module | Doc |
|---|---|
| Auth & User (RBAC) | [docs/AUTH_USER_API.md](docs/AUTH_USER_API.md) |
| Products & Categories | [docs/PRODUCT_API.md](docs/PRODUCT_API.md) |
| News & Categories | [docs/NEWS_API.md](docs/NEWS_API.md) |
| Coupons | [docs/COUPON_API.md](docs/COUPON_API.md) |
| Cart | [docs/CART_API.md](docs/CART_API.md) |
| Orders | [docs/ORDER_API.md](docs/ORDER_API.md) |
| Admin Dashboard | [docs/DASHBOARD_API.md](docs/DASHBOARD_API.md) |
| Pages (CMS) | [docs/PAGE_API.md](docs/PAGE_API.md) |
| Contact | [docs/CONTACT_API.md](docs/CONTACT_API.md) |
| Newsletter | [docs/NEWSLETTER_API.md](docs/NEWSLETTER_API.md) |
| Banners / Showrooms / Gallery / FAQ / Redirects | [docs/BASIC_MODULES_API.md](docs/BASIC_MODULES_API.md) |
| Run & seed & MinIO | [docs/RUN_AND_SEED.md](docs/RUN_AND_SEED.md) |

**Login example**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🛡 Security Model

1. **Login** validates credentials via `AuthenticationManager` + `BCryptPasswordEncoder` (or a Google ID token).
2. Server issues a short-lived **JWT access token** (HS512) + a long-lived DB-stored **refresh token** (rotated on each refresh).
3. `JwtAuthenticationFilter` validates the token per request and populates the `SecurityContext`.
4. **Authorization**: storefront **reads are public**; **writes require staff** (`@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")`); user management is `SUPERADMIN`-only; cart/checkout require any authenticated user.
5. Auth failures return consistent JSON: **401** (`JwtAuthenticationEntryPoint`) or **403** (`CustomAccessDeniedHandler`).

**Roles:** `SUPERADMIN` › `ADMIN` › `CUSTOMER` (stored on `users.role`).

---

## 📦 Response Format

**Success**
```json
{ "code": 1000, "message": "Order placed", "data": { "...": "..." }, "timestamp": "2026-07-11T02:00:00Z" }
```
**Error**
```json
{ "code": 4105, "message": "Mã giảm giá đã hết hạn", "data": null, "timestamp": "2026-07-11T02:00:00Z" }
```

**Error code ranges**

| Range | Meaning |
|---|---|
| `1000` | Success |
| `4000–4005` | Bad request / validation |
| `401x` | Authentication |
| `4030` | Authorization (403) |
| `404x` | Not found |
| `409x` / `41xx` | Conflict (already exists, coupon not applicable…) |
| `9000–9999` | Server / internal errors |

---

## 📄 License

Released under the **MIT License**.

---

<div align="center">

**Gốm Sứ Vũ Gia** — Built with ❤️ using Spring Boot

</div>
