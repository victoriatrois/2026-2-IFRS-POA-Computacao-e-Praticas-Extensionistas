# IFRS POA - Computing and Extension Practices (MNR Evaluation)

University extension project by IFRS Campus Porto Alegre dedicated to the evaluation and management of the National Robotics Fair (*Mostra Nacional de Robótica - MNR*).

---

## 🚀 Tech Stack

- **Backend**: Java 21, Spring Boot, Spring Data JPA, Hibernate, Flyway Migrations, Maven
- **Database**: PostgreSQL 17
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
     cd back/avaliacao-mnr
     ./mvnw spring-boot:run
     ```
   - **Windows PowerShell:**
     ```powershell
     cd back\avaliacao-mnr
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

Database settings are configured in `back/avaliacao-mnr/src/main/resources/application.yaml` and can be overridden using environment variables:

| Variable | Default (Local) | Description |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/avaliacao_mnr_db` | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `PORT` | `8080` | HTTP server port |

---

## 📁 Project Structure

```text
├── back/
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
