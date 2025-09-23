-- Add Operating System to product_variants
ALTER TABLE product_variants
  ADD COLUMN operatingSystem VARCHAR(20) NULL;

CREATE INDEX idx_variant_os ON product_variants (operatingSystem);

