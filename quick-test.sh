#!/bin/bash
# Copyright 2025, David Snyderman
# Quick test script for PhotoSort application

set -e  # Exit on error

echo "========================================="
echo "PhotoSort Quick Test Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo "1. Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓${NC} Java found: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java not found. Please install Java 17+"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version | head -n 1)
    echo -e "${GREEN}✓${NC} Maven found: $MVN_VERSION"
else
    echo -e "${YELLOW}✗${NC} Maven not found. Installing..."
    echo "Run: sudo apt install maven"
    exit 1
fi

# Check PostgreSQL
if command -v psql &> /dev/null; then
    echo -e "${GREEN}✓${NC} PostgreSQL client found"
else
    echo -e "${RED}✗${NC} PostgreSQL client not found. Please install PostgreSQL"
    exit 1
fi

echo ""
echo "2. Checking environment variables..."

# Check database credentials
if [ -z "$DB_USERNAME" ]; then
    echo -e "${YELLOW}✗${NC} DB_USERNAME not set"
    echo "  Run: export DB_USERNAME=your_postgres_username"
else
    echo -e "${GREEN}✓${NC} DB_USERNAME is set"
fi

if [ -z "$DB_PASSWORD" ]; then
    echo -e "${YELLOW}✗${NC} DB_PASSWORD not set"
    echo "  Run: export DB_PASSWORD=your_postgres_password"
else
    echo -e "${GREEN}✓${NC} DB_PASSWORD is set"
fi

if [ -z "$OAUTH_CLIENT_ID" ]; then
    echo -e "${YELLOW}⚠${NC} OAUTH_CLIENT_ID not set (optional for testing)"
else
    echo -e "${GREEN}✓${NC} OAUTH_CLIENT_ID is set"
fi

echo ""
echo "3. Checking database connectivity..."

if [ -n "$DB_USERNAME" ] && [ -n "$DB_PASSWORD" ]; then
    export PGPASSWORD=$DB_PASSWORD
    if psql -U $DB_USERNAME -d postgres -c '\l' &> /dev/null; then
        echo -e "${GREEN}✓${NC} Can connect to PostgreSQL"

        # Check if databases exist
        if psql -U $DB_USERNAME -lqt | cut -d \| -f 1 | grep -qw PhotoSortData; then
            echo -e "${GREEN}✓${NC} PhotoSortData database exists"
        else
            echo -e "${YELLOW}✗${NC} PhotoSortData database does not exist"
            echo "  Run: sudo -u postgres psql -c 'CREATE DATABASE \"PhotoSortData\";'"
        fi

        if psql -U $DB_USERNAME -lqt | cut -d \| -f 1 | grep -qw PhotoSortDataTest; then
            echo -e "${GREEN}✓${NC} PhotoSortDataTest database exists"
        else
            echo -e "${YELLOW}✗${NC} PhotoSortDataTest database does not exist"
            echo "  Run: sudo -u postgres psql -c 'CREATE DATABASE \"PhotoSortDataTest\";'"
        fi
    else
        echo -e "${RED}✗${NC} Cannot connect to PostgreSQL"
        echo "  Check your credentials and make sure PostgreSQL is running"
    fi
    unset PGPASSWORD
else
    echo -e "${YELLOW}⚠${NC} Skipping database check (credentials not set)"
fi

echo ""
echo "4. Checking project structure..."

cd "$(dirname "$0")"

if [ -f "PhotoSortServices/pom.xml" ]; then
    echo -e "${GREEN}✓${NC} pom.xml found"
else
    echo -e "${RED}✗${NC} pom.xml not found"
    exit 1
fi

if [ -d "PhotoSortServices/src/main/java" ]; then
    echo -e "${GREEN}✓${NC} Source directory found"
else
    echo -e "${RED}✗${NC} Source directory not found"
    exit 1
fi

echo ""
echo "========================================="
echo "Summary"
echo "========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. Set environment variables (if not already set):"
echo "   export DB_USERNAME=your_postgres_username"
echo "   export DB_PASSWORD=your_postgres_password"
echo ""
echo "2. Create databases (if they don't exist):"
echo "   sudo -u postgres psql -c 'CREATE DATABASE \"PhotoSortData\";'"
echo "   sudo -u postgres psql -c 'CREATE DATABASE \"PhotoSortDataTest\";'"
echo ""
echo "3. Run the schema script:"
echo "   cd PhotoSortServices"
echo "   psql -U \$DB_USERNAME -d PhotoSortData -f src/main/resources/schema.sql"
echo ""
echo "4. Build the project:"
echo "   cd PhotoSortServices"
echo "   mvn clean install"
echo ""
echo "5. Run tests:"
echo "   mvn test"
echo ""
echo "See TESTING.md for complete testing guide."
echo ""
