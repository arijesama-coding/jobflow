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

**Done (Phase 3 — Companies + Job Offers)**
- `Company` and `JobOffer` entities, repositories, MapStruct mappers, DTOs
- `CurrentUserProvider`: single choke point that resolves the authenticated user, used by every service to enforce row-level ownership (returns 404, not 403, on mismatch — no existence leak)
- Spring Specifications for filtering: companies by search/industry/favorite, job offers by search/company/remote type/contract type/favorite/archived — all combined with pagination (`?page=&size=&sort=`)
- Soft delete (`deleted_at`) on both entities instead of hard delete
- Job offers: free-text skills resolved find-or-create against the shared `skills` table; `deadlinePassed` computed on every response; favorite and archive as separate PATCH toggles
- `CompanyServiceTest`: pins down the IDOR protection specifically (get/update/delete all 404 when the company belongs to another user, even for a valid id)
- Angular: `CompanyService` / `JobOfferService`, and real (non-mocked) Companies and Job Offers pages — search, create, edit, delete, favorite toggle, archive toggle, skills as comma-separated tags

**Done (Phase 4 — Applications + status-history tracking)**
- `Application` entity linking to `Company` and `JobOffer` (both optional — a wishlist entry can exist before either is filled in), plus `ApplicationStatusHistory`
- Every status change is recorded: on create (`fromStatus = null`), on `PUT` update if the status field changed, and on the dedicated `PATCH /api/applications/{id}/status` — the same endpoint the Kanban board (Phase 5) will call on drag & drop
- `GET /api/applications/{id}/history` exposes the full transition timeline, ownership-checked like everything else
- Filtering by search, status, priority, company, and application-date range, paginated
- `ApplicationServiceTest`: pins down that history is recorded correctly (initial create, real transitions, no-op when status is unchanged) and that ownership is enforced
- Angular: `ApplicationService` and a real Applications table page (spec section 10) — search, status filter, inline create/edit form linking to existing companies/job offers, and a status dropdown per row that calls the same PATCH endpoint the Kanban board will use next

**Done (Phase 5 — Kanban board)**
- `KanbanBoardComponent` using Angular CDK drag & drop, added as a view toggle inside the Applications page (Kanban is now the default view, Table is one click away — both share the same `ApplicationService`)
- 8 columns exactly as listed in spec section 9 (À postuler → Postulé → Screening → Entretien → Entretien technique → Offre → Accepté → Refusé). **Known gap, not hidden:** `WISHLIST`, `FINAL_INTERVIEW` and `WITHDRAWN` aren't spec-listed columns, so applications in those three statuses don't appear on the board — they're still fully visible and editable from the Table view
- Dragging a card calls `PATCH /api/applications/{id}/status` (the same endpoint built in Phase 4) — optimistic UI update, rolled back if the request fails
- Cards show company, role, priority badge, application date, and next follow-up date per spec

**Done (Phase 6 — Dashboard)**
- `GET /api/dashboard`, aggregated in-memory per user (personal-tracker scale, not multi-tenant analytics — documented as a deliberate simplification, revisit if that assumption stops holding)
- All 8 stat cards from spec section 11: total, active, interviews, offers received, accepted, rejected, response rate, conversion rate
- **Offers received and response/conversion rates use the status-history table from Phase 4**, not just current status — a rejection after reaching OFFER still counts as an offer received, and a rejection after reaching INTERVIEW still counts as a response and shows up in the funnel. This only exists because Phase 4 built history tracking first.
- Charts: applications by month (rolling last 8, not a hardcoded Jan–Aug), status distribution donut, conversion funnel (Application → Screening → Interview → Offer → Accepted), most-targeted companies, most-effective sources (% reaching interview stage per source) — all rendered as dependency-free inline SVG/CSS rather than pulling in a chart library, to avoid an extra untested npm dependency
- `DashboardServiceTest`: specifically pins down that a later rejection doesn't erase prior funnel progress (the one non-obvious rule in this module)

