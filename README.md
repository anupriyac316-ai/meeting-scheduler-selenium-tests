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

## Tech Stack

- **Language:** Java 17
- **Build tool:** Maven
- **Templating:** Thymeleaf (HTML views)
- **Testing:** JUnit (unit/service tests), Selenium (UI/browser tests)
- **CI/CD:** GitHub Actions (runs unit tests automatically on every push)

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

## Running Tests

Run unit tests only:
```bash
mvn test -Dtest=!*Selenium*,!com.scheduler.selenium.**
```

Run the full test suite, including Selenium (requires a local browser/driver setup):
```bash
mvn test
```

## Continuous Integration

Unit tests run automatically on every push and pull request to `main` via GitHub Actions. See `.github/workflows/tests.yml` for the workflow configuration.

## License

No license specified yet.
