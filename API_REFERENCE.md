# bakery_auth_service API Report

## AuthController

### `POST` `/api/auth/register`
- **API Name:** register
- **Type:** REST / Synchronous

**Request:**
```json
{
  "username": "String - Username (3-50 chars)",
  "email": "String - Valid email format",
  "password": "String - Password (min 6 chars)",
  "firstName": "String - First name (optional, max 50 chars)",
  "lastName": "String - Last name (optional, max 50 chars)",
  "phone": "String - Phone number (optional, max 15 chars)",
  "address": "String - Address (optional)"
}
```

**Response:**
```json
{
  "access_token": "String - JWT Access Token",
  "refresh_token": "String - JWT Refresh Token",
  "token_type": "String - Always 'Bearer'",
  "expires_in": "Long - Expiration time in seconds",
  "user": {
    "id": "UUID",
    "username": "String",
    "email": "String",
    "firstName": "String",
    "lastName": "String",
    "phone": "String",
    "role": "String (e.g., ADMIN, USER, STAFF)",
    "createdAt": "DateTime"
  }
}
```

---

### `POST` `/api/auth/login`
- **API Name:** login
- **Type:** REST / Synchronous

**Request:**
```json
{
  "usernameOrEmail": "String - Registered username or email",
  "password": "String - User's password"
}
```

**Response:**
```json
{
  "access_token": "String",
  "refresh_token": "String",
  "token_type": "String",
  "expires_in": "Long",
  "user": {
    "id": "UUID",
    "username": "String",
    "email": "String",
    "firstName": "String",
    "lastName": "String",
    "phone": "String",
    "role": "String",
    "createdAt": "DateTime"
  }
}
```

---

### `POST` `/api/auth/refresh`
- **API Name:** refreshToken
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <refresh_token>`

**Request:**
None (Uses Authorization Header)

**Response:**
*(Same as Login Response above)*
```json
{
  "access_token": "String",
  "refresh_token": "String",
  "token_type": "String",
  "expires_in": "Long",
  "user": { ... }
}
```

---

### `POST` `/api/auth/refresh-token`
- **API Name:** refreshTokenFromBody
- **Type:** REST / Synchronous

**Request:**
```json
{
  "refreshToken": "String - The refresh token"
}
```

**Response:**
*(Same as Login Response above)*

---

### `POST` `/api/auth/validate`
- **API Name:** validateToken
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <access_token>`

**Request:**
None (Uses Authorization Header)

**Response:**
```json
{
  "valid": "Boolean - True if valid, false otherwise",
  "message": "String - Status message",
  "userId": "UUID - (Present if valid)",
  "username": "String - (Present if valid)",
  "email": "String - (Present if valid)",
  "role": "String - (Present if valid)"
}
```

---

### `POST` `/api/auth/validate-token`
- **API Name:** validateTokenFromBody
- **Type:** REST / Synchronous

**Request:**
```json
{
  "token": "String - The JWT access token to validate"
}
```

**Response:**
*(Same as validateToken Response above)*

---

### `POST` `/api/auth/logout`
- **API Name:** logout
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <access_token>`

**Request:**
None (Uses Authorization Header)

**Response:**
```json
{
  "message": "String - E.g., 'Logout successful'"
}
```

---

### `POST` `/api/auth/change-password`
- **API Name:** changePassword
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <access_token>`

**Request:**
```json
{
  "currentPassword": "String",
  "newPassword": "String"
}
```

**Response:**
```json
{
  "message": "String - E.g., 'Password changed successfully'"
}
```

---

### `POST` `/api/auth/verify-email/{userId}`
- **API Name:** verifyEmail
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)

**Request:**
None

**Response:**
```json
{
  "message": "String - E.g., 'Email verified successfully'"
}
```

---

### `GET` `/api/auth/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - E.g., 'UP'",
  "service": "String - 'bakery-auth-service'",
  "timestamp": "DateTime"
}
```

---

### `GET` `/api/auth/me`
- **API Name:** getCurrentUser
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <access_token>`

**Request:**
None

