-- Run against thebrideside if Hibernate ddl-auto is not update
CREATE TABLE IF NOT EXISTS phone_otp_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_digits VARCHAR(20) NOT NULL,
    channel VARCHAR(16) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    consumed TINYINT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    failed_attempts INT NOT NULL,
    INDEX idx_phone_otp_created (phone_digits, created_at),
    INDEX idx_phone_otp_active (phone_digits, consumed, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
