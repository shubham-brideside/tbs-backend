-- Database initialization script for Docker
CREATE DATABASE IF NOT EXISTS reevah CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE reevah;

-- Create deals table
CREATE TABLE IF NOT EXISTS `deals` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  `category` varchar(50) NOT NULL,
  `event_date` date DEFAULT NULL,
  `venue` varchar(255) DEFAULT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `expected_gathering` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_name` (`user_name`),
  INDEX `idx_contact_number` (`contact_number`),
  INDEX `idx_category` (`category`),
  INDEX `idx_event_date` (`event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create user for the application
CREATE USER IF NOT EXISTS 'brideside'@'%' IDENTIFIED BY 'Shubham@123';
GRANT ALL PRIVILEGES ON reevah.* TO 'brideside'@'%';
FLUSH PRIVILEGES;
