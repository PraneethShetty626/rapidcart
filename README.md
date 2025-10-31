# 🛒 RapidCart - Microservices E-Commerce Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue.svg)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange.svg)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)

A modern, scalable microservices-based e-commerce platform built with Spring Boot, featuring product management, order processing, and real-time notifications.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

**RapidCart** is a production-ready microservices application designed to demonstrate modern e-commerce architecture patterns. The platform implements:

- **Product Management**: Create, update, and manage product inventory
- **Order Processing**: Handle customer orders with stock validation and reservation
- **Asynchronous Notifications**: Event-driven order notifications via message queues
- **RESTful APIs**: Well-documented OpenAPI/Swagger endpoints
- **Containerized Deployment**: Full Docker Compose orchestration

---

## 🏗️ Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Product    │ │    Order     │ │ Notification │
│   Service    │ │   Service    │ │   Service    │
│   :8081      │ │   :8082      │ │   :8083      │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       │                │                │
       └────────┬───────┴────────┬───────┘
                │                │
        ┌───────▼───────┐ ┌─────▼──────┐
        │  PostgreSQL   │ │  RabbitMQ  │
        │    :5436      │ │  :5672     │
        └───────────────┘ └────────────┘
```

### Communication Patterns

1. **Synchronous Communication**: REST APIs between services (Order Service → Product Service)
2. **Asynchronous Communication**: Message queue (Order Service → Notification Service via RabbitMQ)
3. **Database per Service**: Each service maintains its own data schema

---

## 🛠️ Tech Stack

### Backend Framework

- **Spring Boot 3.5.7** - Main application framework
- **Spring Data JPA** - Data persistence layer
- **Spring Web** - RESTful web services
- **Spring AMQP** - RabbitMQ integration
- **Spring Validation** - Request validation
- **Spring Actuator** - Health checks and monitoring

### Database & Messaging

- **PostgreSQL 14** - Relational database
- **RabbitMQ 3** - Message broker for async communication

### Development Tools

- **Lombok** - Boilerplate code reduction
- **SpringDoc OpenAPI** - API documentation (Swagger UI)
- **Maven** - Dependency management and build tool
- **Docker & Docker Compose** - Containerization

### Testing

- **JUnit 5** - Unit testing framework
- **Spring Boot Test** - Integration testing
- **H2 Database** - In-memory database for testing

---

## 📁 Project Structure

```
rapidcart/
├── product-service/              # Product management microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/rapidcart/product_service/
│   │   │   │       ├── controller/     # REST controllers
│   │   │   │       ├── service/        # Business logic
│   │   │   │       ├── repository/     # Data access layer
│   │   │   │       ├── dto/            # Data transfer objects
│   │   │   │       ├── model/          # JPA entities
│   │   │   │       ├── exception/      # Custom exceptions
│   │   │   │       └── config/         # Configuration classes
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-docker.properties
│   │   └── test/                       # Test classes
│   ├── Dockerfile
│   └── pom.xml
│
├── order-service/                # Order processing microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/rapidcart/order_service/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── dto/
│   │   │   │       ├── model/
│   │   │   │       ├── exception/
│   │   │   │       ├── config/
│   │   │   │       └── client/         # External service clients
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-docker.properties
│   │   │       └── V2__add_foreign_key_to_orders.sql
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── notification-service/         # Notification microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/rapidcart/notification_service/
│   │   │   │       ├── listener/       # RabbitMQ listeners
│   │   │   │       ├── service/
│   │   │   │       ├── dto/
│   │   │   │       └── config/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml            # Container orchestration
├── start-services.sh             # Startup script
├── pom.xml                       # Parent POM
├── LICENSE
└── README.md                     # This file
```

---

## 🔧 Services

### 1. Product Service (Port 8081)

**Responsibilities:**

- CRUD operations for products
- Stock management and validation
- Product inventory tracking
- Stock reduction for confirmed orders

**Key Endpoints:**

```
POST   /api/products              - Create new product
GET    /api/products              - Get all products (paginated)
GET    /api/products/{id}         - Get product by ID
PUT    /api/products/{id}         - Update product
DELETE /api/products/{id}         - Soft delete product
GET    /api/products/{id}/stock   - Check stock availability
PUT    /api/products/{id}/reduce-stock - Reduce stock
```

**Technologies:**

- Spring Boot, Spring Data JPA
- PostgreSQL
- SpringDoc OpenAPI

---

### 2. Order Service (Port 8082)

**Responsibilities:**

- Order creation and management
- Stock validation with Product Service
- Order status tracking
- Publishing order events to RabbitMQ

**Key Endpoints:**

```
POST   /api/orders                  - Create new order
GET    /api/orders/{id}             - Get order by ID
GET    /api/orders                  - Get all orders (paginated)
GET    /api/orders/customer/{id}    - Get orders by customer
```

**Technologies:**

- Spring Boot, Spring Data JPA
- PostgreSQL
- RabbitMQ (Publisher)
- RestTemplate for Product Service integration

---

### 3. Notification Service (Port 8083)

**Responsibilities:**

- Listening to order events from RabbitMQ
- Processing and logging notifications
- Extensible for email/SMS notifications

**Key Features:**

- Asynchronous message consumption
- Event-driven architecture
- Decoupled from Order Service

**Technologies:**

- Spring Boot
- RabbitMQ (Consumer)
- Spring AMQP

---

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

### Required Software

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker** ([Download](https://www.docker.com/get-started))
- **Docker Compose** ([Download](https://docs.docker.com/compose/install/))

### Verify Installation

```bash
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.8+
docker --version
docker compose --version
```

---

## 🚀 Getting Started

### Option 1: Quick Start with Docker Compose (Recommended)

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/rapidcart.git
   cd rapidcart
   ```

