CREATE TABLE dashboard_statistics
(
    id            UUID           NOT NULL,
    total_users   BIGINT         NOT NULL,
    active_orders BIGINT         NOT NULL,
    total_revenue DECIMAL(12, 2) NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_dashboard_statistics PRIMARY KEY (id)
);

CREATE TABLE dashboard_statistics_snapshots
(
    id            UUID           NOT NULL,
    snapshot_date date           NOT NULL,
    total_users   BIGINT         NOT NULL,
    active_orders BIGINT         NOT NULL,
    total_revenue DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_dashboard_statistics_snapshots PRIMARY KEY (id)
);

CREATE TABLE store_settings
(
    id                  UUID    NOT NULL,
    is_accepting_orders BOOLEAN NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_store_settings PRIMARY KEY (id)
);

CREATE TABLE users
(
    id             UUID         NOT NULL,
    username       VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    first_name     VARCHAR(50),
    last_name      VARCHAR(50),
    phone          VARCHAR(15),
    address        TEXT,
    role           VARCHAR(255) NOT NULL,
    status         VARCHAR(255) NOT NULL,
    email_verified BOOLEAN,
    login_attempts INTEGER,
    locked_until   TIMESTAMP WITHOUT TIME ZONE,
    last_login     TIMESTAMP WITHOUT TIME ZONE,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

CREATE INDEX idx_user_email ON users (email);

CREATE INDEX idx_user_username ON users (username);
