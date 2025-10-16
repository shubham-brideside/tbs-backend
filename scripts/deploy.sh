#!/bin/bash

# Production deployment script for Brideside Backend
# This script builds and deploys the application to production

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="brideside-backend"
DOCKER_IMAGE="brideside-backend"
DOCKER_TAG="latest"
CONTAINER_NAME="brideside-backend"

echo -e "${GREEN}Starting production deployment for $APP_NAME${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}docker-compose is not installed. Please install docker-compose and try again.${NC}"
    exit 1
fi

# Build the application
echo -e "${YELLOW}Building the application...${NC}"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Please fix the issues and try again.${NC}"
    exit 1
fi

# Build Docker image
echo -e "${YELLOW}Building Docker image...${NC}"
docker build -t $DOCKER_IMAGE:$DOCKER_TAG .

if [ $? -ne 0 ]; then
    echo -e "${RED}Docker build failed. Please fix the issues and try again.${NC}"
    exit 1
fi

# Stop existing containers
echo -e "${YELLOW}Stopping existing containers...${NC}"
docker-compose down || true

# Start the application stack
echo -e "${YELLOW}Starting the application stack...${NC}"
docker-compose up -d

# Wait for services to be healthy
echo -e "${YELLOW}Waiting for services to be healthy...${NC}"
sleep 30

# Check if the application is running
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}Application is running successfully!${NC}"
    echo -e "${GREEN}API Documentation: http://localhost:8080/swagger-ui.html${NC}"
    echo -e "${GREEN}Health Check: http://localhost:8080/actuator/health${NC}"
    echo -e "${GREEN}Metrics: http://localhost:8080/actuator/prometheus${NC}"
    echo -e "${GREEN}Prometheus: http://localhost:9090${NC}"
    echo -e "${GREEN}Grafana: http://localhost:3000${NC}"
else
    echo -e "${RED}Application failed to start. Check the logs:${NC}"
    docker-compose logs brideside-backend
    exit 1
fi

echo -e "${GREEN}Deployment completed successfully!${NC}"
