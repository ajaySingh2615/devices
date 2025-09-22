-- Add CPU fields to product_variants
ALTER TABLE product_variants
  ADD COLUMN cpuVendor VARCHAR(20) NULL,
  ADD COLUMN cpuSeries VARCHAR(50) NULL,
  ADD COLUMN cpuGeneration VARCHAR(50) NULL,
  ADD COLUMN cpuModel VARCHAR(80) NULL;

CREATE INDEX idx_variant_cpu_vendor ON product_variants (cpuVendor);
CREATE INDEX idx_variant_cpu_series ON product_variants (cpuSeries);

