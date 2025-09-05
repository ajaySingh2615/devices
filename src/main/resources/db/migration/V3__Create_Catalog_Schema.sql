-- V3__Create_Catalog_Schema.sql
-- Create catalog domain tables for DeviceHub

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id CHAR(36) PRIMARY KEY,
    parent_id CHAR(36),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_slug (slug),
    INDEX idx_category_parent (parent_id),
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Brands table
CREATE TABLE IF NOT EXISTS brands (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brand_slug (slug)
);

-- Attributes table
CREATE TABLE IF NOT EXISTS attributes (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    input_type ENUM('TEXT', 'NUMBER', 'SELECT', 'MULTI_SELECT', 'BOOLEAN') NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_filterable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_attribute_code (code)
);

-- Attribute Values table
CREATE TABLE IF NOT EXISTS attribute_values (
    id CHAR(36) PRIMARY KEY,
    attribute_id CHAR(36) NOT NULL,
    value VARCHAR(200) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_attr_value_attr (attribute_id),
    FOREIGN KEY (attribute_id) REFERENCES attributes(id) ON DELETE CASCADE
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id CHAR(36) PRIMARY KEY,
    category_id CHAR(36) NOT NULL,
    brand_id CHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    description TEXT,
    condition_grade ENUM('A', 'B', 'C') NOT NULL,
    warranty_months INT NOT NULL DEFAULT 6,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_slug (slug),
    INDEX idx_product_category (category_id),
    INDEX idx_product_brand (brand_id),
    INDEX idx_product_active (is_active),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE CASCADE
);

-- Product Variants table
CREATE TABLE IF NOT EXISTS product_variants (
    id CHAR(36) PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    mpn VARCHAR(100),
    color VARCHAR(50),
    storage_gb INT,
    ram_gb INT,
    price_mrp DECIMAL(10,2) NOT NULL,
    price_sale DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 18.00,
    weight_grams INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_variant_sku (sku),
    INDEX idx_variant_product (product_id),
    INDEX idx_variant_price (price_sale),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id CHAR(36) PRIMARY KEY,
    variant_id CHAR(36) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 5,
    reserved INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_inventory_variant (variant_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- Media table
CREATE TABLE IF NOT EXISTS media (
    id CHAR(36) PRIMARY KEY,
    owner_type ENUM('PRODUCT', 'VARIANT', 'BRAND', 'CATEGORY', 'USER') NOT NULL,
    owner_id CHAR(36) NOT NULL,
    url VARCHAR(500) NOT NULL,
    type ENUM('IMAGE', 'VIDEO', 'DOCUMENT') NOT NULL,
    alt VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_media_owner (owner_type, owner_id),
    INDEX idx_media_sort (owner_id, sort_order)
);

-- Insert sample categories
INSERT INTO categories (id, name, slug, description, sort_order, created_at, updated_at) VALUES 
('cat-1', 'Laptops', 'laptops', 'Refurbished laptops from top brands', 1, NOW(), NOW()),
('cat-2', 'Mobile Phones', 'mobile-phones', 'Smartphones and feature phones', 2, NOW(), NOW()),
('cat-3', 'Tablets', 'tablets', 'Android and iOS tablets', 3, NOW(), NOW()),
('cat-4', 'Cameras', 'cameras', 'Digital cameras and accessories', 4, NOW(), NOW()),
('cat-5', 'Printers', 'printers', 'Inkjet and laser printers', 5, NOW(), NOW());

-- Insert sample brands
INSERT INTO brands (id, name, slug, description, created_at, updated_at) VALUES 
('brand-1', 'Apple', 'apple', 'Premium devices from Apple Inc.', NOW(), NOW()),
('brand-2', 'Samsung', 'samsung', 'Korean electronics giant', NOW(), NOW()),
('brand-3', 'Dell', 'dell', 'Business and personal computers', NOW(), NOW()),
('brand-4', 'HP', 'hp', 'Hewlett-Packard computers and printers', NOW(), NOW()),
('brand-5', 'Lenovo', 'lenovo', 'ThinkPad and IdeaPad laptops', NOW(), NOW());

-- Insert sample products
INSERT INTO products (id, category_id, brand_id, title, slug, description, condition_grade, warranty_months, created_at, updated_at) VALUES 
('prod-1', 'cat-1', 'brand-1', 'MacBook Air M1 13-inch', 'macbook-air-m1-13', 'Powerful and efficient laptop with M1 chip', 'A', 12, NOW(), NOW()),
('prod-2', 'cat-2', 'brand-1', 'iPhone 12', 'iphone-12', 'Premium smartphone with A14 Bionic chip', 'B', 6, NOW(), NOW()),
('prod-3', 'cat-1', 'brand-3', 'Dell XPS 13', 'dell-xps-13', 'Ultra-portable business laptop', 'A', 6, NOW(), NOW()),
('prod-4', 'cat-2', 'brand-2', 'Samsung Galaxy S21', 'samsung-galaxy-s21', 'Flagship Android smartphone', 'B', 6, NOW(), NOW());

-- Insert sample variants
INSERT INTO product_variants (id, product_id, sku, color, storage_gb, ram_gb, price_mrp, price_sale, created_at, updated_at) VALUES 
('var-1', 'prod-1', 'MBA-M1-256-8', 'Silver', 256, 8, 99900.00, 75000.00, NOW(), NOW()),
('var-2', 'prod-1', 'MBA-M1-512-8', 'Space Gray', 512, 8, 119900.00, 89000.00, NOW(), NOW()),
('var-3', 'prod-2', 'IP12-128-B', 'Blue', 128, 6, 79900.00, 55000.00, NOW(), NOW()),
('var-4', 'prod-2', 'IP12-256-W', 'White', 256, 6, 89900.00, 62000.00, NOW(), NOW()),
('var-5', 'prod-3', 'XPS13-512-16', 'Platinum Silver', 512, 16, 149900.00, 98000.00, NOW(), NOW()),
('var-6', 'prod-4', 'GS21-256-8', 'Phantom Gray', 256, 8, 79900.00, 48000.00, NOW(), NOW());

-- Insert inventory records
INSERT INTO inventory (id, variant_id, quantity, safety_stock, reserved, created_at, updated_at) VALUES 
('inv-1', 'var-1', 15, 5, 2, NOW(), NOW()),
('inv-2', 'var-2', 8, 3, 1, NOW(), NOW()),
('inv-3', 'var-3', 25, 10, 5, NOW(), NOW()),
('inv-4', 'var-4', 12, 5, 3, NOW(), NOW()),
('inv-5', 'var-5', 6, 2, 0, NOW(), NOW()),
('inv-6', 'var-6', 20, 8, 4, NOW(), NOW());
