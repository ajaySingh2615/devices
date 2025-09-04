-- URGENT: Phone Number Normalization Cleanup Script
-- This script fixes the mixed phone number formats in the database

-- 1. First, let's see what we have (run this to check current state)
SELECT 
    id,
    name,
    email,
    phone,
    CASE 
        WHEN phone IS NULL THEN 'NULL'
        WHEN phone LIKE '+%' THEN 'International'
        WHEN phone REGEXP '^[0-9]{10}$' THEN '10-digit (needs +91)'
        ELSE 'Other format'
    END as phone_format
FROM users 
WHERE phone IS NOT NULL
ORDER BY phone_format, phone;

-- 2. Normalize 10-digit numbers to +91 format
UPDATE users 
SET phone = CONCAT('+91', phone) 
WHERE phone IS NOT NULL 
  AND phone REGEXP '^[0-9]{10}$' 
  AND phone NOT LIKE '+%';

-- 3. Check for potential duplicates after normalization
SELECT 
    REPLACE(REPLACE(phone, '+91', ''), ' ', '') as clean_phone,
    COUNT(*) as user_count,
    GROUP_CONCAT(id) as user_ids,
    GROUP_CONCAT(email) as emails,
    GROUP_CONCAT(name) as names
FROM users 
WHERE phone IS NOT NULL
GROUP BY REPLACE(REPLACE(phone, '+91', ''), ' ', '')
HAVING user_count > 1;

-- 4. Verify all phones now start with + (should return 0 rows)
SELECT id, name, email, phone 
FROM users 
WHERE phone IS NOT NULL 
  AND phone NOT LIKE '+%';

-- 5. Final check - all phone formats
SELECT 
    CASE 
        WHEN phone IS NULL THEN 'NULL'
        WHEN phone LIKE '+91%' THEN 'India (+91)'
        WHEN phone LIKE '+1%' THEN 'US/CA (+1)'
        WHEN phone LIKE '+%' THEN 'Other International'
        ELSE 'INVALID FORMAT'
    END as phone_type,
    COUNT(*) as count
FROM users 
GROUP BY phone_type
ORDER BY count DESC;
