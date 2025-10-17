-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS thebrideside;

-- Use the database
USE thebrideside;

-- Create the contacts table
CREATE TABLE IF NOT EXISTS contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contact_name VARCHAR(255) DEFAULT NULL,
    pipedrive_contact_id VARCHAR(100) DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY ix_contacts_contact_name (contact_name),
    KEY ix_contacts_id (id)
);

-- Create the deals table
CREATE TABLE IF NOT EXISTS deals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    event_date DATE,
    venue VARCHAR(255),
    budget DECIMAL(10,2),
    expected_gathering INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pipedrive_deal_id VARCHAR(100) DEFAULT NULL,
    contact_id INT DEFAULT NULL,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE SET NULL
);

-- Show the table structure
DESCRIBE deals;
