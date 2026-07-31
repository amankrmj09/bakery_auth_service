# 🚀 Auth Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)
![Cache](https://img.shields.io/badge/Cache-Redis-red.svg)

The **Auth Service** is a core microservice of the Shah's Bakery Microservice Platform. It is responsible for handling user authentication, authorization, registration, Two-Factor Authentication (OTP), and role-based access control.

## 📑 Table of Contents
- [Architecture & Design](#-architecture--design)
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [API Reference](#-api-reference)
- [Configuration](#-configuration)
- [How to Run Locally](#-how-to-run-locally)
- [Testing](#-testing)
- [Dependencies](#-dependencies)
- [Related Links](#-related-links)

## 🏗️ Architecture & Design
The Auth Service leverages a layered architecture focusing on robust security.
- **Data Storage**: PostgreSQL is used to store user credentials, profiles, addresses, and roles.
- **Caching & OTP**: Redis is utilized for caching One-Time Passwords (OTPs) during login and registration, and for token management (like blacklisting).
- **Communication**: REST APIs handle external traffic, and Feign clients are used to communicate with other internal microservices (like the Notification Service to dispatch emails).
- **Security**: Spring Security integrated with custom JWT (JSON Web Token) filters and role-based access rules.

## ✨ Features
- **Secure Authentication**: Username/Email and password-based login.
- **Two-Factor Authentication (2FA)**: OTP verification steps required for successful registration, login, and password resets.
- **JWT Management**: Generation, validation, and refreshing of stateless JSON Web Tokens.
- **Role-Based Access Control (RBAC)**: User, Baker, Admin, and System role segregation.
- **Account Security Policies**: Configurable failed login lockout thresholds and durations.
- **Admin Analytics**: Dashboard statistics tracking for user engagement, orders, and revenue integrations.
- **Global Store Settings**: Controls global states like "Accepting Orders".

## 📁 Folder Structure
The source code under `src/main/java/com/blubugtech/bakery_auth_service/` is organized as follows:
```text
src/
└── main/
    └── java/.../bakery_auth_service/
        ├── client/     # Feign clients (e.g., NotificationServiceClient)
        ├── config/     # Security configurations, Redis setup, and WebMvc config
        ├── controller/ # Segmented REST endpoints (admin, publicapi, internal)
        ├── dto/        # Request/Response Data Transfer Objects (e.g., auth, user)
        ├── entity/     # JPA Entities (User, UserAddress, StoreSettings)
        ├── exception/  # Custom Exceptions (AuthException) and Handlers
        ├── repository/ # Spring Data JPA interfaces
        ├── security/   # Custom UserDetailsService and JWT implementations
        └── service/    # Core business logic (AuthService, OtpService, UserService)
```

## 🌐 API Reference
> [!NOTE]
> For complete and detailed API definitions, request/response bodies, and schemas, please refer to the [API_REFERENCE.md](./API_REFERENCE.md) file.

**Key Endpoints:**
- `POST /api/auth/register` - Initiate user registration (Sends OTP)
- `POST /api/auth/register/verify` - Verify OTP to complete registration
- `POST /api/auth/login` - Initiate login flow (Sends OTP)
- `POST /api/auth/login/verify` - Verify OTP to complete login and retrieve JWT
- `POST /api/auth/forgot-password` - Initiate password reset flow
- `GET /api/users/profile` - Retrieve current user profile
- `GET /api/users/admin/all` - List all users (Admin only)

## ⚙️ Configuration
Required environment variables are listed in `.env.example`. Create a `.env` file to configure the service locally.

| Variable | Description | Example |
|----------|-------------|---------|
| `SERVER_PORT` | Port for the service | `8080` |
| `AUTH_DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/auth_db` |
| `JWT_SECRET` | Secret key for JWT signing | `supersecretkey...` |
| `REDIS_HOST` | Redis host for OTPs | `localhost` |
| `REDIS_PORT_AUTH`| Redis port | `6379` |
| `LOGIN_MAX_ATTEMPTS` | Failed attempts before lockout | `5` |
| `LOGIN_LOCKOUT_DURATION`| Lockout duration in minutes | `15` |

## 🚀 How to Run Locally

### Prerequisites
- JDK 21+
- Gradle
- PostgreSQL running locally or via Docker
- Redis running locally or via Docker

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_auth_service.git
   cd bakery_auth_service
   ```

2. **Configure Environment:**
   Copy `.env.example` to `.env` and fill in the required values (DB credentials, Redis config, JWT Secret).
   You can spin up the required databases using the compose file in the root project:
   ```bash
   docker-compose -f docker-compose-db.yml up -d
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🧪 Testing
To run the automated test suite for the Auth Service:
```bash
./gradlew test
```

## 🛠️ Dependencies
- **Framework:** Spring Boot 3.x, Spring Security (OAuth2 / JWT)
- **Database:** PostgreSQL (Spring Data JPA)
- **Cache:** Redis (Spring Data Redis)
- **Key Modules:** Spring Web, Eureka Client, OpenFeign

## 🔗 Related Links
- [Main Platform README](../README.md)
- [API Reference Document](./API_REFERENCE.md)
- [Standard README Template](../README_TEMPLATE.md)
