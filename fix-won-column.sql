-- Fix the won column in deals table
-- Make it nullable so it doesn't require a value on insert
ALTER TABLE deals MODIFY COLUMN won BOOLEAN DEFAULT NULL;

