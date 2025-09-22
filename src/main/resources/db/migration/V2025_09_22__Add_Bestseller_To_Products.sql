ALTER TABLE products ADD COLUMN IF NOT EXISTS isBestseller BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_product_bestseller ON products (isBestseller);

