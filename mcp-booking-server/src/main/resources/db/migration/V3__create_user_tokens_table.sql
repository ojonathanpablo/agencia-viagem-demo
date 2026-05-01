CREATE TABLE user_tokens (
    user_id       VARCHAR(255) PRIMARY KEY,
    access_token  TEXT         NOT NULL,
    refresh_token TEXT,
    expires_at    TIMESTAMP
);
