CREATE TABLE IF NOT EXISTS email_suppressions (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(191) NOT NULL UNIQUE,
    reason VARCHAR(64) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_email_suppressions_email ON email_suppressions(email);

