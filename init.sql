-- Database initialization script for Docker
CREATE DATABASE IF NOT EXISTS thebrideside CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE thebrideside;

-- Create contacts table
CREATE TABLE IF NOT EXISTS `contacts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `contact_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pipedrive_contact_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_contacts_contact_name` (`contact_name`),
  KEY `ix_contacts_id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  `pipedrive_deal_id` varchar(100) DEFAULT NULL,
  `contact_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_name` (`user_name`),
  INDEX `idx_contact_number` (`contact_number`),
  INDEX `idx_category` (`category`),
  INDEX `idx_event_date` (`event_date`),
  INDEX `idx_contact_id` (`contact_id`),
  FOREIGN KEY (`contact_id`) REFERENCES `contacts`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create user for the application
CREATE USER IF NOT EXISTS 'brideside'@'%' IDENTIFIED BY 'Shubham@123';
GRANT ALL PRIVILEGES ON thebrideside.* TO 'brideside'@'%';
FLUSH PRIVILEGES;
