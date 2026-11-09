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

import java.time.LocalDate
import java.util.*

object WorkoutStubs {
    const val USER_NAME = "athlete@example.test"
    val WORKOUT_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000101")
    val FIRST_EXERCISE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000201")
    val SECOND_EXERCISE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000202")
    val OTHER_WORKOUT_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000199")
    val MISSING_WORKOUT_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000198")

    object GetWorkout {
        val outputDto = WorkoutDto(
            WORKOUT_ID,
            LocalDate.of(2026, 11, 8),
            "Upper body strength",
            listOf(
                WorkoutExerciseDto(
                    FIRST_EXERCISE_ID,
                    0,
                    "Dumbbell bench press",
                    "Control the lowering phase",
                    listOf(
                        WorkoutSetDto(0, 10, 20.0, null, null, null, null, null),
                        WorkoutSetDto(1, 8, 22.5, null, 8, 22.5, 7, "Good set"),
                    ),
                ),
                WorkoutExerciseDto(
                    SECOND_EXERCISE_ID,
                    1,
                    "Plank",
                    null,
                    listOf(WorkoutSetDto(0, null, null, "Hold for 30 seconds", null, null, null, null)),
                ),
            ),
        )
    }

    object RecordSetResults {
        val updatedSet = WorkoutSetDto(1, 8, 22.5, null, 10, null, null, "Hard final reps")
        val clearedSet = WorkoutSetDto(1, 8, 22.5, null, null, null, null, null)
        val updatedWithZeroValues = WorkoutSetDto(0, 10, 20.0, null, 0, 0.0, 10, "Skipped")
    }

    object EditWorkoutPlan {
        val mixedTargetSets = listOf(
            WorkoutSetDto(0, null, 65.0, "Heavy top set", null, null, null, null),
            WorkoutSetDto(1, 9, null, null, null, null, null, null),
            WorkoutSetDto(2, null, null, null, null, null, null, null),
        )
    }

    object WorkoutHistoryList {
        val outputDto = WorkoutHistoryDto(
            listOf(
                WorkoutDto(
                    UUID.fromString("00000000-0000-0000-0000-000000000115"),
                    LocalDate.of(2026, 11, 9),
                    "Latest workout",
                    listOf(
                        WorkoutExerciseDto(
                            UUID.fromString("00000000-0000-0000-0000-000000000205"),
                            0,
                            "Back squat",
                            "Brace before each rep",
                            listOf(
                                WorkoutSetDto(0, 5, 90.0, null, 5, 90.0, 7, "First set"),
                                WorkoutSetDto(1, 5, 100.0, null, 5, 100.0, 8, "Second set"),
                            ),
                        ),
                        WorkoutExerciseDto(
                            UUID.fromString("00000000-0000-0000-0000-000000000215"),
                            1,
                            "Overhead press",
                            null,
                            listOf(WorkoutSetDto(0, 8, 40.0, null, null, null, null, null)),
                        ),
                    ),
                ),
                WorkoutDto(
                    UUID.fromString("00000000-0000-0000-0000-000000000114"),
                    LocalDate.of(2026, 11, 8),
                    "Workout 4",
                    emptyList(),
                ),
                WorkoutDto(
                    UUID.fromString("00000000-0000-0000-0000-000000000113"),
                    LocalDate.of(2026, 11, 7),
                    "Workout 3",
                    emptyList(),
                ),
                WorkoutDto(
                    UUID.fromString("00000000-0000-0000-0000-000000000112"),
                    LocalDate.of(2026, 11, 6),
                    "Workout 2",
                    emptyList(),
                ),
                WorkoutDto(
                    UUID.fromString("00000000-0000-0000-0000-000000000111"),
                    LocalDate.of(2026, 11, 5),
                    "Workout 1",
                    emptyList(),
                ),
            ),
        )
    }

    data class CreateWorkoutInputDto(
        val userName: String,
        val date: LocalDate?,
        val description: String?,
        val exercises: List<WorkoutExerciseInput>,
    )

    object CreateFullWorkout {
        val inputDto = CreateWorkoutInputDto(
            userName = USER_NAME,
            date = LocalDate.of(2026, 11, 9),
            description = "Upper-body strength with controlled tempo",
            exercises = listOf(
                WorkoutExerciseInput(
                    name = "Bench press",
                    instructions = "Pause on chest",
                    sets = listOf(
                        WorkoutSetInput(8, 50.0, "Controlled tempo"),
                        WorkoutSetInput(6, 55.0, "Explosive concentric"),
                    ),
                ),
                WorkoutExerciseInput(
                    name = "Plank",
                    instructions = "Keep a neutral spine",
                    sets = listOf(WorkoutSetInput(targetDescription = "Hold for 45 seconds")),
                ),
            ),
        )

        val outputDto = WorkoutDto(
            workoutId = UUID(0, 0),
            date = LocalDate.of(2026, 11, 9),
            description = "Upper-body strength with controlled tempo",
            exercises = listOf(
                WorkoutExerciseDto(
                    UUID(0, 0), 0, "Bench press", "Pause on chest",
                    listOf(
                        WorkoutSetDto(0, 8, 50.0, "Controlled tempo", null, null, null, null),
                        WorkoutSetDto(1, 6, 55.0, "Explosive concentric", null, null, null, null),
                    ),
                ),
                WorkoutExerciseDto(
                    UUID(0, 0), 1, "Plank", "Keep a neutral spine",
                    listOf(WorkoutSetDto(0, null, null, "Hold for 45 seconds", null, null, null, null)),
                ),
            ),
        )

        val savedEntity = Workout(
            userName = USER_NAME,
            date = LocalDate.of(2026, 11, 9),
            description = "Upper-body strength with controlled tempo",
            exercises = arrayListOf(
                WorkoutExercise(
                    name = "Bench press",
                    instructions = "Pause on chest",
                    sets = arrayListOf(
                        WorkoutSet(
                            targetReps = 8,
                            targetLoadKg = 50.0,
                            targetDescription = "Controlled tempo",
                        ),
                        WorkoutSet(
                            targetReps = 6,
                            targetLoadKg = 55.0,
                            targetDescription = "Explosive concentric",
                        ),
                    ),
                ),
                WorkoutExercise(
                    name = "Plank",
                    instructions = "Keep a neutral spine",
                    sets = arrayListOf(WorkoutSet(targetDescription = "Hold for 45 seconds")),
                ),
            ),
        )
    }

    object CreatePartialWorkout {
        val expectedDate: LocalDate = LocalDate.now()
        val inputDto = CreateWorkoutInputDto(
            userName = USER_NAME,
            date = null,
            description = null,
            exercises = listOf(
                WorkoutExerciseInput("Squat", sets = listOf(WorkoutSetInput())),
            ),
        )

        val outputDto = WorkoutDto(
            workoutId = UUID(0, 0),
            date = expectedDate,
            description = null,
            exercises = listOf(
                WorkoutExerciseDto(
                    UUID(0, 0), 0, "Squat", null,
                    listOf(WorkoutSetDto(0, null, null, null, null, null, null, null)),
                ),
            ),
        )

        val savedEntity = Workout(
            userName = USER_NAME,
            date = expectedDate,
            description = null,
            exercises = arrayListOf(
                WorkoutExercise(
                    name = "Squat",
                    instructions = null,
                    sets = arrayListOf(WorkoutSet()),
                ),
            ),
        )
    }
}
