-- Simple script to drop user_name column from deals table
-- Make sure the name column exists and has data before running this

-- Drop the index on user_name (if it exists)
-- Note: This will fail silently if the index doesn't exist, which is fine
ALTER TABLE deals DROP INDEX idx_user_name;

-- Drop the user_name column
ALTER TABLE deals DROP COLUMN user_name;