**Response:**
```json
{
  "userId": "UUID",
  "username": "String",
  "email": "String",
  "role": "String"
}
```

---

## UserController

### `GET` `/api/users/profile`
- **API Name:** getUserProfile
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <access_token>`

**Request:**
None

**Response:**
```json
{
  "id": "UUID",
  "username": "String",
  "email": "String",
  "firstName": "String",
  "lastName": "String",
  "phone": "String",
  "address": "String",
  "role": "String (e.g., USER, ADMIN)",
  "status": "String (e.g., ACTIVE, INACTIVE, LOCKED)",
  "emailVerified": "Boolean",
  "lastLogin": "DateTime",
  "createdAt": "DateTime"
}
```

---

### `PUT` `/api/users/profile`
- **API Name:** updateUserProfile
- **Type:** REST / Synchronous
- **Request Headers:** `Authorization: Bearer <access_token>`

**Request:**
```json
{
  "username": "String - Username (3-50 chars)",
  "email": "String - Valid email format",
  "password": "String - Password (min 6 chars)",
  "firstName": "String - First name (optional, max 50 chars)",
  "lastName": "String - Last name (optional, max 50 chars)",
  "phone": "String - Phone number (optional, max 15 chars)",
  "address": "String - Address (optional)"
}
```

**Response:**
*(Same as getUserProfile Response above)*

---

### `GET` `/api/users/{userId}`
- **API Name:** getUserById
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)

**Request:**
None

**Response:**
*(Same as getUserProfile Response above)*

---

### `GET` `/api/users/admin/all`
- **API Name:** getAllUsers
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN

**Request:**
None

**Response:**
```json
[
  {
    "id": "UUID",
    "username": "String",
    "email": "String",
    "firstName": "String",
    "lastName": "String",
    "phone": "String",
    "address": "String",
    "role": "String",
    "status": "String",
    "emailVerified": "Boolean",
    "lastLogin": "DateTime",
    "createdAt": "DateTime"
  }
]
```

---

### `GET` `/api/users/admin/search`
- **API Name:** searchUsers
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Query Parameter:** `query` (String)

**Request:**
None

**Response:**
*(Array of UserResponse, same as getAllUsers)*

---

### `GET` `/api/users/admin/role/{role}`
- **API Name:** getUsersByRole
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Path Variable:** `role` (String)

**Request:**
None

**Response:**
*(Array of UserResponse, same as getAllUsers)*

---

### `PUT` `/api/users/admin/{userId}/role`
- **API Name:** updateUserRole
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Path Variable:** `userId` (UUID)

**Request:**
```json
{
  "role": "String - (e.g., ADMIN, USER, STAFF)"
}
```

**Response:**
```json
{
  "message": "String - E.g., 'User role updated successfully'"
}
```

---

### `PUT` `/api/users/admin/{userId}/status`
- **API Name:** updateUserStatus
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Path Variable:** `userId` (UUID)

**Request:**
```json
{
  "status": "String - (e.g., ACTIVE, INACTIVE, LOCKED)"
}
```

**Response:**
```json
{
  "message": "String - E.g., 'User status updated successfully'"
}
```

---

### `POST` `/api/users/admin/{userId}/unlock`
- **API Name:** unlockUserAccount
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Path Variable:** `userId` (UUID)

**Request:**
None

**Response:**
```json
{
  "message": "String - E.g., 'User account unlocked successfully'"
}
```

---

### `DELETE` `/api/users/admin/{userId}`
- **API Name:** deleteUser
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Path Variable:** `userId` (UUID)

**Request:**
None

**Response:**
```json
{
  "message": "String - E.g., 'User deleted successfully'"
}
```

---

### `GET` `/api/users/admin/statistics`
- **API Name:** getUserStatistics
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN

**Request:**
None

**Response:**
```json
{
  "TOTAL_USERS": "Long",
  "ACTIVE_USERS": "Long",
  "ADMIN_USERS": "Long",
  "NEW_USERS_THIS_MONTH": "Long"
}
```
*(Keys might vary based on actual statistics Map returned)*
