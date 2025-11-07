# Zoo Backend API

A **Java 21 / Spring Boot** backend application for managing a zoo, its animals, and rooms.  
This project uses **MongoDB** as the database, **Caffeine** for caching, and provides RESTful APIs with proper **idempotency**, **concurrency control**, and **caching**.

---

## Features

### Core Features
- **CRUD for Animals and Rooms**
    - Create, Read, Update, Delete animals and rooms.
    - Read single entity endpoints.
- **Room Management**
    - Place an animal into a room.
    - Move an animal from one room to another.
    - Remove an animal from a room.
- **Favorite Rooms**
    - Assign one or more favorite rooms to an animal.
    - Unassign favorite rooms.
    - List favorite rooms with the number of animals that favorited them.
- **Pagination & Sorting**
    - Get all animals in a specific room with sorting (`title` or `located`) and pagination.

### Advanced Features
- **Caching**
    - Read operations for animals and room lists are cached using **Caffeine**.
    - Cache eviction is handled automatically for update and delete operations.
- **Idempotency**
    - Prevents duplicate write operations using `Idempotency-Key` headers.
    - Supports key prefixing to use the same key across multiple endpoints safely.
- **Concurrency Control**
    - Optimistic locking using `@Version` and `If-Match` headers for update and delete.
- **Circuit Breaker**
    - Ensures resilience for downstream service calls.
    - Prevents cascading failures and returns fallback responses if a service is unavailable.
- **Validation**
    - Input validation for request DTOs.
    - Global exception handling using `@ControllerAdvice`.
- **Logging**
    - Custom `LoggerService` to log service and controller events.
    - Logs from global exception handler.

### Testing
- Unit tests for services, repositories, and controllers.
- Integration tests using **MockMVC** and **Testcontainers MongoDB**.
- Caching and idempotency tests included.

### Deployment
- Dockerized Spring Boot application.
- `docker-compose.yml` provided for running MongoDB and the backend together.
- Ready for Kubernetes deployment.

---

## Tech Stack
- Java 21
- Spring Boot 3
- MongoDB
- Spring Data MongoDB
- Spring Cache with Caffeine
- Spring Validation & Spring Web
- Spring AOP / Resilience4j for Circuit Breaker
- Docker / Docker Compose
- JUnit 5, Mockito, MockMVC, Testcontainers

---

## Important Links

| Link                 | Description                               |
|----------------------|-------------------------------------------|
| `/swagger-ui.html`   | Swagger UI for API documentation          |
| `/actuator/health`   | Health check endpoint                     |
| `/api/v1/animals`    | Animal APIs                               |
| `/api/v1/rooms`      | Room APIs                                 |
| `Dockerfile`         | Build container image for the application |
| `docker-compose.yml` | Run application and MongoDB locally       |

---

## Getting Started

### Prerequisites
- Java 21
- Gradle 8+
- Docker & Docker Compose
- MongoDB (if running locally without Docker)

### Running Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/jishasandeep/zoo-backend.git
   cd zoo-backend
