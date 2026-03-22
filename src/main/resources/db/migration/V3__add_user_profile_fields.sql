-- Add phone and date_of_birth to users
ALTER TABLE users
    ADD COLUMN phone VARCHAR(15),
    ADD COLUMN date_of_birth DATE;

-- Create address table
CREATE TABLE address
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id),
    street       VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100) NOT NULL,
    pincode      VARCHAR(10)  NOT NULL,
    address_type VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- One user can have only ONE of each address type
    CONSTRAINT unique_user_address_type
        UNIQUE (user_id, address_type)
);