ALTER TABLE approvals
    ADD COLUMN sla_deadline           TIMESTAMP,
    ADD COLUMN is_auto_approved       BOOLEAN DEFAULT FALSE;

CREATE TABLE approval_history (
    id               BIGSERIAL PRIMARY KEY,
    loan_id          BIGINT NOT NULL REFERENCES loans(id),
    changed_by       BIGINT NOT NULL REFERENCES users(id),
    from_status      VARCHAR(30) NOT NULL,
    to_status        VARCHAR(30) NOT NULL,
    reason           VARCHAR(1000),
    is_auto_approved BOOLEAN DEFAULT FALSE,
    changed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);