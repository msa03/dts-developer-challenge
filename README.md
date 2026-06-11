# HMCTS Task Manager

This submission is a small task management system for HMCTS caseworkers. It provides a REST API for task management, a simple frontend for day-to-day use, and PostgreSQL for persistence.

## What is included

- create, view, update and delete tasks
- task status management
- input validation and error handling
- PostgreSQL database storage
- unit tests for backend and frontend
- Swagger API documentation

## Stack

| Area     | Technology                                     |
| -------- | ---------------------------------------------- |
| Backend  | Java 21, Spring Boot, Spring Data JPA          |
| Frontend | Express, TypeScript, Nunjucks, GOV.UK Frontend |
| Database | PostgreSQL 16                                  |
| Testing  | JUnit 5, Mockito, Jest                         |

## Running the application

### Docker

The quickest way to run everything is:

```bash
docker-compose up --build
```

Services:

- Frontend: `http://localhost:3100/tasks`
- Backend API: `http://localhost:4000`
- API documentation: `http://localhost:4000/swagger-ui.html`

To stop the application:

```bash
docker-compose down
```

### Local development

Start PostgreSQL:

```bash
docker-compose up postgres
```

Start the backend:

```bash
cd hmcts-dev-test-backend
./gradlew bootRun
```

Start the frontend:

```bash
cd hmcts-dev-test-frontend
yarn install
yarn start:dev
```

Default local database settings:

- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=tasks_db`
- `DB_USER_NAME=postgres`
- `DB_PASSWORD=postgres`

## Running tests

Backend:

```bash
cd hmcts-dev-test-backend
./gradlew test
```

Frontend:

```bash
cd hmcts-dev-test-frontend
yarn test
```

## API summary

Base path: `/api/tasks`

| Method   | Endpoint                 | Description        |
| -------- | ------------------------ | ------------------ |
| `POST`   | `/api/tasks`             | Create a task      |
| `GET`    | `/api/tasks`             | Get all tasks      |
| `GET`    | `/api/tasks/{id}`        | Get a task by id   |
| `PUT`    | `/api/tasks/{id}`        | Update a task      |
| `PATCH`  | `/api/tasks/{id}/status` | Update task status |
| `DELETE` | `/api/tasks/{id}`        | Delete a task      |

Full request and response details are available in Swagger.

## Notes

- Tasks are stored in PostgreSQL.
- Description is optional.
- Valid task statuses are `TODO`, `IN_PROGRESS` and `DONE`.
