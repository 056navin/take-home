# Property Lead Management API

A backend API for a real estate platform that manages property leads through a pipeline.  
Built with **Java 17 + Spring Boot 3 + H2 (in-memory database)**.

---

## Quick Start (Under 3 Minutes)

### Prerequisites

- **Java 17** — verify with `java -version`
- **Maven 3.8+** — verify with `mvn -version`
- Set `JAVA_HOME` if needed:
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home)   # macOS
  ```

### Steps

```bash
# 1. Clone and enter the project
cd house

# 2. Build and run (dependencies download, DB auto-seeds)
export JAVA_HOME=$(/usr/libexec/java_home)
mvn clean spring-boot:run
```

That's it. The server starts on **http://localhost:8080**.  
The H2 in-memory database is created and seeded automatically on startup.

### Explore the API

| Resource          | URL                                          |
|-------------------|----------------------------------------------|
| **Swagger UI**    | http://localhost:8080/swagger-ui.html         |
| **API Docs JSON** | http://localhost:8080/v3/api-docs             |
| **H2 Console**    | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:housedb`, user: `sa`, no password) |

### Run Tests

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
mvn test
```

---

## Seeded Users (Login Credentials)

| Role  | Email             | Password      |
|-------|-------------------|---------------|
| Admin | admin@house.com   | password123   |
| Agent | alice@house.com   | password123   |
| Agent | bob@house.com     | password123   |

---

## API Quick Reference

### Auth
```
POST /auth/login         — { "email": "admin@house.com", "password": "password123" }
                           Returns JWT token. Use as: Authorization: Bearer <token>
```

### Properties (read-only, requires JWT)
```
GET /properties          — ?city=Mumbai&status=AVAILABLE&bedrooms=3&page=0&limit=10
GET /properties/:id      — Single property with lead counts by status
```

### Leads (requires JWT)
```
POST   /leads            — Create a lead (property must be AVAILABLE, no duplicate phone+property)
GET    /leads            — ?status=NEW&priority=HOT&property_id=1&page=0&limit=10
GET    /leads/:id        — Single lead with property details
PATCH  /leads/:id        — Update priority or notes only
POST   /leads/:id/transition — { "status": "CONTACTED" }
```

### Lead Status Transitions
```
Pipeline:  NEW → CONTACTED → VISITED → BOOKED
LOST:      Can be set from any status except BOOKED
Terminal:  BOOKED and LOST — no further transitions allowed
Auto:      When a lead is BOOKED, the property status is set to BOOKED
```

### Dashboard
```
GET /dashboard/summary   — Properties by status, leads by status/priority, conversion rate
```

---

## Design Decision

I chose **Spring Boot with H2 in-memory database** instead of the suggested Node.js + PostgreSQL stack because the user requested a Java implementation. H2 eliminates all database setup — no Docker, no installation, no configuration. JPA with Hibernate auto-generates the schema from entity annotations, which serves as both the migration and the documentation of the data model. The trade-off is that data resets on restart, but for a take-home demo this keeps the "clone → run" time under 60 seconds. The transition state machine is implemented as a simple map of `currentStatus → nextValidStatus`, making the rules easy to read, test, and extend.

## What I'd Improve With More Time

- **PostgreSQL with Flyway migrations** for persistent data and proper version-controlled schema changes.
- **Role-based access control** — Agents can only see/manage their own leads; Admins see everything.
- **Pagination metadata** in response headers (total count, total pages, current page).
- **Audit trail** — log every lead status transition with timestamp and user who made the change.
- **Redis caching** for the dashboard summary endpoint with invalidation on lead create/transition.

---

## Tech Stack

| Component     | Technology                    |
|---------------|-------------------------------|
| Language      | Java 17                       |
| Framework     | Spring Boot 3.2.5             |
| Database      | H2 (in-memory)                |
| ORM           | Spring Data JPA / Hibernate   |
| Auth          | Spring Security + JWT (jjwt)  |
| Validation    | Jakarta Bean Validation       |
| API Docs      | SpringDoc OpenAPI (Swagger)   |
| Testing       | JUnit 5 + MockMvc             |
| Build         | Maven                         |

