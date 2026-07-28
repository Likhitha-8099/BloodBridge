# Blood Bridge — Backend

Spring Boot REST API for the Blood Bridge platform.

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blood_bridge
spring.datasource.username=root
spring.datasource.password=your_password
server.port=8083
```

## Run

```bash
# From Blood_Bridge/backend/
mvn spring-boot:run
```

## Test

```bash
mvn clean test
```

## API Base URL
`http://localhost:8083/api`

## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login |
| GET | `/api/blood-requests` | List all requests |
| POST | `/api/blood-requests` | Create request |
| GET | `/api/donors` | List donors |
| GET | `/api/dashboard/overview` | Dashboard stats |
