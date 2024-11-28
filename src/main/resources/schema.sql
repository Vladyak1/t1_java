CREATE TYPE account_status AS ENUM ('ARRESTED', 'BLOCKED', 'CLOSED', 'OPEN');
CREATE TYPE account_type AS ENUM ('DEBIT', 'CREDIT');
CREATE TYPE role_enum AS ENUM ('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN');
CREATE TYPE transaction_status AS ENUM ('ACCEPTED', 'REJECTED', 'BLOCKED', 'CANCELLED', 'REQUESTED');

CREATE TABLE IF NOT EXISTS client (
    client_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    blocked_for BOOLEAN,
    blocked_whom VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(120) NOT NULL,
    CONSTRAINT uk_users_login UNIQUE (login),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    name role_enum NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS account (
    account_id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    account_type account_type NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    status account_status NOT NULL DEFAULT 'OPEN',
    frozen_amount NUMERIC(19,2) NOT NULL DEFAULT 0.0,
    FOREIGN KEY (client_id) REFERENCES client(client_id),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_frozen_amount_non_negative CHECK (frozen_amount >= 0),
    CONSTRAINT chk_frozen_amount_less_than_balance CHECK (frozen_amount <= balance)
);

CREATE TABLE IF NOT EXISTS transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    client_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    status transaction_status NOT NULL DEFAULT 'REQUESTED',
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_transaction_id UNIQUE (transaction_id),
    FOREIGN KEY (client_id) REFERENCES client(client_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE IF NOT EXISTS data_source_error_log (
    id BIGSERIAL PRIMARY KEY,
    stack_trace TEXT,
    message TEXT,
    method_signature VARCHAR(255)
);

CREATE INDEX idx_account_client_id ON account(client_id);
CREATE INDEX idx_account_status ON account(status);
CREATE INDEX idx_transaction_client_id ON transaction(client_id);
CREATE INDEX idx_transaction_account_id ON transaction(account_id);
CREATE INDEX idx_transaction_status ON transaction(status);
CREATE INDEX idx_transaction_timestamp ON transaction(timestamp);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);