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

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import java.time.LocalDate
import java.util.UUID

@JsonClassDescription("A workout exercise with its free-form name, optional instructions, and planned sets.")
data class WorkoutExerciseInput(
    @JsonPropertyDescription("Free-form exercise name. No predefined exercise catalog is required.")
    val name: String,
    @JsonPropertyDescription("Optional instructions or exercise variation.")
    val instructions: String? = null,
    @JsonPropertyDescription("Planned sets in their execution order.")
    val sets: List<WorkoutSetInput>,
)

@JsonClassDescription("A planned set and, when present, its recorded actual result.")
data class WorkoutSetInput(
    @JsonPropertyDescription("Target repetition count, when applicable.")
    val targetReps: Int? = null,
    @JsonPropertyDescription("Target external load in kilograms, when applicable.")
    val targetLoadKg: Double? = null,
    @JsonPropertyDescription("Free-form target, for example 'hold for 45 seconds'.")
    val targetDescription: String? = null,
)

@JsonClassDescription("Replacement target values for one existing set. Null clears that target field.")
data class SetTargetUpdate(
    @JsonPropertyDescription("Zero-based index of the existing set to update.")
    val setNumber: Int,
    @JsonPropertyDescription("Replacement target repetition count; null clears the target.")
    val targetReps: Int? = null,
    @JsonPropertyDescription("Replacement target external load in kilograms; null clears the target.")
    val targetLoadKg: Double? = null,
    @JsonPropertyDescription("Replacement free-form target; null clears the target.")
    val targetDescription: String? = null,
)

@JsonClassDescription("Changes to the plan of one existing exercise.")
data class ChangeExerciseInput(
    @JsonPropertyDescription("Identifier of the exercise in this workout to change.")
    val exerciseId: UUID,
    @JsonPropertyDescription("Full replacement target values for existing sets. Null target fields clear their current values.")
    val setTargets: List<SetTargetUpdate>? = null,
    @JsonPropertyDescription("New sets appended to this exercise, in the order supplied.")
    val addSets: List<WorkoutSetInput>? = null,
    @JsonPropertyDescription("Zero-based indexes of planned sets to remove. Sets with recorded results cannot be removed.")
    val removeSets: List<Int>? = null,
)

@JsonClassDescription("Recorded results for one workout set.")
data class SetResultChangeInput(
    @JsonPropertyDescription("Identifier of the exercise containing this set.")
    val exerciseId: UUID,
    @JsonPropertyDescription("Zero-based set index within the exercise.")
    val setNumber: Int,
    @JsonPropertyDescription("Actual repetitions completed. Use 0 to record a skipped set.")
    val actualReps: Int?,
    @JsonPropertyDescription("Actual external load in kilograms, when applicable.")
    val actualLoadKg: Double?,
    @JsonPropertyDescription("User-reported effort rating, for example on a 1–10 scale.")
    val effort: Int?,
    @JsonPropertyDescription("User feedback about execution or wellbeing.")
    val feedback: String?,
)

@JsonClassDescription("A workout with its exercises, planned sets, and recorded actual results.")
data class WorkoutDto(
    @JsonPropertyDescription("Stable identifier of this workout; use it in subsequent workout tool calls.")
    val workoutId: UUID,
    @JsonPropertyDescription("Workout date in ISO-8601 format (YYYY-MM-DD).")
    val date: LocalDate,
    @JsonPropertyDescription("Optional session intent and planning context.")
    val description: String?,
    @JsonPropertyDescription("Exercises in planned execution order, including their identifiers and sets.")
    val exercises: List<WorkoutExerciseDto>,
)

@JsonClassDescription("Recent workouts for the authenticated user, ordered from newest to oldest.")
data class WorkoutHistoryDto(
    @JsonPropertyDescription("Recent workouts, including recorded results, in newest-to-oldest order.")
    val workouts: List<WorkoutDto>,
)

@JsonClassDescription("An exercise in a workout, including its identifier and sets.")
data class WorkoutExerciseDto(
    @JsonPropertyDescription("Identifier of this exercise within the workout; use it in set result and plan edit calls.")
    val exerciseId: UUID,
    @JsonPropertyDescription("Execution position assigned by the server.")
    val position: Int,
    @JsonPropertyDescription("Free-form exercise name.")
    val name: String,
    @JsonPropertyDescription("Optional exercise instructions or variation.")
    val instructions: String?,
    @JsonPropertyDescription("Planned sets and recorded results, if any.")
    val sets: List<WorkoutSetDto>,
)

@JsonClassDescription("A workout set with optional targets and optional actual results. Missing actual values mean no result has been recorded for that field.")
data class WorkoutSetDto(
    @JsonPropertyDescription("Zero-based set index within the exercise.")
    val number: Int,
    @JsonPropertyDescription("Target repetition count, when applicable.")
    val targetReps: Int?,
    @JsonPropertyDescription("Target external load in kilograms, when applicable.")
    val targetLoadKg: Double?,
    @JsonPropertyDescription("Free-form target, for example a duration.")
    val targetDescription: String?,
    @JsonPropertyDescription("Actual repetitions; 0 explicitly records a skipped set.")
    val actualReps: Int?,
    @JsonPropertyDescription("Actual external load in kilograms, when applicable.")
    val actualLoadKg: Double?,
    @JsonPropertyDescription("User-reported effort rating, when provided.")
    val effort: Int?,
    @JsonPropertyDescription("Feedback about execution or wellbeing; may contain actual duration for timed exercises.")
    val feedback: String?,
)
