-- Create coupon system tables
-- Coupon table
CREATE TABLE coupons (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2),
    max_discount_amount DECIMAL(10,2),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    usage_limit INT NOT NULL DEFAULT 0,
    per_user_limit INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_coupon_code (code),
    INDEX idx_coupon_active (is_active),
    INDEX idx_coupon_dates (start_at, end_at)
);

-- Coupon usage tracking table
CREATE TABLE coupon_usages (
    id VARCHAR(36) PRIMARY KEY,
    coupon_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36),
    discount_amount DECIMAL(10,2) NOT NULL,
    order_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_coupon_usage_coupon (coupon_id),
    INDEX idx_coupon_usage_user (user_id),
    INDEX idx_coupon_usage_order (order_id)
);

-- Insert sample coupons
INSERT INTO coupons (id, code, name, description, type, value, min_order_amount, max_discount_amount, start_at, end_at, usage_limit, per_user_limit) VALUES
('coupon-001', 'WELCOME10', 'Welcome Discount', 'Get 10% off on your first order', 'PERCENTAGE', 10.00, 1000.00, 500.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1000, 1),
('coupon-002', 'SAVE100', 'Flat ₹100 Off', 'Get ₹100 off on orders above ₹2000', 'FIXED', 100.00, 2000.00, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH), 500, 2),
('coupon-003', 'FLASH20', 'Flash Sale', 'Get 20% off on all orders', 'PERCENTAGE', 20.00, 500.00, 1000.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 0, 1),
('coupon-004', 'NEWUSER', 'New User Special', 'Get ₹200 off for new users', 'FIXED', 200.00, 1500.00, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 200, 1);
