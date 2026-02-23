-- =====================
-- TABLE 1: users
-- =====================
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255),
    role          VARCHAR(20)  NOT NULL,
    auth_provider VARCHAR(20)  NOT NULL,
    credit_score  INTEGER      NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 2: loans
-- =====================
CREATE TABLE loans
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT         NOT NULL REFERENCES users (id),
    loan_amount        NUMERIC(15, 2) NOT NULL,
    loan_type          VARCHAR(30)    NOT NULL,
    interest_rate      NUMERIC(5, 2)  NOT NULL,
    processing_fee     NUMERIC(10, 2) NOT NULL,
    tenure_months      INTEGER        NOT NULL,
    collateral_details TEXT,
    status             VARCHAR(30)    NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 3: approvals
-- =====================
CREATE TABLE approvals
(
    id          BIGSERIAL PRIMARY KEY,
    loan_id     BIGINT      NOT NULL UNIQUE REFERENCES loans (id),
    approved_by BIGINT      NOT NULL REFERENCES users (id),
    decision    VARCHAR(20) NOT NULL,
    remarks     TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 4: emi_schedule
-- =====================
CREATE TABLE emi_schedule
(
    id         BIGSERIAL PRIMARY KEY,
    loan_id    BIGINT         NOT NULL REFERENCES loans (id),
    emi_amount NUMERIC(15, 2) NOT NULL,
    due_date   DATE           NOT NULL,
    paid_date  DATE,
    status     VARCHAR(20)    NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 5: payments
-- =====================
CREATE TABLE payments
(
    id           BIGSERIAL PRIMARY KEY,
    emi_id       BIGINT         NOT NULL UNIQUE REFERENCES emi_schedule (id),
    amount       NUMERIC(15, 2) NOT NULL,
    payment_date DATE           NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 6: notifications
-- =====================
CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id),
    type       VARCHAR(50) NOT NULL,
    message    TEXT        NOT NULL,
    is_read    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 7: audit_logs
-- =====================
CREATE TABLE audit_logs
(
    id           BIGSERIAL PRIMARY KEY,
    action       VARCHAR(100) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    entity_name  VARCHAR(100) NOT NULL,
    entity_id    BIGINT       NOT NULL,
    details      VARCHAR(1000),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- TABLE 8: refresh_tokens
-- =====================
CREATE TABLE refresh_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE REFERENCES users (id),
    token       VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
