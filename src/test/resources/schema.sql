CREATE TABLE IF NOT EXISTS deals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