2. **Start all services**

   ```bash
   chmod +x start-services.sh
   ./start-services.sh
   ```

   This script will:

   - Stop any existing containers
   - Build all microservices
   - Start PostgreSQL and RabbitMQ
   - Launch all three services with health checks
   - Wait for all services to be healthy

3. **Verify services are running**

   ```bash
   docker compose ps
   ```

   All services should show status as "healthy".

### Option 2: Manual Docker Compose

1. **Build and start services**

   ```bash
   docker compose up --build -d
   ```

2. **View logs**

   ```bash
   docker compose logs -f
   ```

3. **Stop services**
   ```bash
   docker compose down
   ```

### Option 3: Local Development (Without Docker)

1. **Start PostgreSQL locally**

   ```bash
   # Using Docker
   docker run -d \
     --name postgres \
     -e POSTGRES_DB=rapidcart \
     -e POSTGRES_USER=rapidcart \
     -e POSTGRES_PASSWORD=rapidcart \
     -p 5436:5432 \
     postgres:14
   ```

2. **Start RabbitMQ locally**

   ```bash
   docker run -d \
     --name rabbitmq \
     -e RABBITMQ_DEFAULT_USER=guest \
     -e RABBITMQ_DEFAULT_PASS=guest \
     -p 5672:5672 \
     -p 15672:15672 \
     rabbitmq:3-management
   ```

3. **Build all services**

   ```bash
   mvn clean install
   ```

4. **Run each service** (in separate terminals)

   ```bash
   # Terminal 1 - Product Service
   cd product-service
   mvn spring-boot:run

   # Terminal 2 - Order Service
   cd order-service
   mvn spring-boot:run

   # Terminal 3 - Notification Service
   cd notification-service
   mvn spring-boot:run
   ```

---

## 📚 API Documentation

### Swagger UI (Interactive Documentation)

Once services are running, access the Swagger UI:

- **Product Service**: http://localhost:8081/swagger-ui.html
- **Order Service**: http://localhost:8082/swagger-ui.html
- **Notification Service**: http://localhost:8083/swagger-ui.html

### OpenAPI JSON Specification

