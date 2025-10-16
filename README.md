# Brideside Backend

A production-ready Spring Boot REST API for managing wedding deals and categories.

## 🚀 Production Features

- **High Performance**: Connection pooling, caching, and optimized JPA configuration
- **Security**: Spring Security with CORS, HTTPS, and authentication
- **Monitoring**: Actuator endpoints, Prometheus metrics, and Grafana dashboards
- **Observability**: Structured logging, health checks, and distributed tracing
- **Scalability**: Docker containerization with orchestration support
- **API Documentation**: Swagger/OpenAPI 3.0 documentation
- **Testing**: Comprehensive test suite with integration tests
- **DevOps**: Automated deployment scripts and monitoring setup

## API Endpoints

### POST /api/deals
Creates multiple deals from a single request. Each category creates a separate deal entry.

**Request Body:**
```json
{
  "name": "Shubham",
  "contact_number": "9304683214",
  "categories": [
    {
      "name": "Photography",
      "event_date": "2025-10-20",
      "venue": "The Leela Palace, New Delhi",
      "budget": 200000,
      "expected_gathering": 200
    },
    {
      "name": "Makeup",
      "event_date": "2025-10-25",
      "venue": "Taj Palace, New Delhi",
      "budget": 150000,
      "expected_gathering": 180
    }
  ]
}
```

**Response:**
```json
{
  "message": "Successfully created 2 deal(s) for user Shubham",
  "createdDeals": [
    {
      "id": 1,
      "userName": "Shubham",
      "contactNumber": "9304683214",
      "category": "Photography",
      "eventDate": "2025-10-20",
      "venue": "The Leela Palace, New Delhi",
      "budget": "200000.00",
      "expectedGathering": 200,
      "createdAt": "2024-01-15 10:30:00",
      "updatedAt": "2024-01-15 10:30:00"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

### GET /api/deals
Get all deals.

### GET /api/deals/user/{userName}
Get deals by user name.

### GET /api/deals/contact/{contactNumber}
Get deals by contact number.

### GET /api/deals/category/{category}
Get deals by category.

## 🛠️ Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Maven 3.6+**
- **Docker & Docker Compose**
- **MySQL 8.0+** (for production)

## 🚀 Quick Start

### Development Environment

1. **Setup development environment:**
   ```bash
   chmod +x scripts/setup-dev.sh
   ./scripts/setup-dev.sh
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Access the application:**
   - API: http://localhost:8080/api/deals
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Production Deployment

1. **Deploy with Docker Compose:**
   ```bash
   chmod +x scripts/deploy.sh
   ./scripts/deploy.sh
   ```

2. **Access production services:**
   - API: http://localhost:8080/api/deals
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin123)

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | dev | Active Spring profile |
| `DB_HOST` | localhost:3306 | Database host |
| `DB_DATABASE` | reevah | Database name |
| `DB_USER` | root | Database username |
| `DB_PASSWORD` | Shubham@123 | Database password |
| `DB_POOL_SIZE` | 20 | Connection pool size |
| `LOG_LEVEL_ROOT` | INFO | Root log level |
| `ADMIN_USERNAME` | admin | Admin username |
| `ADMIN_PASSWORD` | admin123 | Admin password |

### Profiles

- **dev**: Development with debug logging and H2 database
- **test**: Testing with in-memory database
- **prod**: Production with optimized settings

## 📊 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: Custom health indicator
- **Readiness Probe**: `/actuator/health/readiness`
- **Liveness Probe**: `/actuator/health/liveness`

### Metrics
- **Prometheus Metrics**: `/actuator/prometheus`
- **Application Metrics**: `/actuator/metrics`
- **JVM Metrics**: Memory, GC, threads
- **HTTP Metrics**: Request duration, status codes

### Logging
- **Structured Logging**: JSON format in production
- **Log Levels**: Configurable per package
- **Log Rotation**: Automatic with size limits
- **Centralized Logging**: Ready for ELK stack

## 🔒 Security Features

- **Authentication**: Basic Auth for admin endpoints
- **CORS**: Configurable cross-origin requests
- **HTTPS**: Ready for SSL termination
- **Security Headers**: HSTS, XSS protection
- **Input Validation**: Comprehensive request validation
- **SQL Injection Protection**: JPA parameterized queries

## 🐳 Docker Deployment

### Development
```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Production
```bash
docker-compose up -d
```

### Custom Configuration
```bash
# Override environment variables
export DB_PASSWORD=your-secure-password
export ADMIN_PASSWORD=your-admin-password
docker-compose up -d
```

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn test -Dspring.profiles.active=test

# Test with coverage
mvn test jacoco:report
```

### Test Coverage
- **Unit Tests**: Service layer and utilities
- **Integration Tests**: Controller endpoints
- **Database Tests**: Repository layer
- **Security Tests**: Authentication and authorization

## 📈 Performance Optimization

### Database
- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: Batch processing and indexing
- **Caching**: Caffeine cache for frequently accessed data

### Application
- **JVM Tuning**: G1GC with optimized parameters
- **HTTP/2**: Enabled for better performance
- **Compression**: Gzip compression for responses
- **Async Processing**: Non-blocking I/O operations

## 🔧 Maintenance

### Backup Database
```bash
chmod +x scripts/backup.sh
./scripts/backup.sh
```

### Health Check
```bash
chmod +x scripts/health-check.sh
./scripts/health-check.sh
```

### Log Management
```bash
# View application logs
docker-compose logs -f brideside-backend

# View database logs
docker-compose logs -f mysql
```

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check if MySQL container is running
   - Verify database credentials
   - Check network connectivity

2. **Application Won't Start**
   - Check Java version (17+)
   - Verify Maven dependencies
   - Check port availability

3. **Health Check Failing**
   - Check application logs
   - Verify database connectivity
   - Check memory usage

### Debug Mode
```bash
# Enable debug logging
export LOG_LEVEL_APP=DEBUG
mvn spring-boot:run
```

## Testing the API

You can test the API using curl, Postman, or any HTTP client:

```bash
curl -X POST http://localhost:8080/api/deals \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Shubham",
    "contact_number": "9304683214",
    "categories": [
      {
        "name": "Photography",
        "event_date": "2025-10-20",
        "venue": "The Leela Palace, New Delhi",
        "budget": 200000,
        "expected_gathering": 200
      }
    ]
  }'
```

## Project Structure

```
src/main/java/com/brideside/backend/
├── BridesideBackendApplication.java    # Main Spring Boot application
├── controller/
│   └── DealController.java             # REST controller
├── dto/
│   ├── DealRequestDto.java             # Request DTO
│   └── DealResponseDto.java            # Response DTO
├── entity/
│   └── Deal.java                       # JPA entity
├── exception/
│   └── GlobalExceptionHandler.java     # Global exception handler
├── repository/
│   └── DealRepository.java             # JPA repository
└── service/
    └── DealService.java                 # Business logic service
```