**Done (Phase 7 — Interviews + Calendar)**
- `Interview` entity linked to `Application` (no direct `user_id` — ownership only reachable via `interview.application.user`, which is exactly the kind of indirection that regresses quietly, so it has its own `InterviewServiceTest`)
- Full CRUD + filtering (application, type, result, date range), paginated
- `GET /api/interviews/calendar?from=&to=` — unpaginated, purpose-built for the calendar grid
- Angular: `InterviewService`, a real Interviews list/form page, and a month-grid `CalendarComponent` (prev/next navigation, day selection, dot markers per day)
- **Known, documented gap:** spec section 22 wants the calendar to also show follow-ups, deadlines and tasks — those modules don't exist yet (Phase 8), so the calendar is interviews-only for now. The grid and event-rendering plumbing are built so adding the other event types later is additive, not a rewrite.

**Done (Phase 8 — Follow-ups + Tasks)**
- `FollowUp` entity (ownership via `application.user`, like Interview) and `Task` entity (direct `user_id`, application link optional — a task doesn't have to be tied to one application)
- `GET /api/follow-ups/stats` — due today / overdue / due this week, per spec section 14's mini-dashboard; `FollowUpServiceTest` pins down the date-bucketing boundaries specifically (off-by-one here would silently mislabel an overdue follow-up as merely due this week)
- Full CRUD + filtering + pagination for both, plus `/calendar` endpoints for each
- **Closed the gap flagged in Phase 7:** `CalendarComponent` now merges interviews, follow-ups, and task due dates into one month view with a color-coded legend and per-day event list — this was promised as "additive, not a rewrite" and it was. Job-offer deadlines are still not on the calendar (would need a new date-range endpoint on the jobs API); flagging that honestly rather than calling the calendar spec-complete.
- Angular: `FollowUpService` / `TaskService`, a Follow-ups page with the stats banner, and a Tasks page with a checkbox-style done toggle

**Done (Phase 9 — Documents + Contacts + Notes)**
- **Documents**: real local-disk file storage behind a `FileStorageService` interface (swappable for S3/GCS later without touching callers). Every stored filename is a generated UUID — the user's original filename is kept only as display metadata and never used to build a filesystem path, which is what actually prevents path traversal, not input sanitization. `FileStorageServiceTest` specifically attempts a `../../etc/passwd`-style traversal and asserts it's rejected. Upload/download/rename/delete/set-primary all implemented; light versioning via an optional `replaceDocumentId` on upload; "only one primary per (user, type, application)" enforced on every primary-setting path. Storage location is configurable (`UPLOAD_DIR`) with a persistent Docker volume.
- **Contacts**: CRUD + search/filter, many-to-many link to Applications (`POST`/`DELETE /api/contacts/{id}/applications/{applicationId}`) via the `application_contacts` join table from the Phase 1 schema
- **Notes**: polymorphic (`entity_type` + `entity_id`, no FK) attached to Application, Company, Interview, or Contact. Ownership of the *target* is verified per type before a note can be attached — `NoteServiceTest` covers every branch of that switch individually rather than trusting it's exhaustive by eye, since a forgotten case is exactly the kind of thing that regresses quietly.
- Angular: full Documents page (upload, download, rename, set-primary, delete) and full Contacts page (CRUD + search). **Known, documented gap:** no dedicated Notes UI yet — notes are meant to be embedded in entity detail views (application detail, company detail, etc.) that don't exist yet in this app (list pages only so far), so building a standalone Notes page would mean either faking context or duplicating navigation that doesn't exist. The backend API is fully built and tested; the UI attaches naturally once detail pages exist.

**Still to build, in spec order**
- Angular side of Phase 2: store/refresh the new refresh token in `AuthService`, auto-refresh on 401 via the interceptor, verify-email / forgot-password / reset-password pages (backend endpoints exist, no UI yet)
- Phase 10: Notifications
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
