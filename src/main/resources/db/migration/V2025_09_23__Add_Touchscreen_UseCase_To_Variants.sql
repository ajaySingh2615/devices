-- Add Touchscreen and UseCase to product_variants
ALTER TABLE product_variants
  ADD COLUMN touchscreen BOOLEAN NULL,
  ADD COLUMN useCase VARCHAR(32) NULL;

CREATE INDEX idx_variant_touchscreen ON product_variants (touchscreen);
CREATE INDEX idx_variant_use_case ON product_variants (useCase);

