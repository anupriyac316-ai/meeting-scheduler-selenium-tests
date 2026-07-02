# Meeting Scheduler

A Java-based meeting scheduling application built with Maven, including automated unit tests (JUnit) and end-to-end UI tests (Selenium).

![Tests](https://github.com/anupriyac316-ai/meeting-scheduler-selenium-tests/actions/workflows/tests.yml/badge.svg)

## Overview

Meeting Scheduler is a web application that lets users register, log in, and book meetings through a simple dashboard. It's built with a standard Java/Spring-style layered architecture (controller, service, repository, model) and uses Thymeleaf templates for the UI.

## Features

- User registration and authentication
- Dashboard for viewing and managing meetings
- Meeting booking flow
- Notification view for updates
- Full unit and integration test coverage
- Containerized with Docker for easy local setup and deployment

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.2.0 (Web, Security, Data JPA)
- **Build tool:** Maven
- **Templating:** Thymeleaf (HTML views)
- **Database:** H2 (local dev) / MySQL (Docker, production via AWS RDS)
- **Testing:** JUnit + Mockito (unit/service tests), Selenium (UI/browser tests)
- **CI/CD:** GitHub Actions (runs unit tests automatically on every push)
- **Containerization:** Docker & Docker Compose

## Project Structure

```
src/
├── main/
│   ├── java/com/scheduler/
│   │   ├── App.java
│   │   ├── SecurityConfig.java
│   │   ├── controller/       # AccountController, AuthController, MeetingController
│   │   ├── model/            # Meeting, User, NotificationView
│   │   ├── repository/       # MeetingRepository, UserRepository
│   │   └── service/          # MeetingService, UserService
│   └── resources/
│       ├── application.properties
│       ├── application-docker.properties
│       └── templates/        # home.html, login.html, register.html, dashboard.html, book.html
└── test/
    ├── java/com/scheduler/
    │   ├── AppTest.java
    │   ├── service/          # MeetingServiceTest, UserServiceTest
    │   └── selenium/         # DashboardFlowTest, SeleniumTestBase
    └── resources/
        └── application-test.properties
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker Desktop (optional — for containerized setup, see [Running with Docker](#running-with-docker))

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/anupriyac316-ai/meeting-scheduler-selenium-tests.git
   cd meeting-scheduler-selenium-tests
   ```

2. Install dependencies:
   ```bash
   mvn install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The app will be available at [http://localhost:8080](http://localhost:8080), using the local H2 in-memory database by default.

## Testing

The project has two layers of automated tests: fast unit tests with JUnit/Mockito, and full end-to-end browser tests with Selenium.

### Unit Tests (JUnit + Mockito)

Located in `src/test/java/com/scheduler/service/`, these test the service layer (`MeetingServiceTest`, `UserServiceTest`) in isolation using mocked repositories — no browser or database required.

Run unit tests only (excludes Selenium tests):
```bash
mvn test -Dtest=!*Selenium*,!com.scheduler.selenium.**
```

### End-to-End Tests (Selenium)

Located in `src/test/java/com/scheduler/selenium/`, these tests drive a real browser to verify user flows like login and meeting booking end-to-end (`DashboardFlowTest`), built on a shared `SeleniumTestBase` setup class.

**Requirements for Selenium tests:**
- A local browser installed (Edge/Chrome)
- Selenium Manager (bundled with Selenium 4.6+) auto-resolves the correct driver binary — no manual WebDriverManager setup needed
- The application must be reachable (either running locally via `mvn spring-boot:run`, or the test suite may start it as part of the test lifecycle — check `SeleniumTestBase` for setup details)

Run the full test suite, including Selenium:
```bash
mvn test
```

**Note:** Selenium tests are UI-driven and can be more sensitive to timing/environment differences than unit tests. If you hit WebDriver or date-input issues locally, confirm your browser and driver versions are compatible, and that the app is up before the test suite starts.

## Running with Docker

MeetSync is fully containerized and can be run locally with Docker, without needing to install Java, Maven, or MySQL directly on your machine.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### Option 1: Run with Docker Compose (recommended for local development)

This spins up both the Spring Boot app and a MySQL database together.

```bash
docker compose up --build
```

The app will be available at [http://localhost:8080](http://localhost:8080).

To stop the containers:
```bash
docker compose down
```

Your data persists across restarts (`docker compose down` followed by `docker compose up` will not erase your database) since MySQL data is stored in a named Docker volume.

To stop the containers **and** wipe the database:
```bash
docker compose down -v
```

### Option 2: Pull the pre-built image from Docker Hub

No need to clone the repo or build from source — just pull and run:

```bash
docker pull anupriya1310/meeting-scheduler:latest
docker run -p 8080:8080 anupriya1310/meeting-scheduler:latest
```

> Note: this runs the app alone. You'll need to point it at a running MySQL instance via environment variables (see below) unless you're using `docker compose up`, which handles this automatically.

### Environment Variables

When running the app container standalone (not via Compose), configure these environment variables to connect to your database:

| Variable | Description | Example |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL | `jdbc:mysql://<host>:3306/meetsync` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `meetsync_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `meetsync_pass` |

Example:
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/meetsync \
  -e SPRING_DATASOURCE_USERNAME=meetsync_user \
  -e SPRING_DATASOURCE_PASSWORD=meetsync_pass \
  anupriya1310/meeting-scheduler:latest
```

### Docker Project Files

```
meeting-scheduler/
├── Dockerfile                              # Multi-stage build: Maven build → JRE runtime
├── .dockerignore
├── docker-compose.yml                      # App + MySQL for local development
└── src/main/resources/
    └── application-docker.properties       # Config used when running in Docker
```

### Tech Notes

- The Docker image uses a **multi-stage build**: a Maven/JDK 17 stage compiles and packages the app, then the final image only contains a lightweight JRE 17 (Alpine-based) runtime — keeping the image small.
- The container runs as a **non-root user** for improved security.
- The `docker` Spring profile overrides the default H2 in-memory database with MySQL, matching the production setup on AWS RDS.

## Continuous Integration

Unit tests run automatically on every push and pull request to `main` via GitHub Actions. See `.github/workflows/tests.yml` for the workflow configuration.

## License

No license specified yet.