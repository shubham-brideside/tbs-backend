#!/bin/bash

# Development setup script for Brideside Backend
# This script sets up the development environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up development environment for Brideside Backend${NC}"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java is not installed. Please install Java 17 or higher.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Java version $JAVA_VERSION is not supported. Please install Java 17 or higher.${NC}"
    exit 1
fi

echo -e "${GREEN}Java version: $JAVA_VERSION${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven 3.6 or higher.${NC}"
    exit 1
fi

# Check Maven version
MAVEN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
echo -e "${GREEN}Maven version: $MAVEN_VERSION${NC}"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed. Please install Docker.${NC}"
    exit 1
fi

# Check if docker-compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}docker-compose is not installed. Please install docker-compose.${NC}"
    exit 1
fi

# Start development database
echo -e "${YELLOW}Starting development database...${NC}"
docker-compose -f docker-compose.dev.yml up -d

# Wait for database to be ready
echo -e "${YELLOW}Waiting for database to be ready...${NC}"
sleep 15

# Test database connection
if docker exec brideside-mysql-dev mysqladmin ping -h localhost --silent; then
    echo -e "${GREEN}Database is ready!${NC}"
else
    echo -e "${RED}Database failed to start. Check the logs:${NC}"
    docker-compose -f docker-compose.dev.yml logs mysql-dev
    exit 1
fi

# Build the application
echo -e "${YELLOW}Building the application...${NC}"
mvn clean compile

# Run tests
echo -e "${YELLOW}Running tests...${NC}"
mvn test

echo -e "${GREEN}Development environment setup completed!${NC}"
echo -e "${GREEN}Database is running on port 3307${NC}"
echo -e "${GREEN}You can now run the application with: mvn spring-boot:run${NC}"
echo -e "${GREEN}Or with profile: mvn spring-boot:run -Dspring-boot.run.profiles=dev${NC}"
