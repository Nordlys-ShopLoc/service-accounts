-- Module M1 - Accounts and security
-- Covered user stories: C01, C02, T01, T02, S16, A03

-- ---------------------------------------------------------------
-- Platform users (clients, merchants, administrators)
-- ---------------------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL     PRIMARY KEY,
    email         VARCHAR(180)  NOT NULL UNIQUE,
    password_hash VARCHAR(100),                          -- NULL: account created in shop, no application access
    first_name    VARCHAR(80)   NOT NULL,
    last_name     VARCHAR(80)   NOT NULL,
    phone         VARCHAR(20),
    license_plate VARCHAR(16),                           -- US-C15, parking benefit
    role          VARCHAR(20)   NOT NULL,                -- CLIENT | MERCHANT | ADMIN
    status        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_role_valid   CHECK (role   IN ('CLIENT', 'MERCHANT', 'ADMIN')),
    CONSTRAINT users_status_valid CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);

-- ---------------------------------------------------------------
-- Loyalty cards
-- The QR code identifies the card, not the account: the merchant
-- scans it during an in-shop purchase (US-S08).
-- ---------------------------------------------------------------
CREATE TABLE loyalty_cards (
    id             BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT      NOT NULL UNIQUE,
    qr_code        VARCHAR(64) NOT NULL UNIQUE,
    balance_cents  INTEGER     NOT NULL DEFAULT 0,
    issued_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    issued_in_shop BOOLEAN     NOT NULL DEFAULT FALSE,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_card_user       FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT card_status_valid  CHECK (status IN ('ACTIVE', 'BLOCKED')),
    -- RM01: the amount loaded on the card cannot exceed 100 euros
    CONSTRAINT card_balance_range CHECK (balance_cents >= 0 AND balance_cents <= 10000)
);

CREATE INDEX idx_cards_qr_code ON loyalty_cards (qr_code);
