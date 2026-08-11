# JobFlow

JobFlow is a SaaS platform for managing a job search end-to-end: companies, job
offers, applications, a Kanban pipeline, interviews, follow-ups, tasks,
documents, contacts, notifications, and analytics.

> **Status:** Phase 1 scaffold (architecture, database schema, Docker, auth
> skeleton). See [Roadmap](#roadmap) below for what's built vs. still to do.

## Tech stack

**Backend** — Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA /
Hibernate, PostgreSQL, Flyway, Lombok, MapStruct, springdoc-openapi, JUnit 5 +
Mockito + Testcontainers, Maven.

**Frontend** — Angular 21 (standalone components, Signals), TypeScript,
Reactive Forms, Angular CDK, RxJS.

**Infrastructure** — Docker Compose (PostgreSQL, backend, Nginx-served
frontend), Flyway migrations, `.env`-based configuration.

## Architecture

```
                         ┌────────────────────┐
                         │   Angular (Nginx)   │
                         │  core/shared/features│
                         └──────────┬──────────┘
                                    │ HTTPS / JSON
                                    ▼
                         ┌────────────────────┐
                         │   Spring Boot API   │
                         │  controller → service│
                         │  → repository → JPA  │
                         └──────────┬──────────┘
                                    │ JDBC
                                    ▼
                         ┌────────────────────┐
                         │     PostgreSQL      │
                         │   (Flyway-managed)  │
                         └────────────────────┘

Backend layering:
controller/  -> REST endpoints, request/response DTOs only
service/     -> business logic, transactions
repository/  -> Spring Data JPA interfaces
entity/      -> JPA entities (never exposed directly via API)
dto/         -> request/response payloads
mapper/      -> MapStruct entity <-> DTO mapping
security/    -> JWT filter, UserDetailsService
exception/   -> GlobalExceptionHandler, typed exceptions
specification/ -> Spring Specifications for advanced filtering

Frontend layering:
core/      -> auth, guards, interceptors, singleton services, models
shared/    -> reusable components, pipes, directives
features/  -> one folder per domain module (dashboard, applications, ...)
layout/    -> shell, sidebar, navbar
```

## Data model

Tables (see `backend/src/main/resources/db/migration/V1__init_schema.sql`):
`users`, `skills`, `user_skills`, `refresh_tokens`,
`email_verification_tokens`, `password_reset_tokens`, `companies`,
`job_offers`, `job_offer_skills`, `applications`,
`application_status_history`, `interviews`, `contacts`,
`application_contacts`, `follow_ups`, `tasks`, `documents`, `notes`,
`notifications`, `audit_logs`. All user-owned tables carry a `user_id` foreign
key so row-level ownership is enforced at the query layer (service methods
must always filter by the authenticated user's id — this still needs to be
wired into each service per the roadmap below).

## Getting started

```bash
cp .env.example .env
# edit .env with real secrets before anything but local dev

docker compose up --build
```

- Backend: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- Frontend: http://localhost:4200

### Local dev without Docker

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

## Environment variables

See `.env.example` for the full list: `DATABASE_URL`, `DATABASE_USERNAME`,
`DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`,
`REFRESH_TOKEN_EXPIRATION`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`,
`MAIL_PASSWORD`. Never commit `.env`.

## Testing

```bash
cd backend && mvn test        # JUnit 5 + Mockito + Testcontainers
cd frontend && npm test       # Karma/Jasmine (or your configured runner)
```

## Roadmap

**Done (Phase 1 — architecture, DB, Docker, auth skeleton)**
- Full project skeleton (backend layered packages, frontend core/shared/features/layout)
- Complete Flyway schema for every module in the spec
- Docker Compose (postgres / backend / frontend) + multi-stage Dockerfiles
- Spring Security + JWT access-token filter, BCrypt, CORS, GlobalExceptionHandler
- Angular auth service/guard/interceptor, routing for every module, landing/login/register pages, shell/sidebar/navbar layout, placeholder pages for each feature module

**Done (Phase 2 — auth completed)**
- Register / login / refresh / logout all fully implemented, no stubs left
- Refresh-token rotation: each refresh issues a new token and revokes the old one (hashed at rest in `refresh_tokens`, never stored in plaintext)
- Account lockout: configurable max failed attempts and lockout duration (`MAX_FAILED_ATTEMPTS`, `LOCKOUT_DURATION_MINUTES`), with auto-unlock once the window passes
- Email verification and password-reset token issue/consume flow, with expiry (`EMAIL_VERIFICATION_EXPIRATION_MINUTES`, `PASSWORD_RESET_EXPIRATION_MINUTES`); forgot-password never reveals whether an email is registered
- Password reset revokes all of that user's refresh tokens (forces re-login everywhere)
- `EmailService` (welcome, verification, reset, interview reminder, follow-up reminder) wired to `spring-boot-starter-mail`, fails soft (logs, doesn't break the request) if SMTP is unreachable
- `AuditLogService` records REGISTER / LOGIN / LOGIN_FAILED / LOGOUT / TOKEN_REFRESHED / PASSWORD_CHANGED / PASSWORD_RESET_REQUESTED / EMAIL_VERIFIED / ACCOUNT_LOCKED, with the caller's IP, in its own transaction so it survives rollbacks
- Unit tests for the lockout state machine (`AuthServiceTest`): duplicate email, lock after N failures, reject while locked, auto-unlock after the window

**Still to build, in spec order**
- Angular side of Phase 2: store/refresh the new refresh token in `AuthService`, auto-refresh on 401 via the interceptor, verify-email / forgot-password / reset-password pages (backend endpoints exist, no UI yet)
- Phase 3: Companies + Job Offers (entities already in schema — need DTOs, mappers, services, controllers, specifications, Angular feature UIs)
- Phase 4: Applications module + status-history tracking
- Phase 5: Kanban board (drag & drop, `PATCH /api/applications/{id}/status`)
- Phase 6: Dashboard (stats + charts)
- Phase 7: Interviews + Calendar view
- Phase 8: Follow-ups + Tasks
- Phase 9: Documents (secured file storage/download) + Contacts + Notes
- Phase 10: Notifications
- Phase 11: Analytics (response/interview/offer/acceptance rates, trends)
- Phase 12: Admin dashboard + audit log viewer
- Phase 13: Full backend/frontend test suites
- Phase 14: Dark mode, responsive/mobile polish, accessibility pass
- Phase 15: Production build hardening

Each phase should only be marked done once it is implemented, compiled,
tested, integrated with the frontend, functional, and responsive — not
simply once the code is written.

## Security notes

- Passwords are BCrypt-hashed; JWT access tokens are short-lived, refresh
  tokens are meant to be stored hashed and revocable (table exists, logic
  pending — see roadmap).
- Every user-owned entity carries `user_id`; service-layer code must filter
  by the authenticated principal on every read/write to prevent IDOR.
- `.env` is git-ignored; `.env.example` has placeholder values only.
