#!/bin/bash

# Backup script for Brideside Backend database
# This script creates a backup of the MySQL database

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-reevah}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD:-Shubham@123}
BACKUP_DIR=${BACKUP_DIR:-./backups}
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/reevah_backup_$DATE.sql"

echo -e "${GREEN}Starting database backup...${NC}"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Create backup
echo -e "${YELLOW}Creating backup: $BACKUP_FILE${NC}"
docker exec brideside-mysql mysqldump -h localhost -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Backup created successfully: $BACKUP_FILE${NC}"
    
    # Compress the backup
    echo -e "${YELLOW}Compressing backup...${NC}"
    gzip "$BACKUP_FILE"
    
    echo -e "${GREEN}Backup compressed: $BACKUP_FILE.gz${NC}"
    
    # Keep only last 7 days of backups
    echo -e "${YELLOW}Cleaning old backups (keeping last 7 days)...${NC}"
    find "$BACKUP_DIR" -name "reevah_backup_*.sql.gz" -mtime +7 -delete
    
    echo -e "${GREEN}Backup completed successfully!${NC}"
else
    echo -e "${RED}Backup failed!${NC}"
    exit 1
fi
