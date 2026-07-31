# Bakery Auth Service API Reference

This document provides a comprehensive reference for the API endpoints exposed by the Bakery Auth Service.

---

## System & Monitoring (Actuator)
**Base Path:** `/actuator`

Standard Spring Boot Actuator endpoints are used for monitoring and metrics.

### 1. Health Check
- **Method:** `GET`
- **Path:** `/actuator/health`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Standard Actuator Health JSON)

### 2. Service Info
- **Method:** `GET`
- **Path:** `/actuator/info`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Standard Actuator Info JSON)

### 3. Prometheus Metrics
- **Method:** `GET`
- **Path:** `/actuator/prometheus`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Prometheus Text Format)

## Auth Controller
**Base Path:** /api/auth

### 1. Initiate Registration
- **Method:** POST
- **Path:** /api/auth/register
- **Type of API:** Public
- **Request Body:** RegisterRequest (JSON)
- **Response Body:** 200 OK (Message with mock OTP)

### 2. Verify Registration OTP
- **Method:** POST
- **Path:** /api/auth/register/verify
- **Type of API:** Public
- **Request Body:** RegisterVerifyRequest (JSON)
- **Response Body:** 201 Created
  Returns AuthResponse with JWT token.

### 3. Resend Registration OTP
- **Method:** POST
- **Path:** /api/auth/register/resend
- **Type of API:** Public
- **Request Body:** ResendOtpRequest (JSON)
- **Response Body:** 200 OK (Message with mock OTP)

### 4. Initiate Login
- **Method:** POST
- **Path:** /api/auth/login
- **Type of API:** Public
- **Request Body:** LoginRequest (JSON)
- **Response Body:** 200 OK
  Returns LoginInitResponse with token and status.

### 5. Verify Login OTP
- **Method:** POST
- **Path:** /api/auth/login/verify
- **Type of API:** Public
- **Request Body:** LoginVerifyRequest (JSON)
- **Response Body:** 200 OK
  Returns AuthResponse with JWT token.

### 6. Resend Login OTP
- **Method:** POST
- **Path:** /api/auth/login/resend
- **Type of API:** Public
- **Request Body:** ResendOtpRequest (JSON)
- **Response Body:** 200 OK (Message with mock OTP)

### 7. Admin Direct Login
- **Method:** POST
- **Path:** /api/auth/admin/login
- **Type of API:** Public
- **Request Body:** LoginRequest (JSON)
- **Response Body:** 200 OK
  Returns AuthResponse with JWT token.

### 8. Initiate Forgot Password
- **Method:** POST
- **Path:** /api/auth/forgot-password
- **Type of API:** Public
- **Request Body:** ForgotPasswordRequest (JSON)
- **Response Body:** 200 OK (Message with mock OTP)

### 9. Reset Password using OTP
- **Method:** POST
- **Path:** /api/auth/forgot-password/reset
- **Type of API:** Public
- **Request Body:** ResetPasswordRequest (JSON)
- **Response Body:** 200 OK (Success message)

### 10. Refresh Token
- **Method:** POST
- **Path:** /api/auth/refresh
- **Type of API:** User
- **Request Header:** Authorization: Bearer <refresh_token>
- **Response Body:** 200 OK
  Returns AuthResponse with JWT token.

### 11. Validate Token
- **Method:** POST
- **Path:** /api/auth/validate
- **Type of API:** Internal
- **Request Header:** Authorization: Bearer <token>
- **Response Body:** 200 OK
  Returns TokenValidationResponse.

### 12. Logout
- **Method:** POST
- **Path:** /api/auth/logout
- **Type of API:** User
- **Request Header:** Authorization: Bearer <token>
- **Response Body:** 200 OK (Success message)

### 13. Change Password
- **Method:** POST
- **Path:** /api/auth/change-password
- **Type of API:** User
- **Request Body:** ChangePasswordRequest (JSON)
- **Response Body:** 200 OK (Success message)

### 14. Verify Email
- **Method:** POST
- **Path:** /api/auth/verify-email/{userId}
- **Type of API:** User
- **Request Body:** None
- **Response Body:** 200 OK (Success message)

### 15. Get Current User Info
- **Method:** GET
- **Path:** /api/auth/me
- **Type of API:** User
- **Request Header:** Authorization: Bearer <token>
- **Response Body:** 200 OK
  Returns TokenValidationResponse with user info.

---

## Internal Statistics Controller
**Base Path:** `/api/users/internal/stats`

### 1. Increment Orders
- **Method:** `POST`
- **Path:** `/api/users/internal/stats/increment-orders`
- **Type of API:** `System (requires X-User-Role: SYSTEM header)`
- **Request Body:** None
- **Response Body:** `200 OK`

### 2. Decrement Orders
- **Method:** `POST`
- **Path:** `/api/users/internal/stats/decrement-orders`
- **Type of API:** `System (requires X-User-Role: SYSTEM header)`
- **Request Body:** None
- **Response Body:** `200 OK`

