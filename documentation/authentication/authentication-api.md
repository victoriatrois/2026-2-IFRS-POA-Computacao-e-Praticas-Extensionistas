# Authentication API

The backend uses stateless JWT access tokens and one-time rotating refresh tokens. Refresh tokens are stored as SHA-256 hashes; raw tokens are returned only to the client.

Interactive API documentation is available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). The raw OpenAPI document is available at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs). In Swagger UI, use **Authorize** and enter the access token returned by `POST /auth/login` to try protected endpoints.

- `POST /auth/login` — JSON body `{ "email": "...", "password": "..." }`; returns `accessToken`, `expiresIn`, `refreshToken`, and a safe user profile.
- `POST /auth/refresh` — JSON body `{ "refreshToken": "..." }`; rotates the refresh token and issues a new access token.
- `POST /auth/logout` — JSON body `{ "refreshToken": "..." }`; revokes that refresh token.
- `GET /auth/me` — requires `Authorization: Bearer <accessToken>`.
- `GET /users` and `POST /users` — ADMIN-only user management.

## Local Setup

Create the ignored local environment file and generate a signing secret:

```bash
cp .env.example .env
openssl rand -base64 32
```

Copy the generated value into `JWT_SECRET`. Keep `BOOTSTRAP_ADMIN_ENABLED=false` unless you are creating the first administrator. For the initial setup, fill in the `BOOTSTRAP_ADMIN_*` variables, set `BOOTSTRAP_ADMIN_ENABLED=true`, and recreate the backend:

```bash
docker-compose up -d backend
```

After the administrator has been created, set `BOOTSTRAP_ADMIN_ENABLED=false` and recreate the backend again. Never commit `.env` or real credentials.

For local frontend development before authentication is integrated, set `SPRING_PROFILES_ACTIVE=dev` in `.env`. The `dev` profile permits requests without JWT authentication, including project import, so the existing import screen can continue working. This bypass is strictly for local development; leave the variable empty or unset in staging and production.

## Calling Protected APIs

Log in to obtain an access token:

```bash
curl -X POST "http://localhost:8080/auth/login" \
	-H "Content-Type: application/json" \
	-d '{"email":"your-admin-email","password":"your-admin-password"}'
```

Use the `accessToken` from the response in the `Authorization` header. Do not use `JWT_SECRET` or `refreshToken` as a bearer token. Access tokens are JWTs with three dot-separated sections and expire according to `JWT_ACCESS_TTL`.

Example request to the protected video-validation endpoint:

```bash
curl -G "http://localhost:8080/videos/validate" \
	-H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
	--data-urlencode "url=https://www.youtube.com/watch?v=VIDEO_ID"
```

A `401 Unauthorized` response usually means the access token is missing, expired, malformed, or was signed with a different `JWT_SECRET`. If the signing secret is exposed, generate a new one and recreate the backend; existing access tokens will no longer be valid.

Audit rows are written for login success/failure, refresh, logout, and denied authorization attempts. Passwords and raw access/refresh tokens are never written to audit records.
