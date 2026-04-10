# Authentication API Documentation

## Overview
This API uses JWT authentication with an `httpOnly` cookie (`jwt`) for browser-style sessions.
You can also pass a bearer token in `Authorization` when needed by API clients.

## Endpoints

### 1. Sign Up
**Endpoint:** `POST /api/auth/signup`

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response (Success - 201):**
```json
{
  "token": null,
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Sign up successful"
}
```
On success, the JWT is returned as `Set-Cookie` (`jwt`, `httpOnly`) instead of response body token.

**Response (Error - 400):**
```json
{
  "token": null,
  "username": null,
  "email": null,
  "message": "Username already taken"
}
```

### 2. Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response (Success - 200):**
```json
{
  "token": null,
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Login successful"
}
```
On success, the JWT is returned as `Set-Cookie` (`jwt`, `httpOnly`).

**Response (Error - 401):**
```json
{
  "token": null,
  "username": null,
  "email": null,
  "message": "Invalid username or password"
}
```

### 3. Logout
**Endpoint:** `POST /api/auth/logout`

You may authenticate with:
- `Cookie: jwt=...` (default browser/Postman flow), or
- `Authorization: Bearer {token}`.

**Response (Success - 200):**
```json
{
  "token": null,
  "username": null,
  "email": null,
  "message": "Logout successful"
}
```
The `jwt` cookie is cleared on logout.

### 4. Current User
**Endpoint:** `GET /api/auth/me`

Returns the currently authenticated user from JWT context.

### 5. Password Reset
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

## Local Configuration Notes

The default setup uses H2 in-memory DB and requires a JWT secret:

```yaml
jwt:
  secret: ${JWT_SECRET}
```

For local development, create `application-local.yaml` (ignored by git) and override local-only settings there.

## Security Features

- Passwords are encrypted with BCrypt
- JWT expiration is configurable (`jwt.expiration`, default 24 hours)
- Logout invalidates JWT via in-memory token blacklist until expiry
- Login attempts are rate-limited per IP
