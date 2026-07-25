<div align="center">

# 🏺 Gốm Sứ Vũ Gia — E-Commerce Backend API

**Spring Boot REST API for Gốm Sứ Vũ Gia ceramics storefront — product catalog, cart, order processing, coupons, CMS content, admin dashboard, JWT + RBAC security, local filesystem image storage, and Flyway database migration/seed.**

<br/>

<!-- 🌐 Language badges -->
<a href="README.md"><img src="https://img.shields.io/badge/🇻🇳_Tiếng_Việt-555?style=for-the-badge" alt="Tiếng Việt"/></a>
<a href="README.en.md"><img src="https://img.shields.io/badge/🇬🇧_English-2C5BFF?style=for-the-badge" alt="English"/></a>

<br/><br/>

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Security-JWT%20%2B%20RBAC-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrate%20%2B%20Seed-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
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
- [Project & API Documentation](#-project--api-documentation)
- [Security Model](#-security-model)
- [Response Format](#-response-format)
- [License](#-license)

---

## 🧭 Overview

Backend REST API for **Gốm Sứ Vũ Gia** storefront. Covers the entire e-commerce flow — product catalog, cart, checkout with coupons, shipping methods, VietQR bank transfers, SePay webhooks — alongside CMS content (news, pages, banners, gallery, showrooms, FAQ) and admin dashboard analytics.

Built with **clean layered architecture** and **SOLID principles**: interface-driven services, dynamic JPA Specifications, compile-time DTO mapping (MapStruct), unified response envelopes, and centralized error handling. Schema and seed data are applied **automatically on startup** via **Flyway**.

---

## 🛠 Tech Stack

| Layer | Technology | Version | Description |
|---|---|---|---|
| **Language** | Java | 21 (LTS) | Record, pattern matching, text blocks |
| **Framework** | Spring Boot | 3.5.10 | Auto-configuration & Dependency Injection |
| **Web** | Spring Web (MVC) | — | REST controllers & JSON processing |
| **Persistence** | Spring Data JPA + Hibernate | — | ORM, repositories, Specification API |
| **Database** | MySQL | 8.x | Relational DB (`mysql-connector-j`) |
| **Migration** | Flyway (+ `flyway-mysql`) | — | Auto schema migration + seed data |
| **Security** | Spring Security | — | Authentication, Method Security (`@PreAuthorize`) |
| **Token** | JJWT | 0.12.6 | JWT Access Token (HS512) + Refresh Token Rotation |
| **OAuth** | Google Identity | — | Google Sign-In (ID-token verification) |
| **File Storage** | Local Filesystem | — | Uploaded images stored in `data/`, served via `/files/**` |
| **Email** | Spring Mail + Thymeleaf | — | Asynchronous HTML email templates |
| **Mapping** | MapStruct | 1.6.3 | Compile-time DTO ↔ Entity mapping |
| **Validation** | Jakarta Bean Validation | — | `@Valid` payload constraints |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | — | Interactive docs at `/swagger-ui.html` |
| **Monitoring** | Spring Boot Actuator | — | Health checks at `/actuator/health` |
| **Boilerplate** | Lombok | — | Getter/setter/builder annotations |
| **Build** | Maven (wrapper `./mvnw`) | — | Dependency & package management |
| **Testing** | JUnit 5 + Mockito + Spring Security Test | — | Service & Controller tests |

---

## ✨ Features & Modules

### Core Infrastructure
- 🔐 **JWT + Refresh Rotation**: Stateless HS512 access token; DB-backed refresh token rotation with HTTP-only cookies.
- 👥 **RBAC**: Single role per user (`SUPERADMIN` / `ADMIN` / `CUSTOMER`) protected by `@PreAuthorize`.
- 🧾 **Unified Envelope**: All responses wrapped in `{ code, message, data, timestamp }`; `code=1000` for success.
- 🧯 **Centralized Exception Handling**: `@RestControllerAdvice` mapping exceptions to standard error codes.
- 🕵️ **JPA Auditing**: Automatic `createdAt`, `updatedAt`, `createdBy`, `updatedBy` tracking via `BaseEntity`.
- 🔎 **Safe Search & Pagination**: JPA Specification filters + whitelist sorting (0-based page index).
- 🌱 **Auto Migration & Seed**: Flyway executes schema (`db/migration`) and seed data (`db/seed`) on startup.
- 🖼 **Local File Storage**: Uploaded files stored under `./data` (Docker `/app/data`), served via `/files/**`; `@StorageUrl` converts relative paths to absolute URLs.

### Business Modules
- **Auth / User**: Register, login, refresh token, logout, profile (`/me`), Google OAuth2, password change, user management.
- **Products**: Categories, products, multi-image gallery, status toggle, SEO slugs, safe search.
- **CMS / Content**: News + categories, dynamic pages, banners, showrooms, gallery, FAQs, URL redirects.
- **Marketing**: Coupons (`PERCENT`, `FIXED`, `FREE_SHIP`), newsletter subscriptions, contact request form.
- **Cart**: User cart management with quantity aggregation and price totals.
- **Orders**: Idempotent checkout, price snapshots, atomic coupon redemption, VietQR payment QR generation, SePay webhooks, async HTML order confirmation emails.
- **Dashboard**: Admin KPIs (revenue, orders, customers), daily revenue analytics, top selling products.

---

## 🏗 Architecture

```text
HTTP ─▶ Controller ─▶ Service (interface → impl) ─▶ Repository (+ Specification) ─▶ MySQL
          │                     │
          │ ApiResponse<T>      │ MapStruct mapper
          ▼                     ▼
  GlobalExceptionHandler ◀─ AppException(ErrorCode)
          ▲
  Security Filter Chain (JWT) ─▶ 401 EntryPoint / 403 AccessDeniedHandler

  Order Placed ─▶ @TransactionalEventListener(AFTER_COMMIT) ─▶ @Async Email (Thymeleaf)
```

---

## 📂 Project Structure

```text
src/main/java/vn/springboot
├── Application.java
├── common/                                # BaseEntity, ErrorCode, AppException, ApiResponse, PageResponse
├── config/                                # SecurityConfig, JpaAuditingConfig, LocalStorageConfig, Seed Data
├── controller/                            # 22 REST Controllers
├── dto/{request,response}                # Domain Request/Response DTOs
├── entity/                                # 19 JPA Entities extending BaseEntity
├── event/                                 # OrderPlacedEvent & OrderEmailListener
├── mapper/                                # MapStruct interfaces
├── repository/ (+ specification)          # Spring Data Repositories & Specifications
├── security/                              # JWT Filter, Google OAuth2, CustomUserDetails
└── service/ (+ impl)                      # Business logic contracts & implementations

src/main/resources
├── application.yaml                       # Application properties
├── db/migration/V1__init_db.sql          # Database schema (Flyway)
├── db/seed/V2__seed_db.sql               # Initial seed data (Flyway)
└── templates/email/order-confirmation.html   # HTML Email Template (Thymeleaf)
```

---

## 🚦 Getting Started

### Option A — Docker Compose (Recommended)

```bash
cp .env.example .env
docker compose up -d --build
docker compose logs -f app
```
- API Base Path → **http://localhost:8080**
- Swagger UI → **http://localhost:8080/swagger-ui.html**

### Option B — Local Maven Execution

```bash
DB_URL="jdbc:mysql://localhost:3306/db_vu_gia_fullstack?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false" \
  ./mvnw spring-boot:run
```

### Default Admin Credentials
- **Username**: `admin` | **Password**: `admin123` | **Email**: `admin@gmail.com`

---

## 🔌 Project & API Documentation

### Architecture & Operations Docs

| Topic | Document |
|---|---|
| Project Overview & PDR | [docs/project-overview-pdr.md](docs/project-overview-pdr.md) |
| Codebase Summary | [docs/codebase-summary.md](docs/codebase-summary.md) |
| Coding Standards | [docs/code-standards.md](docs/code-standards.md) |
| Architecture & System Design | [docs/system-architecture.md](docs/system-architecture.md) |
| Project Roadmap | [docs/project-roadmap.md](docs/project-roadmap.md) |
| Deployment Guide | [docs/deployment-guide.md](docs/deployment-guide.md) |
| API Design Guidelines | [docs/design-guidelines.md](docs/design-guidelines.md) |

### Detailed Module API Docs

| Module | Document |
|---|---|
| Auth & User (RBAC) | [docs/AUTH_USER_API.md](docs/AUTH_USER_API.md) |
| Products & Categories | [docs/PRODUCT_API.md](docs/PRODUCT_API.md) |
| News & Categories | [docs/NEWS_API.md](docs/NEWS_API.md) |
| Coupons | [docs/COUPON_API.md](docs/COUPON_API.md) |
| Shopping Cart | [docs/CART_API.md](docs/CART_API.md) |
| Orders & Payments | [docs/ORDER_API.md](docs/ORDER_API.md) |
| Admin Dashboard | [docs/DASHBOARD_API.md](docs/DASHBOARD_API.md) |
| CMS Pages | [docs/PAGE_API.md](docs/PAGE_API.md) |
| Contact Requests | [docs/CONTACT_API.md](docs/CONTACT_API.md) |
| Newsletter | [docs/NEWSLETTER_API.md](docs/NEWSLETTER_API.md) |
| Basic Modules (Banners, FAQs, etc.) | [docs/BASIC_MODULES_API.md](docs/BASIC_MODULES_API.md) |
| File Storage & Image Serving | [docs/FILE_STORAGE_API.md](docs/FILE_STORAGE_API.md) |

---

## 🛡 Security Model

1. **Authentication**: BCrypt password hashing + Google OAuth2 ID token verification.
2. **Tokens**: Short-lived HS512 JWT Access Token + DB Refresh Token with cookie rotation.
3. **Authorization**: Public read endpoints; Write actions require `@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")`.

---

## 📦 Response Format

- **Success**: `{ "code": 1000, "message": "Success", "data": { ... }, "timestamp": "..." }`
- **Error**: `{ "code": 4044, "message": "Product not found", "data": null, "timestamp": "..." }`

---

## 📄 License

Released under the **MIT License**.

<div align="center">

**Gốm Sứ Vũ Gia** — Built with ❤️ using Spring Boot

</div>
