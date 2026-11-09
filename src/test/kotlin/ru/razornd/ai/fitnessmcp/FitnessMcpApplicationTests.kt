/*
 * Copyright 2026 Daniil <RazorNd> Razorenov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.razornd.ai.fitnessmcp

import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest
import io.modelcontextprotocol.spec.McpSchema.CallToolResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import ru.razornd.ai.fitnessmcp.workout.WorkoutDto
import ru.razornd.ai.fitnessmcp.workout.WorkoutExerciseDto
import ru.razornd.ai.fitnessmcp.workout.WorkoutHistoryDto
import ru.razornd.ai.fitnessmcp.workout.WorkoutSetDto
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import java.util.*

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        $$"spring.security.oauth2.resourceserver.jwt.issuer-uri=${wiremock.server.baseUrl}/issuer"
    ]
)
@EnableWireMock(ConfigureWireMock(filesUnderClasspath = "wiremock"))
@Import(TestcontainersConfiguration::class, TestMcpClientConfiguration::class)
@Sql("classpath:sql/workout/mcp-tools-seed.sql")
class FitnessMcpApplicationTests {

    @Autowired
    private lateinit var mcpClient: McpSyncClient

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Test
    fun `server should registrate workout tools`() {

        val toolNames = mcpClient.listTools()
            .tools
            .map { it.name }

        assertThat(toolNames)
            .containsExactlyInAnyOrder(
                "create_workout",
                "get_workout",
                "get_workout_history",
                "record_set_results",
                "edit_workout_plan"
            )
    }

    @Test
    fun `create workout tool returns created workout`() {
        val result = callTool(
            "create_workout",
            mapOf(
                "date" to "2026-11-10",
                "description" to "MCP-created session",
                "exercises" to listOf(
                    mapOf(
                        "name" to "Goblet squat",
                        "instructions" to "Keep the torso upright",
                        "sets" to listOf(mapOf("targetReps" to 10, "targetLoadKg" to 16.0))
                    )
                )
            )
        )

        assertToolReturnedData(
            result,
            WorkoutDto(
                workoutId = UUID(0, 0),
                date = LocalDate.parse("2026-11-10"),
                description = "MCP-created session",
                exercises = listOf(
                    WorkoutExerciseDto(
                        exerciseId = UUID(0, 0),
                        position = 0,
                        name = "Goblet squat",
                        instructions = "Keep the torso upright",
                        sets = listOf(WorkoutSetDto(0, 10, 16.0, null, null, null, null, null))
                    )
                )
            ),
            "workoutId",
            "exercises.exerciseId"
        )
    }

    @Test
    fun `get workout tool returns seeded workout`() {
        val result = callTool("get_workout", mapOf("workoutId" to SEEDED_WORKOUT_ID.toString()))

        assertToolReturnedData(result, seededWorkout())
    }

    @Test
    fun `get workout history tool returns seeded workouts`() {
        val result = callTool("get_workout_history", mapOf("limit" to 10))

        assertToolReturnedData(result, WorkoutHistoryDto(listOf(seededWorkout())))
    }

    @Test
    fun `record set results tool returns updated workout`() {
        val result = callTool(
            "record_set_results",
            mapOf(
                "workoutId" to SEEDED_WORKOUT_ID.toString(),
                "changes" to listOf(
                    mapOf(
                        "exerciseId" to SEEDED_EXERCISE_ID.toString(),
                        "setNumber" to 0,
                        "actualReps" to 10,
                        "actualLoadKg" to 20.0,
                        "effort" to 7,
                        "feedback" to "Completed"
                    )
                )
            )
        )

        assertToolReturnedData(
            result,
            seededWorkout(
                sets = listOf(WorkoutSetDto(0, 8, 20.0, null, 10, 20.0, 7, "Completed"))
            )
        )
    }

    @Test
    fun `edit workout plan tool returns updated workout`() {
        val result = callTool(
            "edit_workout_plan",
            mapOf(
                "workoutId" to SEEDED_WORKOUT_ID.toString(),
                "date" to "2026-11-11",
                "description" to "Updated via MCP",
                "changeExercises" to emptyList<Any>(),
                "addExercises" to emptyList<Any>(),
                "removeExerciseIds" to emptyList<Any>()
            )
        )

        assertToolReturnedData(
            result,
            seededWorkout(date = LocalDate.parse("2026-11-11"), description = "Updated via MCP")
        )
    }

    private fun callTool(name: String, arguments: Map<String, Any>) =
        mcpClient.callTool(CallToolRequest.builder(name).arguments(arguments).build())

    private fun assertToolReturnedData(result: CallToolResult, expected: Any, vararg ignoredFields: String) {
        assertThat(result.isError).describedAs("Expected tool to not return an error").isFalse()
        assertThat(result.structuredContent).describedAs("Expected tool to return structured content").isNotNull()

        val actual = jsonMapper.convertValue(result.structuredContent, expected.javaClass)
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields(*ignoredFields)
            .isEqualTo(expected)
    }

    private fun seededWorkout(
        date: LocalDate = LocalDate.parse("2026-11-08"),
        description: String = "Seeded workout",
        sets: List<WorkoutSetDto> = listOf(WorkoutSetDto(0, 8, 20.0, null, null, null, null, null))
    ) = WorkoutDto(
        workoutId = SEEDED_WORKOUT_ID,
        date = date,
        description = description,
        exercises = listOf(
            WorkoutExerciseDto(
                exerciseId = SEEDED_EXERCISE_ID,
                position = 0,
                name = "Seeded exercise",
                instructions = "Seeded instructions",
                sets = sets
            )
        )
    )
    private companion object {
        val SEEDED_WORKOUT_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000101")
        val SEEDED_EXERCISE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000201")
    }

}
