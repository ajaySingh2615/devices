CREATE TABLE newsletter_subscribers (
  id VARCHAR(36) PRIMARY KEY,
  email VARCHAR(191) NOT NULL,
  source VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_newsletter_email ON newsletter_subscribers (LOWER(email));

