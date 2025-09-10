CREATE TABLE IF NOT EXISTS addresses (
  id CHAR(36) PRIMARY KEY,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  user_id CHAR(36) NOT NULL,
  name VARCHAR(120) NOT NULL,
  phone VARCHAR(20),
  line1 VARCHAR(255) NOT NULL,
  line2 VARCHAR(255),
  city VARCHAR(120) NOT NULL,
  state VARCHAR(120) NOT NULL,
  country VARCHAR(120) NOT NULL,
  pincode VARCHAR(10) NOT NULL,
  is_default BIT(1) NOT NULL,
  CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_addresses_user ON addresses(user_id);
CREATE INDEX idx_addresses_default ON addresses(is_default);


