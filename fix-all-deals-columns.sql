-- Fix all missing columns in deals table
-- Run this script to rename legacy columns and adjust nullability

-- Rename 'value' to 'deal_value' and allow nulls
ALTER TABLE deals CHANGE COLUMN value deal_value DECIMAL(10,2) DEFAULT NULL;

-- Drop legacy 'won' column if it exists
ALTER TABLE deals DROP COLUMN won;

