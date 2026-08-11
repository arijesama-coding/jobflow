-- =========================================================
-- JobFlow — Initial schema
-- =========================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================== USERS =====================
CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    first_name              VARCHAR(100),
    last_name               VARCHAR(100),
    phone                   VARCHAR(50),
    location                VARCHAR(255),
    photo_url               VARCHAR(500),
    professional_title      VARCHAR(255),
    summary                 TEXT,
    years_experience        INT,
    linkedin_url            VARCHAR(500),
    github_url              VARCHAR(500),
    portfolio_url           VARCHAR(500),
    availability            VARCHAR(50),
    salary_min              NUMERIC(12,2),
    salary_max              NUMERIC(12,2),
    contract_type           VARCHAR(30),
    remote_preference        VARCHAR(30),
    target_location          VARCHAR(255),
    role                    VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_email_verified        BOOLEAN NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts    INT NOT NULL DEFAULT 0,
    locked_until             TIMESTAMP,
    theme_preference         VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE skills (
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_skills (
    user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE email_verification_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== COMPANIES =====================
CREATE TABLE companies (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    logo_url      VARCHAR(500),
    website       VARCHAR(500),
    industry      VARCHAR(255),
    location      VARCHAR(255),
    description   TEXT,
    size          VARCHAR(50),
    linkedin_url  VARCHAR(500),
    notes         TEXT,
    is_favorite   BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== JOB OFFERS =====================
CREATE TABLE job_offers (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id         UUID REFERENCES companies(id) ON DELETE SET NULL,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    location           VARCHAR(255),
    remote_type        VARCHAR(30),
    contract_type      VARCHAR(30),
    salary_min         NUMERIC(12,2),
    salary_max         NUMERIC(12,2),
    currency           VARCHAR(10) DEFAULT 'EUR',
    job_url            VARCHAR(500),
    source             VARCHAR(50),
    publication_date   DATE,
    deadline           DATE,
    notes              TEXT,
    is_favorite        BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived        BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at         TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE job_offer_skills (
    job_offer_id UUID NOT NULL REFERENCES job_offers(id) ON DELETE CASCADE,
    skill_id     UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (job_offer_id, skill_id)
);

-- ===================== APPLICATIONS =====================
CREATE TABLE applications (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id               UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id            UUID REFERENCES companies(id) ON DELETE SET NULL,
    job_offer_id          UUID REFERENCES job_offers(id) ON DELETE SET NULL,
    status                VARCHAR(30) NOT NULL DEFAULT 'WISHLIST',
    application_date      DATE,
    salary_expectation    NUMERIC(12,2),
    source                VARCHAR(50),
    priority              VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    cover_letter_doc_id   UUID,
    cv_doc_id             UUID,
    notes                 TEXT,
    next_follow_up_date   DATE,
    deleted_at            TIMESTAMP,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE application_status_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id  UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    from_status     VARCHAR(30),
    to_status       VARCHAR(30) NOT NULL,
    changed_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== INTERVIEWS =====================
CREATE TABLE interviews (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id  UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    type            VARCHAR(30) NOT NULL,
    scheduled_at    TIMESTAMP NOT NULL,
    duration_minutes INT,
    location        VARCHAR(255),
    meeting_url     VARCHAR(500),
    interviewer     VARCHAR(255),
    notes           TEXT,
    feedback        TEXT,
    result          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== CONTACTS =====================
CREATE TABLE contacts (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id  UUID REFERENCES companies(id) ON DELETE SET NULL,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(50),
    position    VARCHAR(255),
    linkedin_url VARCHAR(500),
    notes       TEXT,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE application_contacts (
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    contact_id     UUID NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    PRIMARY KEY (application_id, contact_id)
);

-- ===================== FOLLOW-UPS =====================
CREATE TABLE follow_ups (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id  UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    follow_up_date  DATE NOT NULL,
    type            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== TASKS =====================
CREATE TABLE tasks (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id  UUID REFERENCES applications(id) ON DELETE SET NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    due_date        DATE,
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20) NOT NULL DEFAULT 'TODO',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== DOCUMENTS =====================
CREATE TABLE documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id  UUID REFERENCES applications(id) ON DELETE SET NULL,
    type            VARCHAR(30) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    storage_path    VARCHAR(500) NOT NULL,
    version         INT NOT NULL DEFAULT 1,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== NOTES =====================
CREATE TABLE notes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type     VARCHAR(30) NOT NULL, -- APPLICATION, COMPANY, INTERVIEW, CONTACT
    entity_id       UUID NOT NULL,
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== NOTIFICATIONS =====================
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(40) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    message     TEXT,
    entity_type VARCHAR(30),
    entity_id   UUID,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== AUDIT LOG =====================
CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    action      VARCHAR(50) NOT NULL,
    entity      VARCHAR(50),
    entity_id   UUID,
    ip_address  VARCHAR(64),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- ===================== INDEXES =====================
CREATE INDEX idx_companies_user ON companies(user_id);
CREATE INDEX idx_job_offers_user ON job_offers(user_id);
CREATE INDEX idx_job_offers_company ON job_offers(company_id);
CREATE INDEX idx_applications_user ON applications(user_id);
CREATE INDEX idx_applications_company ON applications(company_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_priority ON applications(priority);
CREATE INDEX idx_interviews_application ON interviews(application_id);
CREATE INDEX idx_interviews_scheduled_at ON interviews(scheduled_at);
CREATE INDEX idx_contacts_user ON contacts(user_id);
CREATE INDEX idx_follow_ups_application ON follow_ups(application_id);
CREATE INDEX idx_follow_ups_date ON follow_ups(follow_up_date);
CREATE INDEX idx_tasks_user ON tasks(user_id);
CREATE INDEX idx_documents_user ON documents(user_id);
CREATE INDEX idx_notes_entity ON notes(entity_type, entity_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
