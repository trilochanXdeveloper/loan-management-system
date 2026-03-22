ALTER TABLE users
    ADD COLUMN failed_login_attempts INT       DEFAULT 0,
    ADD COLUMN account_locked_until  TIMESTAMP DEFAULT NULL,
    ADD COLUMN last_login_at         TIMESTAMP DEFAULT NULL;