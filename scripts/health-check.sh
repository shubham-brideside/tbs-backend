#!/bin/bash

# Health check script for Brideside Backend
# This script checks the health of all services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Checking health of Brideside Backend services...${NC}"

# Check if Docker containers are running
echo -e "${YELLOW}Checking Docker containers...${NC}"
if docker ps | grep -q brideside-backend; then
    echo -e "${GREEN}✓ Brideside Backend container is running${NC}"
else
    echo -e "${RED}✗ Brideside Backend container is not running${NC}"
fi

if docker ps | grep -q brideside-mysql; then
    echo -e "${GREEN}✓ MySQL container is running${NC}"
else
    echo -e "${RED}✗ MySQL container is not running${NC}"
fi

# Check application health endpoint
echo -e "${YELLOW}Checking application health...${NC}"
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Application health check passed${NC}"
else
    echo -e "${RED}✗ Application health check failed${NC}"
fi

# Check database connectivity
echo -e "${YELLOW}Checking database connectivity...${NC}"
if docker exec brideside-mysql mysqladmin ping -h localhost --silent; then
    echo -e "${GREEN}✓ Database is accessible${NC}"
else
    echo -e "${RED}✗ Database is not accessible${NC}"
fi

# Check Prometheus
echo -e "${YELLOW}Checking Prometheus...${NC}"
if curl -f http://localhost:9090/-/healthy > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Prometheus is running${NC}"
else
    echo -e "${RED}✗ Prometheus is not running${NC}"
fi

# Check Grafana
echo -e "${YELLOW}Checking Grafana...${NC}"
if curl -f http://localhost:3000/api/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Grafana is running${NC}"
else
    echo -e "${RED}✗ Grafana is not running${NC}"
fi

echo -e "${GREEN}Health check completed!${NC}"
