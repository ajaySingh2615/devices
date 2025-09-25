-- Add first_name, last_name, gender to users
ALTER TABLE users ADD COLUMN first_name VARCHAR(80);
ALTER TABLE users ADD COLUMN last_name VARCHAR(80);
ALTER TABLE users ADD COLUMN gender VARCHAR(16);

-- Verification timestamps
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN phone_verified_at TIMESTAMP NULL;

-- Best-effort split of existing name into first and last
-- For Postgres: split on first space
UPDATE users SET first_name = split_part(name, ' ', 1),
                 last_name = NULLIF(BTRIM(SUBSTRING(name FROM POSITION(' ' IN name)+1)), '');


