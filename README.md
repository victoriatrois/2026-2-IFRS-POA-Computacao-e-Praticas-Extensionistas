# IFRS POA - Computing and Extension Practices (MNR Evaluation)

University extension project by IFRS Campus Porto Alegre dedicated to the evaluation and management of the National Robotics Fair (*Mostra Nacional de Robótica - MNR*).

---

## 🚀 Tech Stack

- **Backend**: Java 25, Spring Boot 4.1, Spring Data JPA, Hibernate, Flyway Migrations, Maven
- **Database**: PostgreSQL 18
- **Frontend**: Next.js (React), TypeScript, Tailwind CSS
- **Containerization**: Docker, Docker Compose

---

## 📋 Prerequisites & Requirements

Before getting started, make sure you have the following installed:

1. **[Docker Desktop](https://www.docker.com/products/docker-desktop/)** (with Docker Compose enabled)
2. *(Optional for local backend development)*: **Java JDK 25+**
3. *(Optional for local frontend development)*: **Node.js 20+** and npm/yarn/pnpm

---

## 🛠️ How to Run

### Option 1: Running the Complete Stack with Docker Compose (Recommended)

This option spins up both the PostgreSQL database and the Spring Boot backend inside interconnected containers.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/victoriatrois/2026-2-IFRS-POA-Computacao-e-Praticas-Extensionistas.git
   cd 2026-2-IFRS-POA-Computacao-e-Praticas-Extensionistas
   ```

2. **Start the containers (building the images):**
   ```bash
   docker compose up --build -d
   ```

3. **Follow the backend application logs:**
   ```bash
   docker compose logs -f backend
   ```

4. **Stop the containers:**
   ```bash
   docker compose down
   ```
   *(To wipe persisted database volumes and start completely fresh, use `docker compose down -v`)*

---

### Option 2: Local Development (PostgreSQL in Docker + Backend on Host/IDE)

If you prefer running and debugging the backend directly from your IDE (IntelliJ IDEA, VS Code, Eclipse) or via the command line:

1. **Start only the PostgreSQL database container:**
   ```bash
   docker compose up -d postgres
   ```

2. **Run the Backend via the Maven Wrapper:**
   - **Linux / macOS:**
     ```bash
     cd backend/avaliacao-mnr
     ./mvnw spring-boot:run
     ```
   - **Windows PowerShell:**
     ```powershell
     cd backend\avaliacao-mnr
     .\mvnw.cmd spring-boot:run
     ```

---

## 🧪 Testing the Database Connection & Migrations

The backend uses **Flyway** for automated database schema management and versioning. During startup, the initial migration script (`V1__create_tb_test.sql`) creates the table `tb_test` and inserts a sample record.

### 1. Test via the HTTP Endpoint

Once the backend is running, send a `GET` request to the test endpoint:

- **Browser**: Visit [http://localhost:8080/api/test](http://localhost:8080/api/test)
- **cURL**:
  ```bash
  curl http://localhost:8080/api/test
  ```
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/test"
  ```

**Expected JSON Response:**
```json
[
  {
    "id": 1,
    "description": "Database connection and Flyway migration test successful!",
    "createdAt": "2026-08-22T20:20:00Z"
  }
]
```

---

### 2. Direct Inspection inside PostgreSQL

You can also inspect the generated tables and data directly inside the PostgreSQL container:

```bash
docker exec -it postgres-avaliacao-mnr psql -U postgres -d avaliacao_mnr_db
```

Useful `psql` commands:
- List tables: `\dt`
- View migration history: `SELECT * FROM flyway_schema_history;`
- Query test data: `SELECT * FROM tb_test;`
- Exit: `\q`

---

## ⚙️ Environment Variables & Configuration

Database settings are configured in `backend/avaliacao-mnr/src/main/resources/application.yaml` and can be overridden using environment variables:

| Variable | Default (Local) | Description |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/avaliacao_mnr_db` | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `PORT` | `8080` | HTTP server port |
| `JWT_SECRET` | Local development fallback | HMAC signing secret; use a unique random value of at least 32 bytes outside local development |
| `JWT_ACCESS_TTL` | `PT15M` | Access token lifetime as an ISO-8601 duration |
| `JWT_REFRESH_TTL` | `P30D` | Refresh token lifetime as an ISO-8601 duration |
| `BOOTSTRAP_ADMIN_ENABLED` | `false` | Enables creation of the initial administrator when no user with that email exists |
| `BOOTSTRAP_ADMIN_NAME` | — | Initial administrator's first name |
| `BOOTSTRAP_ADMIN_SURNAME` | — | Initial administrator's surname |
| `BOOTSTRAP_ADMIN_EMAIL` | — | Initial administrator's email |
| `BOOTSTRAP_ADMIN_CPF` | — | Initial administrator's CPF |
| `BOOTSTRAP_ADMIN_PASSWORD` | — | Initial administrator's password (minimum 12 characters) |

### Local Authentication Setup

Create the local environment file from the template and generate a JWT secret:

```bash
cp .env.example .env
openssl rand -base64 32
```

Copy the generated value into `JWT_SECRET` in `.env`. Keep `BOOTSTRAP_ADMIN_ENABLED=false` unless you are creating the first administrator. For the initial setup, fill in the `BOOTSTRAP_ADMIN_*` values, set `BOOTSTRAP_ADMIN_ENABLED=true`, start the backend, and then set it back to `false` before restarting.

### Authentication API

See the [Authentication API documentation](documentation/authentication/authentication-api.md) for JWT endpoints, Swagger usage, administrator bootstrap, and audit behavior.

---

## 📁 Project Structure

```text
├── backend/
│   └── avaliacao-mnr/             # Spring Boot backend application
│       ├── Dockerfile             # Multi-stage Docker build
│       ├── pom.xml                # Maven project dependencies
│       └── src/
│           └── main/
│               ├── java/          # Java source code (controllers, models, repositories)
│               └── resources/
│                   ├── application.yaml
│                   └── db/migration/  # Flyway SQL migration scripts (V1, V2, ...)
├── frontend/                      # Next.js frontend application
├── docker-compose.yml             # PostgreSQL & Backend service orchestration
└── README.md
```
