# How to Import Postman Collection

## Import

1. Open Postman.
2. Click **Import**.
3. Select `Media_API_Collection.postman_collection.json` from the project root.

## Run Locally

```bash
cd /Users/tope/Documents/github_repos/media
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080`.

## Auth Model Used by This Collection

- `POST /api/auth/signup` and `POST /api/auth/login` set an `httpOnly` cookie (`jwt`) via `Set-Cookie`.
- The response body intentionally returns `"token": null`.
- Postman stores cookies automatically per domain, so authenticated requests work after login without manually setting `Authorization`.

## Environment Variables

| Variable | Purpose | Example |
|----------|---------|---------|
| `base_url` | API base URL | `http://localhost:8080` |
| `username` | Username from auth responses | `john_doe` |
| `media_id` | ID of created media | `1` |

## Suggested Test Flow

1. `Authentication -> Sign Up`
2. `Authentication -> Login`
3. `Media Management -> Create Media`
4. `Media Management -> Get All Media`
5. `Media Management -> Update Media`
6. `Media Management -> Delete Media`
7. `Authentication -> Logout`

## Troubleshooting

### 401 Unauthorized
- Run login first in the same Postman session/workspace.
- Confirm Postman has a `jwt` cookie for `localhost:8080`.
- If needed for API-client testing, send `Authorization: Bearer <token>` manually.

### H2 Console
- H2 console is disabled by default in secure config.
- Enable locally in untracked `application-local.yaml` when needed.
