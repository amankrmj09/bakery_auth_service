# 🧁 Auth Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)

Welcome to the **Auth Service**, a core component of the Shah's Bakery Microservice Platform.

## 📑 Table of Contents
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [Dependencies](#-dependencies)
- [Endpoints](#-endpoints)
- [How to Run](#-how-to-run)
- [Related Links](#-related-links)

## ✨ Features
- Secure user authentication and authorization.
- JWT (JSON Web Token) generation and validation.
- Role-based access control management.
- Password hashing and secure user credential storage.
- Admin Analytics & Dashboard Statistics tracking.
- Global Store Settings and configurations management.

## 📁 Folder Structure
The main `src/main/java` directory is organized as follows:
```text
src/
└── main/
    └── java/.../bakery_auth_service/
        ├── client/     # Feign clients for inter-service communication.
        ├── config/     # Security configurations, JWT setup, and Redis caching.
        ├── controller/ # REST endpoints for authentication, users, and admin statistics.
        ├── dto/        # Data Transfer Objects for request/response payloads.
        ├── entity/     # Database entities mapping to PostgreSQL tables.
        ├── exception/  # Custom exceptions and global error handlers.
        ├── repository/ # Spring Data JPA interfaces for database access.
        ├── security/   # Custom UserDetailsService and Spring Security implementations.
        └── service/    # Core business logic for authentication, users, and dashboard stats.
```

## 🛠️ Dependencies
- **Framework:** Spring Boot, Spring Security (OAuth2 / JWT)
- **Database:** PostgreSQL
- **Key Modules:** Eureka Client, Spring Web, Spring Data JPA

## 🌐 Endpoints
> [!NOTE]
> For complete and detailed API definitions, please refer to the OpenAPI Reference available via the API Gateway's Swagger UI.

- `POST /api/auth/register` - Registers a new user account.
- `POST /api/auth/login` - Authenticates a user and returns a JWT.
- `POST /api/auth/validate` - Validates a provided JWT token.
- `POST /api/auth/refresh` - Refreshes an expired access token.

## 🚀 How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_auth_service.git
   cd bakery_auth_service
   ```

2. **Configure Environment:**
   Ensure your `.env` or `application.yml` properties (including DB credentials) are set.

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🔗 Related Links
- [Main Platform README](../README.md)
