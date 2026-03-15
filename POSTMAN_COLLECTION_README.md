# How to Import Postman Collection

## 📥 Download and Import Steps

### Step 1: Download the Collection File
The collection file `Media_API_Collection.postman_collection.json` is located in the root directory of the project.

### Step 2: Open Postman
- Open Postman desktop application
- If you don't have it, download from: https://www.postman.com/downloads/

### Step 3: Import the Collection
**Option A: Using Import Button**
1. Click **Import** button (top left area)
2. Click **Upload Files**
3. Select `Media_API_Collection.postman_collection.json`
4. Click **Open** and then **Import**

**Option B: Drag and Drop**
1. Drag `Media_API_Collection.postman_collection.json` file directly into Postman window

## 🚀 Using the Collection

### Step 1: Start Your Application
```bash
cd /Users/tope/Documents/github_repos/media
mvn spring-boot:run
```
The application will start on `http://localhost:8080`

### Step 2: Verify Environment Variables
1. In Postman, click the **Environment** dropdown (top right)
2. Select **Media API Collection** or the environment you imported
3. Verify these variables:
   - `base_url`: `http://localhost:8080`
   - `jwt_token`: (will be auto-filled after login)
   - `username`: (will be auto-filled after login)
   - `media_id`: `1` (default, update after creating media)

### Step 3: Test the API Workflow

#### 1. **Sign Up** (Create new account)
   - Select: Authentication → Sign Up
   - Update the request body with your desired username, email, password
   - Click **Send**
   - JWT token is automatically saved to environment

#### 2. **Login** (Get JWT token)
   - Select: Authentication → Login
   - Use the credentials from Sign Up
   - Click **Send**
   - JWT token is automatically saved to environment

#### 3. **Create Media** (Add new media item)
   - Select: Media Management → Create Media
   - Update the title, description, and URL as needed
   - Click **Send**
   - Media ID is automatically saved to environment

#### 4. **Get All Media** (View all items)
   - Select: Media Management → Get All Media
   - Click **Send**

#### 5. **Get Media by ID** (View specific item)
   - Select: Media Management → Get Media by ID
   - Click **Send** (uses `media_id` from environment)

#### 6. **Update Media** (Edit existing item)
   - Select: Media Management → Update Media
   - Update the request body with new values
   - Click **Send** (uses `media_id` from environment)

#### 7. **Delete Media** (Remove item)
   - Select: Media Management → Delete Media
   - Click **Send** (uses `media_id` from environment)

#### 8. **Logout** (End session)
   - Select: Authentication → Logout
   - Click **Send**
   - JWT token is used from environment

## 📊 Included Endpoints

### Authentication (3 endpoints)
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Authenticate and get JWT token
- `POST /api/auth/logout` - End user session

### Media Management (5 endpoints)
- `GET /api/media` - Get all media
- `GET /api/media/{id}` - Get specific media
- `POST /api/media` - Create new media
- `PUT /api/media/{id}` - Update media
- `DELETE /api/media/{id}` - Delete media

### Database Console (1 endpoint)
- `GET /h2-console` - Access H2 database web console

## 🔑 Environment Variables Explained

| Variable | Purpose | Example |
|----------|---------|---------|
| `base_url` | API base URL | `http://localhost:8080` |
| `jwt_token` | JWT authentication token (auto-filled after login) | `eyJhbGc...` |
| `username` | Current logged-in username (auto-filled after login) | `john_doe` |
| `media_id` | ID of created/selected media (auto-filled after creation) | `1` |

## ✅ Expected Responses

| Endpoint | Status Code | Description |
|----------|-------------|-------------|
| Sign Up | 201 Created | New user registered successfully |
| Login | 200 OK | Authentication successful |
| Logout | 200 OK | Session ended |
| Get All Media | 200 OK | List of media items returned |
| Get Media by ID | 200 OK | Specific media item returned |
| Create Media | 201 Created | New media created |
| Update Media | 200 OK | Media updated successfully |
| Delete Media | 204 No Content | Media deleted successfully |

## 🐛 Troubleshooting

### "Connection refused" error
- Ensure the application is running: `mvn spring-boot:run`
- Check that port 8080 is not in use
- Verify `base_url` environment variable is correct

### "Unauthorized" (401) error
- Make sure you've logged in first to get the JWT token
- Token should be automatically saved to `jwt_token` environment variable
- If manual, use format: `Bearer <your_token_here>`

### "Not found" (404) error
- Verify the endpoint URL is correct
- Check that the API is running
- For ID-based requests, ensure the media ID exists

### Database errors
- Access H2 console: `http://localhost:8080/h2-console`
- Login with username: `sa`, password: (empty)
- Check if tables exist: `SELECT * FROM USERS;`

## 📝 Notes

- All timestamps are in milliseconds (Unix epoch)
- JWT tokens expire after 24 hours (86400000 ms)
- H2 database is in-memory, data resets when application stops
- For production, replace with PostgreSQL or MySQL

## 🔗 Additional Resources

- Postman Documentation: https://learning.postman.com/
- Media Application H2 Setup: See `H2_DATABASE_SETUP.md`
- Spring Boot Documentation: https://spring.io/projects/spring-boot

