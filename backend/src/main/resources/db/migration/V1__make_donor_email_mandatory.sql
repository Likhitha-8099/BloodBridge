-- ==============================================================================
-- Migration Script: Make Email Mandatory and Unique in Donor Module
-- Description: Safely populates NULL emails from users table and adds constraints
-- ==============================================================================

-- Step 1: Safely backfill any existing NULL email values in donor_profiles from associated users
UPDATE donor_profiles dp
JOIN users u ON dp.user_id = u.id
SET dp.email = u.email
WHERE dp.email IS NULL OR dp.email = '';

-- Step 2: Ensure all donor_profiles have non-null email (fallback for orphaned records if any)
UPDATE donor_profiles
SET email = CONCAT('donor_', id, '@bloodbridge.org')
WHERE email IS NULL OR email = '';

-- Step 3: Modify email column to NOT NULL
ALTER TABLE donor_profiles MODIFY COLUMN email VARCHAR(255) NOT NULL;

-- Step 4: Add UNIQUE constraint on email column if not already present
ALTER TABLE donor_profiles ADD CONSTRAINT uk_donor_profiles_email UNIQUE (email);
