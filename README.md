# 🩸 Blood Bridge

A full-stack blood donation management platform connecting donors, patients, and hospitals.

## Project Structure

```
Blood_Bridge/
├── backend/       ← Spring Boot REST API (Java 17, Maven)
├── frontend/      ← React + Vite web app
├── .gitignore
└── README.md
```

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8+ (database: `blood_bridge`, port: 3306)

### Run Backend
```bash
cd backend
mvn spring-boot:run
```
API available at: `http://localhost:8083`

### Run Frontend
```bash
cd frontend
npm install     # first time only
npm run dev
```
App available at: `http://localhost:5173`

## Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Spring Boot 3, Spring Security, JWT |
| Database | MySQL + Spring Data JPA             |
| Frontend | React 18, Vite, Tailwind CSS        |

## Documentation
- [Backend README](./backend/README.md) — API details, configuration
- [Frontend README](./frontend/README.md) — UI details, environment setup