- **Product Service**: http://localhost:8081/v3/api-docs
- **Order Service**: http://localhost:8082/v3/api-docs
- **Notification Service**: http://localhost:8083/v3/api-docs

### Example API Calls

#### Create a Product

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "sku": "LAP-001",
    "description": "High-performance laptop",
    "price": 999.99,
    "stockQuantity": 50
  }'
```

#### Get All Products

```bash
curl http://localhost:8081/api/products?page=0&size=10
```

#### Create an Order

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2,
    "customerId": 100
  }'
```

#### Get Order by ID

```bash
curl http://localhost:8082/api/orders/1
```

---

## ⚙️ Configuration

### Environment Variables

Each service can be configured using environment variables:

#### Product Service

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5436/rapidcart
SPRING_DATASOURCE_USERNAME=rapidcart
SPRING_DATASOURCE_PASSWORD=rapidcart
SERVER_PORT=8081
```

#### Order Service

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5436/rapidcart
SPRING_DATASOURCE_USERNAME=rapidcart
SPRING_DATASOURCE_PASSWORD=rapidcart
PRODUCT_SERVICE_URL=http://localhost:8081
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SERVER_PORT=8082
```

#### Notification Service

```properties
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SERVER_PORT=8083
```

### Application Profiles

The application supports two profiles:

1. **default** - For local development (see `application.properties`)
2. **docker** - For containerized deployment (see `application-docker.properties`)

Activate a profile:

```bash
java -jar app.jar --spring.profiles.active=docker
```

---

## 🧪 Testing

### Run All Tests

```bash
# From project root
mvn clean test

# For specific service
cd product-service
mvn test
```

### Test Coverage

Each service includes:

- **Unit Tests**: Testing individual components
- **Integration Tests**: Testing controller endpoints with in-memory H2 database
- **Test Configuration**: Separate `application-test.properties` for test environments

### Example Test Execution

```bash
# Run tests with coverage report
mvn clean test jacoco:report

# Run only integration tests
mvn verify -DskipUnitTests
```

---

## 📊 Monitoring

### Health Checks

All services expose Spring Actuator health endpoints:

- **Product Service**: http://localhost:8081/actuator/health
- **Order Service**: http://localhost:8082/actuator/health
- **Notification Service**: http://localhost:8083/actuator/health

### RabbitMQ Management Console

Access the RabbitMQ management interface:

- **URL**: http://localhost:15672
- **Username**: guest
- **Password**: guest

### Database Access

Connect to PostgreSQL:

```bash
docker exec -it rapidcart-postgres psql -U rapidcart -d rapidcart
```

### Container Logs

```bash
# View all logs
docker compose logs -f

# View specific service logs
docker compose logs -f product-service
docker compose logs -f order-service
docker compose logs -f notification-service
```

---

## 🔒 Security Considerations

### Current Implementation

- Basic validation on input data
- Soft delete for data integrity
- Environment-based configuration

### Recommended Enhancements

- [ ] Add Spring Security with OAuth2/JWT
- [ ] Implement API Gateway (e.g., Spring Cloud Gateway)
- [ ] Add rate limiting
- [ ] Encrypt sensitive data
- [ ] Enable CORS configuration
- [ ] Implement audit logging
- [ ] Add input sanitization

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Run tests**
   ```bash
   mvn clean test
   ```
5. **Commit with meaningful messages**
   ```bash
   git commit -m "Add feature: your feature description"
   ```
6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create a Pull Request**

### Code Style

- Follow Java naming conventions
- Write meaningful comments
- Ensure all tests pass
- Update documentation as needed

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Support

For questions or issues:

- **Open an issue**: [GitHub Issues](https://github.com/yourusername/rapidcart/issues)
- **Email**: praneethshetty626@gmail.com

---

## 👥 Authors

- **Praneeth Shetty** - _Initial work_

---

## 🙏 Acknowledgments

- Spring Boot team for excellent documentation
- PostgreSQL and RabbitMQ communities
- Docker for simplifying deployment
- Contributors and maintainers

---

**Made with ❤️ using Spring Boot**
