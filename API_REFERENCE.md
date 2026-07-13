# Bakery Auth Service API Reference

This document provides a comprehensive reference for the API endpoints exposed by the Bakery Auth Service.

---

## Table of Contents

1. [Auth Controller](#auth-controller)
2. [Internal Statistics Controller](#internal-statistics-controller)
3. [Store Settings Controller](#store-settings-controller)
4. [User Controller](#user-controller)

---

## Auth Controller
**Base Path:** `/api/auth`

### 1. User Registration
- **Method:** `POST`
- **Path:** `/register`
- **Request Body:**
  ```json
  {
    "username": "string",      // Required (3-50 chars)
    "email": "string",         // Required (Valid email)
    "password": "string",      // Required (Min 6 chars)
    "firstName": "string",     // Optional (Max 50 chars)
    "lastName": "string",      // Optional (Max 50 chars)
    "phone": "string",         // Optional (Max 15 chars)
    "address": "string"        // Optional
  }
  ```
- **Response:** `201 Created`
  ```json
  {
    "access_token": "string",
    "refresh_token": "string",
    "token_type": "Bearer",
    "expires_in": 3600,
    "user": {
      "id": "UUID",
      "username": "string",
      "email": "string",
      "firstName": "string",
      "lastName": "string",
      "phone": "string",
      "role": "string",
      "createdAt": "2023-01-01T00:00:00"
    }
  }
  ```

### 2. User Login
- **Method:** `POST`
- **Path:** `/login`
- **Request Body:**
  ```json
  {
    "usernameOrEmail": "string", // Required
    "password": "string"         // Required
  }
  ```
- **Response:** `200 OK`
  *(Same as Registration Response)*

### 3. Refresh Token (Header)
- **Method:** `POST`
- **Path:** `/refresh`
- **Headers:** `Authorization: Bearer <refresh_token>`
- **Response:** `200 OK`
  *(Same as Registration Response)*

### 4. Refresh Token (Body)
- **Method:** `POST`
- **Path:** `/refresh-token`
- **Request Body:**
  ```json
  {
    "refreshToken": "string"
  }
  ```
- **Response:** `200 OK`
  *(Same as Registration Response)*

### 5. Validate Token (Header)
- **Method:** `POST`
- **Path:** `/validate`
- **Headers:** `Authorization: Bearer <access_token>`
- **Response:** `200 OK`
  ```json
  {
    "valid": true,
    "message": "string",
    "userId": "UUID",       // If valid
    "username": "string",   // If valid
    "email": "string",      // If valid
    "role": "string"        // If valid
  }
  ```

### 6. Validate Token (Body)
- **Method:** `POST`
- **Path:** `/validate-token`
- **Request Body:**
  ```json
  {
    "token": "string"
  }
  ```
- **Response:** `200 OK`
  *(Same as Validate Token Header Response)*

### 7. Logout
- **Method:** `POST`
- **Path:** `/logout`
- **Headers:** `Authorization: Bearer <access_token>`
- **Response:** `200 OK`
  ```json
  {
    "message": "Logout successful"
  }
  ```

### 8. Change Password
- **Method:** `POST`
- **Path:** `/change-password`
- **Headers:** `Authorization: Bearer <access_token>`
- **Request Body:**
  ```json
  {
    "currentPassword": "string",
    "newPassword": "string"
  }
  ```
- **Response:** `200 OK`
  ```json
  {
    "message": "Password changed successfully"
  }
  ```

### 9. Verify Email
- **Method:** `POST`
- **Path:** `/verify-email/{userId}`
- **Response:** `200 OK`
  ```json
  {
    "message": "Email verified successfully"
  }
  ```

### 10. Health Check
- **Method:** `GET`
- **Path:** `/health`
- **Response:** `200 OK`
  ```json
  {
    "status": "UP",
    "service": "bakery-auth-service",
    "timestamp": "2023-01-01T00:00:00"
  }
  ```

### 11. Get Current User Info
- **Method:** `GET`
- **Path:** `/me`
- **Headers:** `Authorization: Bearer <access_token>`
- **Response:** `200 OK`
  ```json
  {
    "userId": "UUID",
    "username": "string",
    "email": "string",
    "role": "string"
  }
  ```

---

## Internal Statistics Controller
**Base Path:** `/api/users/internal/stats`

### 1. Increment Orders
- **Method:** `POST`
- **Path:** `/increment-orders`
- **Headers:** `X-User-Role: SYSTEM`
- **Response:** `200 OK`

### 2. Decrement Orders
- **Method:** `POST`
- **Path:** `/decrement-orders`
- **Headers:** `X-User-Role: SYSTEM`
- **Response:** `200 OK`

### 3. Add Revenue
- **Method:** `POST`
- **Path:** `/add-revenue`
- **Headers:** `X-User-Role: SYSTEM`
- **Request Body:**
  ```json
  {
    "amount": 100.50
  }
  ```
- **Response:** `200 OK`

---

## Store Settings Controller
**Base Path:** `/api/store/settings`

### 1. Get Store Settings
- **Method:** `GET`
- **Path:** `/`
- **Response:** `200 OK`
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
- **Path:** `/`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Request Body:**
  ```json
  {
    "isAcceptingOrders": false
  }
  ```
- **Response:** `200 OK`
  *(Same as Get Store Settings Response)*

---

## User Controller
**Base Path:** `/api/users`

### 1. Get User Profile
- **Method:** `GET`
- **Path:** `/profile`
- **Headers:** `Authorization: Bearer <access_token>`
- **Response:** `200 OK`
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
- **Path:** `/profile`
- **Headers:** `Authorization: Bearer <access_token>`
- **Request Body:**
  *(Same as Registration Request Body)*
- **Response:** `200 OK`
  *(Same as Get User Profile Response)*

### 3. Get User By ID
- **Method:** `GET`
- **Path:** `/{userId}`
- **Headers:** `Authorization: Bearer <access_token>` (Self or Admin)
- **Response:** `200 OK`
  *(Same as Get User Profile Response)*

### 4. Get All Users (Admin)
- **Method:** `GET`
- **Path:** `/admin/all`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
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
- **Path:** `/admin/search?query={string}`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
  *(Array of User Profile objects)*

### 6. Get Users By Role (Admin)
- **Method:** `GET`
- **Path:** `/admin/role/{role}`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
  *(Array of User Profile objects)*

### 7. Update User Role (Admin)
- **Method:** `PUT`
- **Path:** `/admin/{userId}/role`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Request Body:**
  ```json
  {
    "role": "ADMIN"
  }
  ```
- **Response:** `200 OK`
  ```json
  {
    "message": "User role updated successfully"
  }
  ```

### 8. Update User Status (Admin)
- **Method:** `PUT`
- **Path:** `/admin/{userId}/status`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Request Body:**
  ```json
  {
    "status": "ACTIVE"
  }
  ```
- **Response:** `200 OK`
  ```json
  {
    "message": "User status updated successfully"
  }
  ```

### 9. Unlock User Account (Admin)
- **Method:** `POST`
- **Path:** `/admin/{userId}/unlock`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
  ```json
  {
    "message": "User account unlocked successfully"
  }
  ```

### 10. Delete User (Admin)
- **Method:** `DELETE`
- **Path:** `/admin/{userId}`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
  ```json
  {
    "message": "User deleted successfully"
  }
  ```

### 11. Get User Statistics (Admin)
- **Method:** `GET`
- **Path:** `/admin/statistics`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
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
- **Path:** `/admin/dashboard-stats?timeframe={string}`
- **Headers:** `Authorization: Bearer <access_token>` (Admin Role Required)
- **Response:** `200 OK`
  ```json
  {
    "revenue": 5000.0,
    "orders": 120
  }
  ```
  *(Map of string to object representing dashboard stats)*
