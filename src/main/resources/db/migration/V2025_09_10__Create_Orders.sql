-- Create orders table
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    status ENUM('CREATED', 'PAID', 'PACKED', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'RETURNED') NOT NULL DEFAULT 'CREATED',
    subtotal DECIMAL(10,2) NOT NULL,
    discount_total DECIMAL(10,2) DEFAULT 0.00,
    tax_total DECIMAL(10,2) NOT NULL,
    shipping_total DECIMAL(10,2) NOT NULL,
    grand_total DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_method ENUM('RAZORPAY', 'COD', 'STRIPE', 'PAYPAL') DEFAULT 'RAZORPAY',
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    razorpay_signature TEXT,
    cod_flag BOOLEAN DEFAULT FALSE,
    applied_coupon_id VARCHAR(36),
    order_notes TEXT,
    delivery_instructions TEXT,
    estimated_delivery_date TIMESTAMP NULL,
    actual_delivery_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_created_at (created_at),
    INDEX idx_orders_razorpay_order_id (razorpay_order_id),
    INDEX idx_orders_razorpay_payment_id (razorpay_payment_id),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (applied_coupon_id) REFERENCES coupons(id) ON DELETE SET NULL
);

-- Create order_items table
CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    product_snapshot JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_variant_id (variant_id),
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE RESTRICT
);

-- Create order_addresses table
CREATE TABLE order_addresses (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    type ENUM('BILLING', 'SHIPPING') NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    line1 VARCHAR(255) NOT NULL,
    line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_order_addresses_order_id (order_id),
    INDEX idx_order_addresses_type (type),
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
