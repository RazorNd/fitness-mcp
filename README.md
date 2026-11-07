# Fitness MCP

Fitness MCP is a Model Context Protocol (MCP) server intended to help AI agents create and track fitness training plans. It is also intended to store descriptions of the equipment available at a gym, so agents can make recommendations that fit the user's surroundings and take past training into account.

> **Project status:** The repository currently contains the application scaffold and MCP server configuration. Fitness-specific tools and data models for training plans, workout results, and gym equipment have not been implemented yet.

## Goals

The planned server capabilities include:

- Create and retrieve descriptions of training plans and workouts.
- Record completed workouts and their results, so an agent can use a user's training history when planning future sessions.
- Maintain a catalog of gym machines and other available equipment, including descriptions useful for selecting exercises.
- Expose this information through MCP tools for use by compatible AI agents.

These are project goals, not currently available MCP tools.

## Technology

- Kotlin and Java 25
- Spring Boot and Spring AI MCP server (WebMVC transport)
- PostgreSQL, Spring Data JDBC, and Flyway
- OAuth 2.0 Resource Server with JWT validation
- Docker Compose for local PostgreSQL and Grafana LGTM services

## Requirements

- JDK 25
- Docker with Docker Compose support

The project includes a Gradle wrapper, so a separate Gradle installation is not required.

## Run locally

Start the supporting services:

```bash
docker compose up -d
```

Then start the Spring Boot application:

```bash
./gradlew bootRun
```

The Compose configuration provides PostgreSQL and Grafana LGTM for local development and observability. The application is configured as a stateless MCP server. JWT validation is configured with the issuer `https://auth.razornd.ru` in `src/main/resources/application.yaml`.

> The PostgreSQL credentials in `compose.yaml` are development defaults. Do not use them in production.

## Tests

Run the test suite:

```bash
./gradlew test
```

Docker must be available. The Spring application context test starts PostgreSQL 18.6 using Testcontainers; it does not start Redis or Grafana LGTM containers.

To run only the application context test:

```bash
./gradlew test --tests 'ru.razornd.ai.fitnessmcp.FitnessMcpApplicationTests'
```

## MCP tools

Fitness-specific MCP tools are not implemented yet. Once available, this section will describe each tool, its inputs and outputs, and how to connect an MCP client to the server.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
