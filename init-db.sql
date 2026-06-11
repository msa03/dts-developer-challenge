-- HMCTS Task Management Database Initialization
-- This file is executed automatically by PostgreSQL on container startup

-- Create the tasks_db database (if not already created by environment variables)
-- Note: The database is already created via POSTGRES_DB environment variable

-- Ensure UTF-8 encoding for the database
ALTER DATABASE tasks_db SET client_encoding = 'UTF8';
ALTER DATABASE tasks_db SET default_transaction_isolation = 'read committed';
ALTER DATABASE tasks_db SET timezone = 'UTC';
