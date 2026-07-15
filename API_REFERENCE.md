# Bakery Auth Service API Reference

This document provides a comprehensive reference for the API endpoints exposed by the Bakery Auth Service.

---

## Auth Controller
**Base Path:** `/api/auth`

### 1. User Registration
- **Method:** `POST`
- **Path:** `/api/auth/register`
- **Type of API:** `Public`
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
- **Response Body:** `201 Created`
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
- **Path:** `/api/auth/login`
- **Type of API:** `Public`
- **Request Body:**
  ```json
  {
    "usernameOrEmail": "string", // Required
    "password": "string"         // Required
  }
  ```
- **Response Body:** `200 OK`
  *(Same as Registration Response)*

### 3. Refresh Token (Header)
- **Method:** `POST`
- **Path:** `/api/auth/refresh`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Same as Registration Response)*

### 4. Refresh Token (Body)
- **Method:** `POST`
- **Path:** `/api/auth/refresh-token`
- **Type of API:** `Public`
- **Request Body:**
  ```json
  {
    "refreshToken": "string"
  }
  ```
- **Response Body:** `200 OK`
  *(Same as Registration Response)*

### 5. Validate Token (Header)
- **Method:** `POST`
- **Path:** `/api/auth/validate`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
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
- **Path:** `/api/auth/validate-token`
- **Type of API:** `Public`
- **Request Body:**
  ```json
  {
    "token": "string"
  }
  ```
- **Response Body:** `200 OK`
  *(Same as Validate Token Header Response)*

### 7. Logout
- **Method:** `POST`
- **Path:** `/api/auth/logout`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "message": "Logout successful"
  }
  ```

### 8. Change Password
- **Method:** `POST`
- **Path:** `/api/auth/change-password`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "currentPassword": "string",
    "newPassword": "string"
  }
  ```
- **Response Body:** `200 OK`
  ```json
  {
    "message": "Password changed successfully"
  }
  ```

### 9. Verify Email
- **Method:** `POST`
- **Path:** `/api/auth/verify-email/{userId}`
- **Type of API:** `Public`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "message": "Email verified successfully"
  }
  ```

### 10. Health Check
- **Method:** `GET`
- **Path:** `/api/auth/health`
- **Type of API:** `Public`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "status": "UP",
    "service": "bakery-auth-service",
    "timestamp": "2023-01-01T00:00:00"
  }
  ```

### 11. Get Current User Info
- **Method:** `GET`
- **Path:** `/api/auth/me`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
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
- **Path:** `/api/users/internal/stats/increment-orders`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`

### 2. Decrement Orders
- **Method:** `POST`
- **Path:** `/api/users/internal/stats/decrement-orders`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`

### 3. Add Revenue
- **Method:** `POST`
- **Path:** `/api/users/internal/stats/add-revenue`
- **Type of API:** `Admin`
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
