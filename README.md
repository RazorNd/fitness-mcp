# Fitness MCP

Fitness MCP is a Model Context Protocol (MCP) server intended to help AI agents create and track fitness training plans. It is also intended to store descriptions of the equipment available at a gym, so agents can make recommendations that fit the user's surroundings and take past training into account.

> **Project status:** The server provides MCP tools for creating and editing workout plans, retrieving workouts and workout history, and recording set results. A gym equipment catalog is not currently implemented.

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
- PostgreSQL, Spring Data JPA, and Flyway
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

The Compose configuration provides PostgreSQL and Grafana LGTM for local development and observability. The application is configured as a stateless MCP server. For local startup, set the database properties as described under [Configuration](#configuration).

> The PostgreSQL credentials in `compose.yaml` are development defaults. Do not use them in production.

## Configuration

The application requires a PostgreSQL connection. Its configuration uses the `fitness.*` properties below; database connection values have no defaults. The JWT issuer defaults to `https://auth.razornd.ru` and can be changed for another identity provider.

Use the `FITNESS_*` names shown below for environment-based configuration. The standard Spring variables such as `SPRING_DATASOURCE_URL` do not populate these custom properties.

| Property | Environment variable | Required | Default |
| --- | --- | --- | --- |
| `fitness.datasource.url` | `FITNESS_DATASOURCE_URL` | Yes | — |
| `fitness.datasource.username` | `FITNESS_DATASOURCE_USERNAME` | Yes | — |
| `fitness.datasource.password` | `FITNESS_DATASOURCE_PASSWORD` | Yes | — |
| `fitness.mcp.endpoint` | `FITNESS_MCP_ENDPOINT` | No | `/mcp` |
| `fitness.jwt.issuer-uri` | `FITNESS_JWT_ISSUER_URI` | No | `https://auth.razornd.ru` |
| `fitness.server.port` | `FITNESS_SERVER_PORT` | No | `8080` |
| `fitness.server.forward-headers-strategy` | `FITNESS_SERVER_FORWARD_HEADERS_STRATEGY` | No | `framework` |
| `instance.id` | `INSTANCE_ID` | No | `unknown` |

The JWT issuer must match the `iss` claim in access tokens, and its discovery metadata must be reachable by the application. MCP requests must include a valid bearer token. The MCP endpoint defaults to `/mcp` and can be changed with `fitness.mcp.endpoint`.

### Environment variables

For example, configure the values when starting the application:

```bash
export FITNESS_DATASOURCE_URL='jdbc:postgresql://localhost:54528/fitness-mcp'
export FITNESS_DATASOURCE_USERNAME='user'
export FITNESS_DATASOURCE_PASSWORD='secret'
export FITNESS_JWT_ISSUER_URI='https://auth.example.com'
./gradlew bootRun
```

For a container deployment, use the database hostname and port reachable from the application container, and provide secrets through your deployment platform. Do not use the development credentials above in production.

Spring also imports optional configuration files from `/run/secrets/` (config tree). A file named `fitness.datasource.password` supplies that property; required database settings must still be provided if the directory or files are absent.

### YAML configuration

The same values can be supplied as Spring properties in an external YAML configuration:

```yaml
fitness:
  datasource:
    url: jdbc:postgresql://db:5432/fitness-mcp
    username: fitness_mcp
    password: change-me
  mcp:
    endpoint: /mcp
  jwt:
    issuer-uri: https://auth.example.com
  server:
    port: 8080
    forward-headers-strategy: framework
instance:
  id: fitness-mcp-1
```

Provide the file using Spring Boot's normal external configuration options, for example `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/fitness-mcp/`.

### Command-line properties

Spring Boot command-line arguments use the same property names:

```bash
java -jar fitness-mcp.jar \
  --fitness.datasource.url='jdbc:postgresql://db:5432/fitness-mcp' \
  --fitness.datasource.username='fitness_mcp' \
  --fitness.datasource.password='change-me' \
  --fitness.mcp.endpoint='/mcp' \
  --fitness.jwt.issuer-uri='https://auth.example.com'
```

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

- `create_workout` — save a workout plan.
- `get_workout` — retrieve a workout and its plan/results.
- `get_workout_history` — retrieve recent workouts and results for the authenticated user.
- `record_set_results` — record or correct completed set results.
- `edit_workout_plan` — edit a workout date, description, exercises, or planned sets.

Workout data is scoped to the authenticated user identified by the access token.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
