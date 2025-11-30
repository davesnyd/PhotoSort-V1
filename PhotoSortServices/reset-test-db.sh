#!/bin/bash
# Reset test database before running tests

echo "Resetting test database..."

if [ -z "$DB_USERNAME" ]; then
    echo "Error: DB_USERNAME not set"
    exit 1
fi

# Drop and recreate schema
psql -U $DB_USERNAME -d PhotoSortDataTest << 'EOF'
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO PUBLIC;
EOF

# Recreate tables
psql -U $DB_USERNAME -d PhotoSortDataTest -f src/main/resources/schema.sql

echo "Test database reset complete!"
