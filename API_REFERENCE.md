# API Reference

## Authentication Endpoints
Controller: [`AuthController`](./src/main/java/com/blubugtech/bakery_auth_service/controller/publicapi/AuthController.java)

### Initiate registration
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890",
  "address": "123 Main St"
}
```

**Response Body:**
```json
{
  "message": "OTP Sent. Mock OTP: 123456"
}
```

### Verify registration OTP
**POST** `/api/auth/register/verify`

**Request Body:**
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response Body:**
```json
{
  "access_token": "jwt_access_token",
  "refresh_token": "jwt_refresh_token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "role": "USER",
    "createdAt": "2023-10-10T10:00:00"
  }
}
```

### Resend registration OTP
**POST** `/api/auth/register/resend`

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Response Body:**
```json
{
  "message": "OTP Sent. Mock OTP: 123456"
}
```

### Login user
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}
```

**Response Body:**
```json
{
  "requiresOtp": true,
  "message": "OTP sent to email",
  "authResponse": null
}
```

### Login admin
**POST** `/api/auth/admin/login`

**Request Body:**
```json
{
  "usernameOrEmail": "admin",
  "password": "adminpassword"
}
```

**Response Body:**
```json
{
  "requiresOtp": true,
  "message": "OTP sent to admin email",
  "authResponse": null
}
```

### Verify admin login
**POST** `/api/auth/admin/login/verify`

**Request Body:**
```json
{
  "usernameOrEmail": "admin",
  "otp": "123456"
}
```

**Response Body:**
```json
{
  "access_token": "jwt_access_token",
  "refresh_token": "jwt_refresh_token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "admin",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "phone": "1234567890",
    "role": "ADMIN",
    "createdAt": "2023-10-10T10:00:00"
  }
}
```

### Verify user login
**POST** `/api/auth/login/verify`

**Request Body:**
```json
{
  "usernameOrEmail": "johndoe",
  "otp": "123456"
}
```

**Response Body:**
```json
{
  "access_token": "jwt_access_token",
  "refresh_token": "jwt_refresh_token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "role": "USER",
    "createdAt": "2023-10-10T10:00:00"
  }
}
```

### Resend login OTP
**POST** `/api/auth/login/resend`

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Response Body:**
```json
{
  "message": "OTP Sent. Mock OTP: 123456"
}
```

### Initiate forgot password
**POST** `/api/auth/forgot-password`

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Response Body:**
```json
{
  "message": "OTP Sent. Mock OTP: 123456"
}
```

### Reset password
**POST** `/api/auth/forgot-password/reset`

**Request Body:**
```json
{
  "email": "john@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

**Response Body:**
```json
{
  "message": "Password reset successfully"
}
```

### Refresh token
**POST** `/api/auth/refresh`

**Response Body:**
```json
{
  "access_token": "new_jwt_access_token",
  "refresh_token": "new_jwt_refresh_token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "role": "USER",
    "createdAt": "2023-10-10T10:00:00"
  }
}
```

### Validate token
**POST** `/api/auth/validate`

**Response Body:**
```json
{
  "valid": true,
  "message": "Valid",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

### Logout
**POST** `/api/auth/logout`

**Response Body:**
```json
{
  "message": "Logout successful"
}
```

### Change password
**POST** `/api/auth/change-password`

**Request Body:**
```json
{
  "currentPassword": "password123",
  "newPassword": "newpassword123"
}
```

**Response Body:**
```json
{
  "message": "Password changed successfully"
}
```

### Verify email
**POST** `/api/auth/verify-email/{userId}`

**Response Body:**
```json
{
  "message": "Email verified successfully"
}
```

### Get current user info from token
**GET** `/api/auth/me`

**Response Body:**
```json
{
  "valid": true,
  "message": "Valid",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

---

## User Profile Endpoints
Controller: [`PublicUserController`](./src/main/java/com/blubugtech/bakery_auth_service/controller/publicapi/PublicUserController.java)

### Get current user profile
**GET** `/api/users/profile`

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890",
  "address": "123 Main St",
  "role": "USER",
  "status": "ACTIVE",
  "emailVerified": true,
  "lastLogin": "2023-10-10T10:00:00",
  "twoFactorEnabled": true,
  "loginNotificationsEnabled": false,
  "createdAt": "2023-10-01T10:00:00"
}
```

### Update user profile
**PUT** `/api/users/profile`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "Johnny",
  "lastName": "Doe",
  "phone": "0987654321",
  "address": "456 Side St",
  "twoFactorEnabled": true,
  "loginNotificationsEnabled": true
}
```

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "Johnny",
  "lastName": "Doe",
  "phone": "0987654321",
  "address": "456 Side St",
  "role": "USER",
  "status": "ACTIVE",
  "emailVerified": true,
  "lastLogin": "2023-10-10T10:00:00",
  "twoFactorEnabled": true,
  "loginNotificationsEnabled": true,
  "createdAt": "2023-10-01T10:00:00"
}
```

---

## User Address Endpoints
Controller: [`UserAddressController`](./src/main/java/com/blubugtech/bakery_auth_service/controller/publicapi/UserAddressController.java)

### Get all addresses
**GET** `/api/users/addresses`

**Response Body:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "title": "Home",
      "addressLine": "123 Main St",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "62701",
      "country": "USA",
      "isDefault": true,
      "zipCode": "62701"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1
}
```

### Add an address
**POST** `/api/users/addresses`

**Request Body:**
```json
{
  "title": "Work",
  "addressLine": "456 Office Rd",
  "city": "Springfield",
  "state": "IL",
  "postalCode": "62701",
  "country": "USA",
  "isDefault": false,
  "zipCode": "62701"
}
```

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174001",
  "title": "Work",
  "addressLine": "456 Office Rd",
  "city": "Springfield",
  "state": "IL",
  "postalCode": "62701",
  "country": "USA",
  "isDefault": false,
  "zipCode": "62701"
}
```

