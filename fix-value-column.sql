-- Rename legacy 'value' column to 'deal_value' and make it nullable
ALTER TABLE deals CHANGE COLUMN value deal_value DECIMAL(10,2) DEFAULT NULL;

