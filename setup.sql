-- Create database
CREATE DATABASE IF NOT EXISTS doc_admission_db;
USE doc_admission_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert test user
INSERT IGNORE INTO users (username, password, email) VALUES ('admin', 'admin123', 'admin@example.com');

-- Show tables and data
SELECT * FROM users;