### 3. Add Revenue
- **Method:** `POST`
- **Path:** `/api/users/internal/stats/add-revenue`
- **Type of API:** `System (requires X-User-Role: SYSTEM header)`
- **Request Body:**
  ```json
  {
    "amount": 100.50
  }
  ```
- **Response Body:** `200 OK`

---

## Store Settings Controller
**Base Path:** `/api/store/settings`

### 1. Get Store Settings
- **Method:** `GET`
- **Path:** `/api/store/settings/`
- **Type of API:** `Public`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "id": "UUID",
    "isAcceptingOrders": true,
    "createdAt": "2023-01-01T00:00:00",
    "updatedAt": "2023-01-01T00:00:00"
  }
  ```

### 2. Update Store Settings
- **Method:** `PUT`
- **Path:** `/api/store/settings/`
- **Type of API:** `Admin`
- **Request Body:**
  ```json
  {
    "isAcceptingOrders": false
  }
  ```
- **Response Body:** `200 OK`
  *(Same as Get Store Settings Response)*

---

## User Controller
**Base Path:** `/api/users`

### 1. Get User Profile
- **Method:** `GET`
- **Path:** `/api/users/profile`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "id": "UUID",
    "username": "string",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "phone": "string",
    "address": "string",
    "role": "string",
    "status": "string",
    "emailVerified": true,
    "lastLogin": "2023-01-01T00:00:00",
    "createdAt": "2023-01-01T00:00:00"
  }
  ```

### 2. Update User Profile
- **Method:** `PUT`
- **Path:** `/api/users/profile`
- **Type of API:** `User`
- **Request Body:**
  *(Same as Registration Request Body)*
- **Response Body:** `200 OK`
  *(Same as Get User Profile Response)*

### 3. Get User By ID
- **Method:** `GET`
- **Path:** `/api/users/{userId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Same as Get User Profile Response)*

### 4. Get All Users (Admin)
- **Method:** `GET`
- **Path:** `/api/users/admin/all`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  [
    {
      "id": "UUID",
      "username": "string"
      // ... User Profile fields
    }
  ]
  ```

### 5. Search Users (Admin)
- **Method:** `GET`
- **Path:** `/api/users/admin/search?query={string}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Array of User Profile objects)*

### 6. Get Users By Role (Admin)
- **Method:** `GET`
- **Path:** `/api/users/admin/role/{role}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Array of User Profile objects)*

### 7. Update User Role (Admin)
- **Method:** `PUT`
- **Path:** `/api/users/admin/{userId}/role`
- **Type of API:** `Admin`
- **Request Body:**
  ```json
  {
    "role": "ADMIN"
  }
  ```
- **Response Body:** `200 OK`
  ```json
  {
    "message": "User role updated successfully"
  }
  ```

### 8. Update User Status (Admin)
- **Method:** `PUT`
- **Path:** `/api/users/admin/{userId}/status`
- **Type of API:** `Admin`
- **Request Body:**
  ```json
  {
    "status": "ACTIVE"
  }
  ```
- **Response Body:** `200 OK`
  ```json
  {
    "message": "User status updated successfully"
  }
  ```

### 9. Unlock User Account (Admin)
- **Method:** `POST`
- **Path:** `/api/users/admin/{userId}/unlock`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "message": "User account unlocked successfully"
  }
  ```

### 10. Delete User (Admin)
- **Method:** `DELETE`
- **Path:** `/api/users/admin/{userId}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "message": "User deleted successfully"
  }
  ```

### 11. Get User Statistics (Admin)
- **Method:** `GET`
- **Path:** `/api/users/admin/statistics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "totalUsers": 100,
    "activeUsers": 90,
    "lockedUsers": 5
  }
  ```
  *(Map of string to long)*

### 12. Get Dashboard Statistics (Admin)
- **Method:** `GET`
- **Path:** `/api/users/admin/dashboard-stats?timeframe={string}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "revenue": 5000.0,
    "orders": 120
  }
  ```
  *(Map of string to object representing dashboard stats)*

---

## User Address Controller
**Base Path:** `/api/users/addresses`

### 1. Get Current User Addresses
- **Method:** `GET`
- **Path:** `/api/users/addresses`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  [
    {
      "id": "UUID",
      "title": "Home",
      "addressLine": "123 Main St",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "62701",
      "country": "USA",
      "isDefault": true
    }
  ]
  ```

### 2. Add New Address
- **Method:** `POST`
- **Path:** `/api/users/addresses`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "title": "Home",
    "addressLine": "123 Main St",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA",
    "isDefault": true
  }
  ```
- **Response Body:** `200 OK`
  *(Same as Address Response Object)*

### 3. Update Address
- **Method:** `PUT`
- **Path:** `/api/users/addresses/{addressId}`
- **Type of API:** `User`
- **Request Body:**
  *(Same as Add Address Request Body)*
- **Response Body:** `200 OK`
  *(Same as Address Response Object)*

### 4. Delete Address
- **Method:** `DELETE`
- **Path:** `/api/users/addresses/{addressId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`

### 5. Set Default Address
- **Method:** `PUT`
- **Path:** `/api/users/addresses/{addressId}/default`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`

