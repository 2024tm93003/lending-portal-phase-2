# Lending Portal Platform

Backend: Spring Boot (H2 in-memory DB) with REST APIs for authentication, equipment admin, and borrowing flow.

## Quick start (Docker)

Build the backend image directly from the provided `Dockerfile`:

```bash
docker build -t lending-portal-backend .
```

Run the container, exposing port 8080:

```bash
docker run --rm -p 8080:8080 lending-portal-backend
```

Services:
- Backend on http://localhost:8080

## Local backend (without Docker)

1. Build the jar (needs Maven 3.9+ and Java 17)
   ```bash
   mvn package
   ```
2. Run
   ```bash
   java -jar target/lending-portal-backend-0.0.1-SNAPSHOT.jar
   ```

## Seed users

- Student: `ram / ram@123`
- Staff: `suresh  / suresh@123`
- Admin: `prakash / prakash@123`

Sample equipment inventory is preloaded on startup.

## API docs / Swagger UI

If you run the backend locally (or via Docker) the OpenAPI/Swagger UI is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html (or /swagger-ui/index.html)
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The UI lists all endpoints and allows trying requests directly from your browser.

