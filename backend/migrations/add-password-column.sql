-- Migration: Add password column to users table
-- Run this SQL script directly on your PostgreSQL database

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS password VARCHAR(255) NULL;

-- Optional: Add index on email if not already exists (for faster lookups)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
