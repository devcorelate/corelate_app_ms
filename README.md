# Corelate Application Management Microservice

A Spring Boot microservice for managing application data, forms, lists, and publishing workflows within the Corelate platform. This service provides RESTful APIs for CRUD operations on application data with support for event-driven architecture using Kafka.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Event-Driven Architecture](#event-driven-architecture)
- [Observability](#observability)
- [Docker Deployment](#docker-deployment)
- [Development](#development)
- [Resilience Patterns](#resilience-patterns)
- [Security](#security)

## Overview

The Application Management microservice is part of the Corelate ecosystem, responsible for:

- Managing application lists and form data
- Handling application publishing workflows
- Tracking publish logs with user visibility controls
- Event-driven communication via Kafka
- Integration with external services (Accounts microservice)
- Data encryption/decryption capabilities

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.2.1
- **Spring Cloud**: 2023.0.1
- **Database**: PostgreSQL
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway (via Feign)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **Observability**: 
  - OpenTelemetry 1.32.0
  - Micrometer with Prometheus
  - Spring Boot Actuator
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Build Tool**: Maven 3.9+
- **Containerization**: Docker (Jib Maven Plugin)

## Architecture

### Microservices Architecture

This service follows a microservices architecture pattern with:

- **Service Discovery**: Registers with Eureka Server for dynamic service discovery
- **Configuration Management**: Centralized configuration via Spring Cloud Config Server
- **Inter-Service Communication**: Feign clients for synchronous REST calls
- **Event-Driven**: Kafka for asynchronous messaging
- **Circuit Breaker**: Resilience4j for fault tolerance

### Key Components

```
├── Controllers
│   ├── AppController - Main CRUD operations for lists and forms
│   ├── ApiController - Build info and system metadata
│   └── EncryptionController - Encryption/decryption endpoints
├── Services
│   ├── AppServiceImpl - Business logic implementation
│   └── EncryptDecryptService - Encryption service
├── Repositories
│   ├── FormDataRepository
│   ├── FormDataEntityRepository
│   ├── ListRepository
│   └── PublishLogsRepository
├── Entities
│   ├── FormData
│   ├── FormDataEntity
│   ├── ListData
│   └── PublishLog
└── External Clients
    └── AppFeignClient - Integration with Accounts service
```

## Prerequisites

- **Java Development Kit (JDK)**: 21 or higher
- **Maven**: 3.9 or higher
- **PostgreSQL**: 12 or higher
- **Apache Kafka**: 3.x
- **Eureka Server**: Running on port 8070
- **Config Server**: Running on port 8071 (optional)
- **Docker**: For containerized deployment

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd app
```

### 2. Set Up PostgreSQL Database

```bash
# Create database
createdb -U postgres appdb

# Or using psql
psql -U postgres
CREATE DATABASE appdb;
```

### 3. Configure Application

Update `src/main/resources/application.yml` with your environment settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/appdb
    username: postgres
    password: password
```

### 4. Start Required Services

Ensure the following services are running:

- PostgreSQL (port 5435)
- Kafka (port 9092)
- Eureka Server (port 8070)
- Config Server (port 8071) - optional

### 5. Build and Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on port **8085**.

### 6. Access Swagger UI

Navigate to: `http://localhost:8085/swagger-ui.html`

## Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8085 | Application server port |
| `spring.application.name` | app | Service name for discovery |
| `spring.datasource.url` | jdbc:postgresql://localhost:5435/appdb | Database connection URL |
| `eureka.client.serviceUrl.defaultZone` | http://localhost:8070/eureka/ | Eureka server URL |
| `encrypt.key` | WEd0VcO8pyygSprR0UZRe7ENxaG2aV | Encryption key |

### Kafka Configuration

```yaml
spring:
  cloud:
    stream:
      bindings:
        updateCommunication-in-0:
          destination: communication-sent
          group: app
        sendCommunication-out-0:
          destination: send-communication
      kafka:
        binder:
          brokers:
            - localhost:9092
```

### Database Configuration

The application uses JPA with Hibernate for ORM:

- **Dialect**: PostgreSQL
- **DDL Auto**: update (automatically updates schema)
- **Show SQL**: true (logs SQL statements)

## API Documentation

### Base URLs

- **Data Operations**: `/data`
- **API Metadata**: `/api`
- **Encryption**: `/`

### Main Endpoints

#### List Management

**Create List**
```http
POST /data/create
Content-Type: application/json

{
  "listId": "string",
  "name": "string",
  "description": "string"
}
```

**Fetch All Lists**
```http
GET /data/fetch/all
```

#### Form Data Management

**Create Form Data**
```http
POST /data/add/form-data
Content-Type: application/json

{
  "templateId": "string",
  "formId": "string",
  "formData": {},
  "createdBy": "string"
}
```

**Get All Data by Workflow**
```http
GET /data/fetch/all/data-by-workflow?templateId={templateId}
```

**Update Form Data**
```http
POST /data/update
Content-Type: application/json

{
  "templateId": "string",
  "formId": "string",
  "updatedData": [
    {
      "id": "string",
      "data": {}
    }
  ]
}
```

#### Publishing Management

**Publish Application**
```http
PUT /data/update/application/publish
Content-Type: application/json

{
  "applicationId": "string",
  "publish": true,
  "publishBy": "string",
  "visibility": "public",
  "users": [1, 2, 3]
}
```

**Get All Published Applications**
```http
GET /data/fetch/all/published
```

#### Encryption Endpoints

**Create Encryption Keys**
```http
GET /createkeys
```

**Encrypt Message**
```http
POST /encrypt
Content-Type: application/json

"message to encrypt"
```

**Decrypt Message**
```http
POST /decrypt
Content-Type: application/json

"encrypted message"
```

#### System Information

**Get Build Version**
```http
GET /api/build-info
```

**Get Java Version**
```http
GET /api/java-version
```

### Response Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 202 | Accepted - Entity successfully logged in |
| 417 | Expectation Failed - Update/Delete operation failed |
| 500 | Internal Server Error |

## Database Schema

### Main Entities

#### ListData
- `listId` (PK) - Unique list identifier
- `name` - List name
- `description` - List description
- `communicationSw` - Communication switch flag
- `createdAt` - Creation timestamp
- `createdBy` - Creator username

#### FormData
- `dataId` (PK) - Unique data identifier
- `templateId` - Template reference
- `formId` - Form reference
- `data` - JSON data storage

#### FormDataEntity
- `id` (PK) - Auto-generated ID
- `templateId` - Template reference
- `formId` - Form reference
- `formData` - JSON form data
- `createdBy` - Creator username
- `createdAt` - Creation timestamp

#### PublishLog
- `id` (PK) - Auto-generated ID
- `applicationId` - Application reference
- `publish` - Publish status (boolean)
- `publishAt` - Publish timestamp
- `publishBy` - Publisher username
- `visibility` - Visibility setting (public/private)
- `users` - List of user IDs with access

## Event-Driven Architecture

### Kafka Topics

#### Consumer
- **Topic**: `communication-sent`
- **Group**: `app`
- **Function**: `updateCommunication`
- **Purpose**: Listens for communication sent events and updates communication status

#### Producer
- **Topic**: `send-communication`
- **Function**: `sendCommunication`
- **Purpose**: Publishes events to trigger communication sending

### Event Flow

```
1. Application published → PublishLog created
2. Event published to send-communication topic
3. Communication service processes event
4. Communication sent event published to communication-sent topic
5. This service consumes event and updates communicationSw flag
```

## Observability

### Actuator Endpoints

All actuator endpoints are exposed:

```
http://localhost:8085/actuator
```

Available endpoints:
- `/actuator/health` - Health check with readiness/liveness probes
- `/actuator/info` - Application information
- `/actuator/metrics` - Prometheus metrics
- `/actuator/prometheus` - Prometheus scraping endpoint
- `/actuator/shutdown` - Graceful shutdown (POST)

### OpenTelemetry Integration

The application is instrumented with OpenTelemetry Java agent for distributed tracing:

- **Version**: 1.32.0
- **Trace ID**: Included in log patterns
- **Span ID**: Included in log patterns

### Logging

Log pattern includes trace context:
```
%5p [app,%X{trace_id},%X{span_id}]
```

Debug logging enabled for:
```
com.corelate.app: DEBUG
```

### Metrics

Prometheus metrics are tagged with:
```yaml
metrics:
  tags:
    application: app
```

## Docker Deployment

### Build Docker Image with Jib

```bash
# Build and push to Docker Hub
./mvnw clean compile jib:build

# Build to local Docker daemon
./mvnw clean compile jib:dockerBuild
```

**Image**: `devcorelate01/app:v1.4.08`

### Using Dockerfile

```bash
# Build image
docker build -f src/main/resources/Dockerfile -t corelate-app:latest .

# Run container
docker run -d \
  -p 8085:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5435/appdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  --name corelate-app \
  corelate-app:latest
```

### Docker Compose Example

```yaml
version: '3.8'
services:
  app:
    image: devcorelate01/app:v1.4.08
    ports:
      - "8085:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/appdb
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka:8070/eureka/
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - kafka
      - eureka
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/corelate/app/
│   │   ├── controllers/          # REST controllers
│   │   ├── service/              # Business logic
│   │   │   ├── impl/             # Service implementations
│   │   │   └── client/           # Feign clients
│   │   ├── repository/           # JPA repositories
│   │   ├── entity/               # JPA entities
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── mapper/               # Entity-DTO mappers
│   │   ├── exeption/             # Exception handlers
│   │   ├── utility/              # Utility classes (JWT)
│   │   ├── functions/            # Business functions
│   │   └── constants/            # Application constants
│   └── resources/
│       ├── application.yml       # Configuration
│       └── Dockerfile            # Container definition
└── test/
    └── java/                     # Test classes
```

### Running Tests

```bash
./mvnw test
```

### Code Style

The project uses:
- **Lombok** for reducing boilerplate code
- **SLF4J** for logging
- **Jakarta Validation** for input validation
- **MapStruct** pattern for entity-DTO mapping

### Adding New Endpoints

1. Create DTO in `dto/` package
2. Create entity in `entity/` package
3. Create repository interface in `repository/`
4. Implement service method in `service/impl/`
5. Add controller endpoint in `controllers/`
6. Document with Swagger annotations

## Resilience Patterns

### Circuit Breaker

```yaml
resilience4j.circuitbreaker:
  configs:
    default:
      slidingWindowSize: 10
      permittedNumberOfCallsInHalfOpenState: 2
      failureRateThreshold: 50
      waitDurationInOpenState: 10000
```

- Opens circuit after 50% failure rate
- Allows 2 calls in half-open state
- Waits 10 seconds before retry

### Retry

```yaml
resilience4j.retry:
  configs:
    default:
      maxRetryAttempts: 3
      waitDuration: 500
      enableExponentialBackoff: true
      exponentialBackoffMultiplier: 2
```

- Retries up to 3 times
- Exponential backoff: 500ms, 1000ms, 2000ms
- Ignores NullPointerException
- Retries on TimeoutException

### Rate Limiter

```yaml
resilience4j.ratelimiter:
  configs:
    default:
      timeoutDuration: 1000
      limitRefreshPeriod: 5000
      limitForPeriod: 1
```

- 1 request per 5 seconds
- 1 second timeout

## Security

### JWT Integration

The service includes JWT utility for token validation:
- `JwtUtil` class for token operations
- JJWT library version 0.11.5

### Encryption

Custom encryption/decryption service available:
- Key-based encryption
- Configurable encryption key in `application.yml`

### Security Dependencies (Commented)

Spring Security dependencies are present but commented out. To enable:

1. Uncomment Spring Security dependencies in `pom.xml`
2. Configure security in application configuration
3. Add authentication/authorization rules

### Best Practices

- Use environment variables for sensitive data
- Rotate encryption keys regularly
- Enable HTTPS in production
- Implement proper authentication/authorization
- Validate all input data
- Use prepared statements (JPA handles this)

## Integration with Other Services

### Accounts Microservice

Feign client integration for fetching user account details:

```java
@FeignClient("accounts")
public interface AppFeignClient {
    @GetMapping("/api/fetch/account")
    CustomerDto fetchAccountsById(@RequestParam("accountId") Long accountId);
}
```

Used in publish workflow to fetch user emails for notifications.

### Service Dependencies

- **Eureka Server**: Service registration and discovery
- **Config Server**: Centralized configuration (optional)
- **Accounts Service**: User account information
- **Kafka**: Event streaming

## Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5435

# Verify credentials in application.yml
```

**Eureka Registration Failed**
```bash
# Ensure Eureka server is running
curl http://localhost:8070/eureka/apps

# Check network connectivity
```

**Kafka Connection Issues**
```bash
# Verify Kafka is running
kafka-topics.sh --list --bootstrap-server localhost:9092

# Check topic exists
kafka-topics.sh --describe --topic communication-sent --bootstrap-server localhost:9092
```

### Health Check

```bash
curl http://localhost:8085/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

[Specify your license here]

## Contact

**Author**: Seth Hernandez

**Project**: Corelate Application Management Microservice

**Version**: 1.0.0 (v1.4.08)

---

For more information about the Corelate platform, please refer to the main documentation.
