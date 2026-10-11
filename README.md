# 🛡️ Online Complaint Management System — Backend API

Enterprise-grade Spring Boot 3 RESTful API for the **Online Complaint Management System (CMS)**. Provides secure role-based access control (Admin, Staff, Citizen), end-to-end complaint lifecycle management, SLA monitoring, automated email alerts, and audit history.

---

## 🛠️ Tech Stack & Architecture

- **Framework**: Spring Boot 3.2.0 (Java 17 / 21)
- **Security**: Spring Security 6 with Stateless JWT Authentication & Refresh Tokens
- **ORM / Persistence**: Spring Data JPA & Hibernate 6
- **Database**: MySQL 8.x
- **Build Tool**: Apache Maven (Includes `./mvnw` wrapper)
- **Containerization**: Docker multi-stage build
- **Documentation**: SpringDoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
- **Health Probes**: Public health check at `/api/health`

---

## 🔑 Default System Credentials

The application includes an automated seeder (`DataInitializer`) that boots default accounts and sample categories if the database is fresh:

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@cms.com` | `Admin@123` | Full system control, assign complaints, manage staff & departments, view reports |
| **Technician / Staff** | `staff@cms.com` | `Staff@123` | Staff portal, update complaint resolution status, internal comments |
| **Registered Citizen** | `user@cms.com` | `User@123` | File complaints, track status, submit feedback, upload documents |

---

## ⚙️ Environment Variables Reference

Configure these environment variables in your deployment environment (e.g. Render, Railway, Docker):

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `PORT` | HTTP Server port | `8080` (or dynamically set by cloud provider) |
| `SPRING_DATASOURCE_URL` / `DB_URL` | JDBC URL for MySQL | `jdbc:mysql://<host>:<port>/complaint_management_db?createDatabaseIfNotExist=true&useSSL=true` |
| `SPRING_DATASOURCE_USERNAME` / `DB_USERNAME` | MySQL Database user | `root` or cloud user |
| `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | MySQL Database password | `<your-db-password>` |
| `APP_CORS_ALLOWED_ORIGINS` / `CORS_ORIGINS` | Comma-separated frontend URLs | `https://complaint-frontend.onrender.com,http://localhost:5173` |
| `JWT_SECRET` | 256-bit signing secret for JWT tokens | `MySecretKeyForComplaintManagementSystemJWT2026` |
| `JWT_EXPIRATION` | Access token lifespan in milliseconds | `900000` (15 minutes) |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifespan in milliseconds | `604800000` (7 days) |
| `MAIL_ENABLED` | Enable or disable Gmail alerts | `false` (default on free clouds without SMTP credentials) |
| `GMAIL_USERNAME` | Gmail address for notifications | `your.email@gmail.com` |
| `GMAIL_PASSWORD` | Google 16-character App Password | `xxxx xxxx xxxx xxxx` |

---

## 🚀 Running Locally

### Prerequisites
- JDK 17 or JDK 21 installed
- MySQL 8.0 running locally on port 3306 with database `complaint_management_db`

### Commands
```bash
# Clone the repository
git clone https://github.com/yenduriveeraneeraj/complaint-backend.git
cd complaint-backend

# Build with Maven Wrapper
./mvnw clean package -DskipTests   # Linux / macOS
.\mvnw.cmd clean package -DskipTests # Windows

# Run the executable JAR
java -jar target/complaint-management-1.0.0.jar
```

Backend will start on `http://localhost:8080`.
- Health Check: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🐳 Docker Build & Run

```bash
# Build Docker image
docker build -t complaint-backend .

# Run container with environment variables
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://<remote-host>:3306/complaint_management_db" \
  -e DB_USERNAME="<username>" \
  -e DB_PASSWORD="<password>" \
  -e CORS_ORIGINS="http://localhost:5173" \
  complaint-backend
```

---

## ☁️ Cloud Deployment (Render Web Service)

1. Create a free MySQL database on **TiDB Cloud Serverless** or **Aiven for MySQL**.
2. Push this backend repository to GitHub: `https://github.com/yenduriveeraneeraj/complaint-backend`.
3. In [Render Dashboard](https://dashboard.render.com), click **New +** > **Web Service**.
4. Connect `complaint-backend` repository.
5. Configure settings:
   - **Name**: `complaint-backend`
   - **Language / Environment**: `Docker`
   - **Region**: Singapore (or closest to you)
   - **Branch**: `main`
   - **Plan**: `Free`
6. Add Environment Variables:
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://<host>:<port>/complaint_management_db?useSSL=true`
   - `SPRING_DATASOURCE_USERNAME`: `<your_db_username>`
   - `SPRING_DATASOURCE_PASSWORD`: `<your_db_password>`
   - `APP_CORS_ALLOWED_ORIGINS`: `https://complaint-frontend.onrender.com,http://localhost:5173`
   - `MAIL_ENABLED`: `false`
7. Click **Deploy Web Service**.
8. Test your deployment once live: `https://complaint-backend.onrender.com/api/health`.

---

## 📚 API Endpoints Summary

- `GET /api/health` — Public health probe
- `POST /api/auth/register` — Citizen registration
- `POST /api/auth/login` — Authentication & JWT tokens
- `POST /api/auth/refresh-token` — Silent token refresh
- `GET /api/public/complaints/track/{number}` — Public tracking without login
- `GET /api/departments` — List active departments
- `POST /api/complaints` — Submit new complaint (multipart/form-data)
- `GET /api/complaints` — View user complaints
- `GET /api/staff-portal/my-complaints` — Technician complaints board
- `GET /api/admin/dashboard` — Administrative statistics & KPI analytics
- `GET /api/admin/reports/export/csv` — Export complaints to CSV
- `GET /api/admin/reports/export/pdf` — Export complaints to PDF
