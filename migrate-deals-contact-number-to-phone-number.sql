-- Rename deals.contact_number -> deals.phone_number (matches JPA Deal.contactNumber -> column phone_number).
-- Run once on databases that still have contact_number.

USE thebrideside;

ALTER TABLE deals CHANGE COLUMN contact_number phone_number VARCHAR(20) NOT NULL;

-- Optional (MySQL 8.0+): rename index if you still have idx_contact_number
-- ALTER TABLE deals RENAME INDEX idx_contact_number TO idx_phone_number;
