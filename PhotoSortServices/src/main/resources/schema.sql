-- Copyright 2025, David Snyderman
-- PostgreSQL Database Schema for PhotoSort Application

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS script_execution_log CASCADE;
DROP TABLE IF EXISTS scripts CASCADE;
DROP TABLE IF EXISTS user_column_preferences CASCADE;
DROP TABLE IF EXISTS photo_permissions CASCADE;
DROP TABLE IF EXISTS photo_tags CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS photo_metadata CASCADE;
DROP TABLE IF EXISTS metadata_fields CASCADE;
DROP TABLE IF EXISTS exif_data CASCADE;
DROP TABLE IF EXISTS photos CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    google_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255),
    user_type VARCHAR(20) NOT NULL,
    first_login_date TIMESTAMP NOT NULL,
    last_login_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_google_id ON users(google_id);
CREATE INDEX idx_users_email ON users(email);

-- Photos table
CREATE TABLE photos (
    photo_id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT REFERENCES users(user_id),
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL UNIQUE,
    file_size BIGINT,
    file_created_date TIMESTAMP,
    file_modified_date TIMESTAMP,
    added_to_system_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_public BOOLEAN DEFAULT FALSE,
    image_width INTEGER,
    image_height INTEGER,
    thumbnail_path VARCHAR(1000)
);

CREATE INDEX idx_photos_owner_id ON photos(owner_id);
CREATE INDEX idx_photos_file_name ON photos(file_name);
CREATE INDEX idx_photos_added_to_system_date ON photos(added_to_system_date);

-- EXIF Data table
CREATE TABLE exif_data (
    exif_id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT REFERENCES photos(photo_id) ON DELETE CASCADE,
    date_time_original TIMESTAMP,
    camera_make VARCHAR(100),
    camera_model VARCHAR(100),
    gps_latitude DECIMAL(10, 8),
    gps_longitude DECIMAL(11, 8),
    exposure_time VARCHAR(50),
    f_number VARCHAR(50),
    iso_speed INTEGER,
    focal_length VARCHAR(50),
    orientation INTEGER
);

CREATE INDEX idx_exif_data_photo_id ON exif_data(photo_id);

-- Metadata Fields table
CREATE TABLE metadata_fields (
    field_id BIGSERIAL PRIMARY KEY,
    field_name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_metadata_fields_field_name ON metadata_fields(field_name);

-- Photo Metadata table
CREATE TABLE photo_metadata (
    metadata_id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT REFERENCES photos(photo_id) ON DELETE CASCADE,
    field_id BIGINT REFERENCES metadata_fields(field_id),
    metadata_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(photo_id, field_id)
);

CREATE INDEX idx_photo_metadata_photo_id ON photo_metadata(photo_id);
CREATE INDEX idx_photo_metadata_field_id ON photo_metadata(field_id);

-- Tags table
CREATE TABLE tags (
    tag_id BIGSERIAL PRIMARY KEY,
    tag_value VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tags_tag_value ON tags(tag_value);

-- Photo Tags table (junction table)
CREATE TABLE photo_tags (
    photo_tag_id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT REFERENCES photos(photo_id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(tag_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(photo_id, tag_id)
);

CREATE INDEX idx_photo_tags_photo_id ON photo_tags(photo_id);
CREATE INDEX idx_photo_tags_tag_id ON photo_tags(tag_id);

-- Photo Permissions table
CREATE TABLE photo_permissions (
    permission_id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT REFERENCES photos(photo_id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(photo_id, user_id)
);

CREATE INDEX idx_photo_permissions_photo_id ON photo_permissions(photo_id);
CREATE INDEX idx_photo_permissions_user_id ON photo_permissions(user_id);

-- User Column Preferences table
CREATE TABLE user_column_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    column_type VARCHAR(50) NOT NULL,
    column_name VARCHAR(100),
    display_order INTEGER NOT NULL,
    UNIQUE(user_id, column_type, column_name)
);

CREATE INDEX idx_user_column_preferences_user_id ON user_column_preferences(user_id);

-- Scripts table
CREATE TABLE scripts (
    script_id BIGSERIAL PRIMARY KEY,
    script_name VARCHAR(100) UNIQUE NOT NULL,
    script_file_name VARCHAR(255),
    script_contents TEXT,
    run_time TIME,
    periodicity_minutes INTEGER,
    file_extension VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scripts_file_extension ON scripts(file_extension);

-- Script Execution Log table
CREATE TABLE script_execution_log (
    log_id BIGSERIAL PRIMARY KEY,
    script_id BIGINT REFERENCES scripts(script_id),
    photo_id BIGINT REFERENCES photos(photo_id),
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20),
    error_message TEXT
);

CREATE INDEX idx_script_execution_log_script_id ON script_execution_log(script_id);
CREATE INDEX idx_script_execution_log_execution_time ON script_execution_log(execution_time);
