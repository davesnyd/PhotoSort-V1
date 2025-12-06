-- PhotoSort Database Initialization Script
-- Copyright 2025, David Snyderman
--
-- This script runs automatically when the PostgreSQL container starts
-- It grants all necessary permissions to the PhotoSort user

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;

-- Connect to the PhotoSortData database
\c PhotoSortData

-- Grant privileges on all tables in the public schema
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO photosort_user;

-- Grant privileges on all sequences in the public schema
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO photosort_user;

-- Grant privileges on the schema itself
GRANT ALL PRIVILEGES ON SCHEMA public TO photosort_user;

-- Set default privileges for future tables and sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO photosort_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO photosort_user;
