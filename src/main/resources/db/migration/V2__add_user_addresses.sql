CREATE TABLE user_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(50),
    address_line TEXT NOT NULL,
    city VARCHAR(100),
    zip_code VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);
