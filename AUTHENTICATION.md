# Authentication API Documentation

## Overview
This application provides JWT-based authentication with Sign Up, Login, and Logout functionality.

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
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Sign up successful"
}
```

**Response (Error - 400):**
```json
{
  "token": null,
  "username": null,
  "email": null,
  "message": "Username already taken" OR "Email already registered"
}
```

---

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
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Login successful"
}
```

**Response (Error - 401):**
```json
{
  "token": null,
  "username": null,
  "email": null,
  "message": "Invalid username or password"
}
```

---

### 3. Logout
**Endpoint:** `POST /api/auth/logout`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (Success - 200):**
```json
{
  "token": null,
  "username": null,
  "email": null,
  "message": "Logout successful"
}
```

---

## Database Configuration

The application uses Oracle Database. Update the `application.yaml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:xe
    username: system
    password: oracle
```

The database tables will be auto-created on application startup (ddl-auto: create-drop).

---

## Security Features

- Passwords are encrypted using BCrypt
- JWT tokens for stateless authentication
- Token expiration: 24 hours (configurable)
- Username and email uniqueness validation

---

## Media API

The application also includes a Media API with CRUD operations:

- `GET /api/media` - Get all media
- `GET /api/media/{id}` - Get media by ID
- `POST /api/media` - Create new media
- `PUT /api/media/{id}` - Update media
- `DELETE /api/media/{id}` - Delete media

