-- Migration script to drop user_name column from deals table
-- Run this script on your database to update the schema

-- Step 1: Copy data from user_name to name if name column exists but is empty
-- (Skip this step if name column already has data or doesn't exist)
-- UPDATE deals SET name = user_name WHERE (name IS NULL OR name = '') AND user_name IS NOT NULL;

-- Step 2: Drop the index on user_name if it exists
-- Note: This will fail silently if the index doesn't exist, which is fine
ALTER TABLE deals DROP INDEX idx_user_name;

-- Step 3: Drop the user_name column
ALTER TABLE deals DROP COLUMN user_name;

-- Step 4: Create index on name column
-- (Only if name column exists and index doesn't already exist)
CREATE INDEX idx_name ON deals(name);

