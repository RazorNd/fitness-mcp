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

package ru.razornd.ai.fitnessmcp.workout

import io.modelcontextprotocol.common.McpTransportContext
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import ru.razornd.ai.fitnessmcp.userName
import java.time.LocalDate
import java.util.*

@Component
@Suppress("unused")
class WorkoutTools(
    private val workoutService: WorkoutService,
) {

    @McpTool(
        name = "create_workout",
        title = "Create a workout plan",
        description = "Save a workout plan that you have already prepared using the user's context. This tool stores the supplied plan; it does not generate exercises or choose loads. Omit date to use the current date. The response contains the saved workout and exercise identifiers needed by later tools.",
        generateOutputSchema = true,
        annotations = McpAnnotations(destructiveHint = false, idempotentHint = false, openWorldHint = false),
    )
    fun createWorkout(
        @McpToolParam(
            description = "Workout date in ISO-8601 format (YYYY-MM-DD). If omitted, the server uses today's date.",
            required = false
        )
        date: LocalDate?,
        @McpToolParam(
            description = "Optional session intent and planning context, such as its goal, why the exercises or loads were selected, and information useful for future workout planning.",
            required = false
        )
        description: String?,
        @McpToolParam(description = "Exercises in planned execution order. Each exercise needs a free-form name and at least one planned set.")
        exercises: List<WorkoutExerciseInput>,
        context: McpTransportContext,
    ): WorkoutDto = workoutService.createWorkout(context.userName, date, description, exercises)

    @McpTool(
        name = "get_workout",
        title = "Get a workout",
        description = "Return the current workout, including its date, description, exercises, planned set targets, and any recorded results. Only workouts belonging to the authenticated user are accessible.",
        generateOutputSchema = true,
        annotations = McpAnnotations(readOnlyHint = true, openWorldHint = false),
    )
    fun getWorkout(
        @McpToolParam(description = "Identifier of the workout to retrieve.")
        workoutId: UUID,
        context: McpTransportContext,
    ): WorkoutDto = workoutService.getWorkout(context.userName, workoutId)

    @McpTool(
        name = "get_workout_history",
        title = "Get workout history",
        description = "Return recent workouts for the authenticated user, including exercise names and recorded set results, so you can plan the next session using actual prior performance. Exercise names are free-form; do not assume a canonical exercise catalog or exact name matching. The server returns up to 100 workouts when limit is omitted and caps larger limits at 100.",
        generateOutputSchema = true,
        annotations = McpAnnotations(readOnlyHint = true, openWorldHint = false),
    )
    fun getWorkoutHistory(
        @McpToolParam(
            description = "Maximum number of recent workouts to return. Omit to use the server default; the server enforces an upper bound.",
            required = false
        )
        limit: Int?,
        context: McpTransportContext,
    ): WorkoutHistoryDto = workoutService.getWorkoutHistory(context.userName, limit)

    @McpTool(
        name = "record_set_results",
        title = "Record workout set results",
        description = "Record or correct results for one or more sets in a workout and return the updated workout. Supply each change with exerciseId and the zero-based setNumber, plus actualReps, actualLoadKg, effort, and feedback. All result values are written directly; null clears that stored field. Use actualReps=0 to record a skipped set, and do not infer or fill in a load for a skipped set. When the user reports only repetitions, explicitly include the known load from the current context; never invent unknown values. The user is identified by the authenticated server context.",
        generateOutputSchema = true,
        annotations = McpAnnotations(destructiveHint = true, idempotentHint = true, openWorldHint = false),
    )
    fun recordSetResults(
        @McpToolParam(description = "Identifier of the workout containing the sets.")
        workoutId: UUID,
        @McpToolParam(description = "Set result changes. Each change identifies an exercise and zero-based set index and supplies the actual result fields.")
        changes: List<SetResultChangeInput>,
        context: McpTransportContext,
    ): WorkoutDto = workoutService.recordSetResults(
        context.userName,
        workoutId,
        changes,
    )

    @McpTool(
        name = "edit_workout_plan",
        title = "Edit a workout plan",
        description = "Update a workout's date, planning description, or uncompleted plan and return the updated workout. Exercise positions and set numbers are zero-based indexes. Group full target replacements, sets to append, and set indexes to remove by exercise in changeExercises. Target replacement sets all target fields; null clears the corresponding field. Set removals use indexes from the original plan and are applied before appending new sets. Existing actual set results are never changed by this tool. Target changes and removals are rejected for sets with recorded results. Exercises with recorded results cannot be removed. Supply at least one change.",
        generateOutputSchema = true,
        annotations = McpAnnotations(destructiveHint = true, idempotentHint = true, openWorldHint = false),
    )
    fun editWorkoutPlan(
        @McpToolParam(description = "Identifier of the workout to update.")
        workoutId: UUID,
        @McpToolParam(
            description = "Replacement workout date in ISO-8601 format (YYYY-MM-DD). Omit to leave unchanged.",
            required = false
        )
        date: LocalDate?,
        @McpToolParam(
            description = "Replacement session intent or planning context. Null or omitted leaves it unchanged.",
            required = false
        )
        description: String?,
        @McpToolParam(
            description = "Changes grouped by exercise. Each item identifies one exercise and can include full target replacements, sets to append in supplied order, and zero-based indexes of original sets to remove. Null target values clear target fields. Removals are applied before additions, so newly appended sets cannot be addressed for removal in the same call. Null and empty change lists are ignored.",
            required = false
        )
        changeExercises: List<ChangeExerciseInput>?,
        @McpToolParam(
            description = "New exercises and their planned sets, appended to the workout in supplied order.",
            required = false
        )
        addExercises: List<WorkoutExerciseInput>?,
        @McpToolParam(
            description = "Identifiers of exercises to remove. Exercises with recorded results cannot be removed.",
            required = false
        )
        removeExerciseIds: List<UUID>?,
        context: McpTransportContext,
    ): WorkoutDto = workoutService.editWorkoutPlan(
        context.userName,
        workoutId,
        date,
        description,
        changeExercises.orEmpty(),
        addExercises.orEmpty(),
        removeExerciseIds.orEmpty(),
    )
}
