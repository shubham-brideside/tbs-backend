-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS reevah;

-- Use the database
USE reevah;

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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Show the table structure
DESCRIBE deals;
