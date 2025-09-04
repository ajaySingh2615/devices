-- Migration to normalize existing phone numbers and handle duplicates
-- This ensures all phone numbers follow the +countrycode format

-- First, let's identify duplicate phone numbers (with and without +91)
-- We'll keep the user with email (more complete profile) and merge the phone-only user

-- Step 1: Update phone numbers that are 10 digits to add +91 prefix
UPDATE users 
SET phone = CONCAT('+91', phone) 
WHERE phone IS NOT NULL 
  AND phone REGEXP '^[0-9]{10}$'
  AND phone NOT LIKE '+%';

-- Step 2: Handle potential duplicates after normalization
-- This query finds users that might be duplicates after phone normalization
-- Manual review may be needed for production data

-- Example: If we had both:
-- User A: email='user@example.com', phone='8808319836' (now +918808319836)
-- User B: email=null, phone='+918808319836'
-- We would merge User B into User A and delete User B

-- For now, we'll add a comment for manual review
-- INSERT INTO migration_log (action, description, created_at) 
-- VALUES ('PHONE_NORMALIZATION', 'Normalized phone numbers to international format', NOW());

-- Note: In production, you would need to:
-- 1. Export duplicate user data for review
-- 2. Merge user profiles carefully
-- 3. Update foreign key references
-- 4. Delete duplicate entries

-- This is a complex operation that should be done with careful data backup and validation