### Update an address
**PUT** `/api/users/addresses/{addressId}`

**Request Body:**
```json
{
  "title": "Home",
  "addressLine": "123 Main St Apt 2",
  "city": "Springfield",
  "state": "IL",
  "postalCode": "62701",
  "country": "USA",
  "isDefault": true,
  "zipCode": "62701"
}
```

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Home",
  "addressLine": "123 Main St Apt 2",
  "city": "Springfield",
  "state": "IL",
  "postalCode": "62701",
  "country": "USA",
  "isDefault": true,
  "zipCode": "62701"
}
```

### Delete an address
**DELETE** `/api/users/addresses/{addressId}`

**Response Body:**
```json
{}
```

### Set address as default
**PUT** `/api/users/addresses/{addressId}/default`

**Response Body:**
```json
{}
```

---

## Store Settings Endpoints
Controller: [`StoreSettingsController`](./src/main/java/com/blubugtech/bakery_auth_service/controller/publicapi/StoreSettingsController.java)

### Get store settings
**GET** `/api/store/settings`

**Response Body:**
```json
{
  "isAcceptingOrders": true,
  "adminNotificationEmail": "admin@bakery.com"
}
```

### Update store settings (Admin only)
**PUT** `/api/store/settings`

**Request Body:**
```json
{
  "isAcceptingOrders": true,
  "adminNotificationEmail": "admin@bakery.com"
}
```

**Response Body:**
```json
{
  "isAcceptingOrders": true,
  "adminNotificationEmail": "admin@bakery.com"
}
```

---

## Admin User Management Endpoints
Controller: [`UserController`](./src/main/java/com/blubugtech/bakery_auth_service/controller/admin/UserController.java)

### Get user by ID
**GET** `/api/users/{userId}`

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890",
  "address": "123 Main St",
  "role": "USER",
  "status": "ACTIVE",
  "emailVerified": true,
  "lastLogin": "2023-10-10T10:00:00",
  "twoFactorEnabled": true,
  "loginNotificationsEnabled": false,
  "createdAt": "2023-10-01T10:00:00"
}
```

### Get all users
**GET** `/api/users/admin/all`

**Response Body:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "1234567890",
      "address": "123 Main St",
      "role": "USER",
      "status": "ACTIVE",
      "emailVerified": true,
      "lastLogin": "2023-10-10T10:00:00",
      "twoFactorEnabled": true,
      "loginNotificationsEnabled": false,
      "createdAt": "2023-10-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1
}
```

### Search users
**GET** `/api/users/admin/search?query=john`

**Response Body:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "1234567890",
      "address": "123 Main St",
      "role": "USER",
      "status": "ACTIVE",
      "emailVerified": true,
      "lastLogin": "2023-10-10T10:00:00",
      "twoFactorEnabled": true,
      "loginNotificationsEnabled": false,
      "createdAt": "2023-10-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1
}
```

### Get users by role
**GET** `/api/users/admin/role/{role}`

**Response Body:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "1234567890",
      "address": "123 Main St",
      "role": "USER",
      "status": "ACTIVE",
      "emailVerified": true,
      "lastLogin": "2023-10-10T10:00:00",
      "twoFactorEnabled": true,
      "loginNotificationsEnabled": false,
      "createdAt": "2023-10-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1
}
```

### Update user role
**PUT** `/api/users/admin/{userId}/role`

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

**Response Body:**
```json
{
  "message": "User role updated successfully"
}
```

### Update user status
**PUT** `/api/users/admin/{userId}/status`

**Request Body:**
```json
{
  "status": "ACTIVE"
}
```

**Response Body:**
```json
{
  "message": "User status updated successfully"
}
```

### Unlock user account
**POST** `/api/users/admin/{userId}/unlock`

**Response Body:**
```json
{
  "message": "User account unlocked successfully"
}
```

### Delete user
**DELETE** `/api/users/admin/{userId}`

**Response Body:**
```json
{
  "message": "User deleted successfully"
}
```

### Get user statistics
**GET** `/api/users/admin/statistics`

**Response Body:**
```json
{
  "totalUsers": 100,
  "TOTAL_USERS": 100,
  "activeUsers": 90,
  "verifiedUsers": 80,
  "adminUsers": 5
}
```

### Get dashboard statistics
**GET** `/api/users/admin/dashboard-stats?timeframe=1m`

**Response Body:**
```json
{
  "currentPeriodRevenue": 5000.00,
  "previousPeriodRevenue": 4000.00,
  "growthPercentage": 25.0,
  "totalUsers": 100,
  "activeOrders": 10,
  "totalRevenue": 50000.00,
  "timeframe": "1m",
  "chartData": [
    {
      "name": "2023-10-01",
      "revenue": 100.00
    }
  ]
}
```

---

## Internal Statistics Endpoints
Controller: [`InternalStatisticsController`](./src/main/java/com/blubugtech/bakery_auth_service/controller/internal/InternalStatisticsController.java)

### Increment total orders
**POST** `/api/users/internal/stats/increment-orders`

**Response Body:**
```json
{}
```

### Decrement total orders
**POST** `/api/users/internal/stats/decrement-orders`

**Response Body:**
```json
{}
```

### Add revenue
**POST** `/api/users/internal/stats/add-revenue`

**Request Body:**
```json
{
  "amount": 100.00
}
```

**Response Body:**
```json
{}
```
