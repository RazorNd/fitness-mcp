# Repository guidance

- Use JDK 25 and the Gradle wrapper (`./gradlew`). The Gradle wrapper is pinned to Gradle 9.8.2.
- Main entry point: `src/main/kotlin/ru/razornd/ai/fitnessmcp/FitnessMcpApplication.kt` (`ru.razornd.ai.fitnessmcp.FitnessMcpApplication`).
- Local run: start the Compose services with `docker compose up -d`, then run `./gradlew bootRun`.
- Verify with `./gradlew test`. To run the Spring application context test alone, use `./gradlew test --tests 'ru.razornd.ai.fitnessmcp.FitnessMcpApplicationTests'`.
- Docker must be available for tests: `FitnessMcpApplicationTests` starts PostgreSQL through Testcontainers. The test container image (`postgres:18.6`) is kept aligned with the PostgreSQL image in `compose.yaml`. The current test configuration does not start Redis or Grafana LGTM containers; `compose.yaml` also defines Grafana LGTM for local development/observability.
- New Kotlin files need the Apache 2.0 license header used by existing Kotlin sources.
- When IDEA tools are available, use `reformat_file` on changed Kotlin code files and `lint_files` to check them for errors and warnings. Use `build_project` for IDE compilation diagnostics; run `./gradlew test` for test verification.

## Commit messages

- Follow Conventional Commits: `type(scope): description`; scope is optional. Use types such as `feat`, `fix`, `docs`, `refactor`, `test`, and `chore`—not bracketed labels like `[feat]`.
- Keep the subject concise (ideally at most 72 characters), imperative, and without a trailing period.
- Prefer adding one relevant emoji after the colon to give the history some character; omit it when none fits. Keep it optional, use at most one, and never replace the commit type (for example, `feat(mcp): ✨ add activity lookup tool`).
- For non-trivial changes, separate the subject and body with a blank line. Summarize what changed and include relevant reasoning or trade-offs discussed with the user. Keep it focused; omit the body for trivial changes.
