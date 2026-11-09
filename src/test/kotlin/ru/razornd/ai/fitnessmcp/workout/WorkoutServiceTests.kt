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

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.boot.jpa.test.autoconfigure.find
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.context.jdbc.Sql
import ru.razornd.ai.fitnessmcp.TestcontainersConfiguration
import java.time.LocalDate
import java.util.*

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@AutoConfigureTestEntityManager
class WorkoutServiceTests {

    @Autowired
    private lateinit var workoutService: WorkoutService

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Test
    @Transactional
    fun `create fully populated workout`() {
        val input = WorkoutStubs.CreateFullWorkout.inputDto
        val result = workoutService.createWorkout(input.userName, input.date, input.description, input.exercises)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("workoutId", "exercises.exerciseId")
            .isEqualTo(WorkoutStubs.CreateFullWorkout.outputDto)

        testEntityManager.flush()
        testEntityManager.clear()
        val savedEntity = testEntityManager.find<Workout>(result.workoutId)
        assertThat(savedEntity)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "exercises.id", "exercises.sets.id")
            .isEqualTo(WorkoutStubs.CreateFullWorkout.savedEntity)
    }

    @Test
    @Transactional
    fun `create partially populated workout`() {
        val input = WorkoutStubs.CreatePartialWorkout.inputDto
        val result = workoutService.createWorkout(input.userName, input.date, input.description, input.exercises)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("workoutId", "exercises.exerciseId")
            .isEqualTo(WorkoutStubs.CreatePartialWorkout.outputDto)

        testEntityManager.flush()
        testEntityManager.clear()
        val savedEntity = testEntityManager.find<Workout>(result.workoutId)
        assertThat(savedEntity)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "exercises.id", "exercises.sets.id")
            .isEqualTo(WorkoutStubs.CreatePartialWorkout.savedEntity)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `get workout successfully`() {
        val result = workoutService.getWorkout(WorkoutStubs.USER_NAME, WorkoutStubs.WORKOUT_ID)

        assertThat(result).usingRecursiveComparison().isEqualTo(WorkoutStubs.GetWorkout.outputDto)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `get workout fails when id does not exist`() {
        assertThatThrownBy {
            workoutService.getWorkout(WorkoutStubs.USER_NAME, WorkoutStubs.MISSING_WORKOUT_ID)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(WorkoutStubs.MISSING_WORKOUT_ID.toString())
            .hasMessageContaining(WorkoutStubs.USER_NAME)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `get workout fails when it belongs to another user`() {
        assertThatThrownBy {
            workoutService.getWorkout(WorkoutStubs.USER_NAME, WorkoutStubs.OTHER_WORKOUT_ID)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(WorkoutStubs.OTHER_WORKOUT_ID.toString())
            .hasMessageContaining(WorkoutStubs.USER_NAME)
    }

    @Test
    @Sql("classpath:sql/workout/workout-history-list.sql")
    fun `history returns ordered workouts and their ordered exercises and sets for this user`() {
        val result = workoutService.getWorkoutHistory(WorkoutStubs.USER_NAME, 10)

        assertThat(result).usingRecursiveComparison().isEqualTo(WorkoutStubs.WorkoutHistoryList.outputDto)
    }

    @Test
    @Sql("classpath:sql/workout/workout-history-foreign-only.sql")
    fun `history returns empty list when database only has other users workouts`() {
        assertThat(workoutService.getWorkoutHistory(WorkoutStubs.USER_NAME, null).workouts).isEmpty()
    }

    @Test
    @Sql("classpath:sql/workout/workout-history-many.sql")
    fun `history without limit returns maximum size`() {
        assertThat(workoutService.getWorkoutHistory(WorkoutStubs.USER_NAME, null).workouts).hasSize(100)
    }

    @Test
    @Sql("classpath:sql/workout/workout-history-many.sql")
    fun `history limit above seven is applied`() {
        assertThat(workoutService.getWorkoutHistory(WorkoutStubs.USER_NAME, 12).workouts).hasSize(12)
    }

    @Test
    @Sql("classpath:sql/workout/workout-history-fewer-than-limit.sql")
    fun `history returns fewer records than requested limit`() {
        assertThat(workoutService.getWorkoutHistory(WorkoutStubs.USER_NAME, 10).workouts).hasSize(3)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record result updates only selected set and stores nullable fields directly`() {
        val result = workoutService.recordSetResults(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            listOf(SetResultChangeInput(WorkoutStubs.FIRST_EXERCISE_ID, 1, 10, null, null, "Hard final reps")),
        )
        assertThat(result.exercises.first().sets.last())
            .usingRecursiveComparison()
            .isEqualTo(WorkoutStubs.RecordSetResults.updatedSet)
        assertThat(result.exercises.first().sets.last().actualLoadKg).isNull()
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record result can clear result fields by writing null`() {
        val result = workoutService.recordSetResults(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            listOf(SetResultChangeInput(WorkoutStubs.FIRST_EXERCISE_ID, 1, null, null, null, null)),
        )

        assertThat(result.exercises.first().sets.last())
            .usingRecursiveComparison()
            .isEqualTo(WorkoutStubs.RecordSetResults.clearedSet)
        assertThat(result.exercises.first().sets.last().actualReps).isNull()
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record result rejects an exercise outside the workout`() {
        val exerciseId = UUID.randomUUID()
        assertThatThrownBy {
            workoutService.recordSetResults(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                listOf(SetResultChangeInput(exerciseId, 0, 5, null, null, null)),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(exerciseId.toString())
            .hasMessageContaining(WorkoutStubs.WORKOUT_ID.toString())
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record results rejects a missing set with its index in the message`() {
        assertThatThrownBy {
            workoutService.recordSetResults(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                listOf(SetResultChangeInput(WorkoutStubs.FIRST_EXERCISE_ID, 8, 5, null, null, null)),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Set 8")
            .hasMessageContaining(WorkoutStubs.FIRST_EXERCISE_ID.toString())
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record results reports a missing workout`() {
        assertThatThrownBy {
            workoutService.recordSetResults(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.MISSING_WORKOUT_ID,
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(WorkoutStubs.MISSING_WORKOUT_ID.toString())
            .hasMessageContaining(WorkoutStubs.USER_NAME)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record results with no changes returns current workout`() {
        val result = workoutService.recordSetResults(WorkoutStubs.USER_NAME, WorkoutStubs.WORKOUT_ID, emptyList())

        assertThat(result).usingRecursiveComparison().isEqualTo(WorkoutStubs.GetWorkout.outputDto)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `record results updates multiple sets without validating supplied values`() {
        val result = workoutService.recordSetResults(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            listOf(
                SetResultChangeInput(WorkoutStubs.FIRST_EXERCISE_ID, 0, -2, -1.0, 12, "Hard set"),
                SetResultChangeInput(WorkoutStubs.SECOND_EXERCISE_ID, 0, 0, null, null, "Skipped"),
            ),
        )

        assertThat(result.exercises[0].sets[0])
            .usingRecursiveComparison()
            .isEqualTo(WorkoutStubs.RecordSetResults.updatedWithUnvalidatedValues)
        assertThat(result.exercises[1].sets[0].actualReps).isZero()
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan changes existing roots and adds and removes roots`() {
        val result = workoutService.editWorkoutPlan(
            userName = WorkoutStubs.USER_NAME,
            workoutId = WorkoutStubs.WORKOUT_ID,
            date = LocalDate.of(2026, 11, 9),
            description = "Adjusted plan",
            changeExercises = listOf(
                ChangeExerciseInput(
                    exerciseId = WorkoutStubs.FIRST_EXERCISE_ID,
                    setTargets = listOf(SetTargetUpdate(0, targetReps = 12)),
                    addSets = listOf(WorkoutSetInput(targetReps = 6)),
                ),
                ChangeExerciseInput(
                    exerciseId = WorkoutStubs.SECOND_EXERCISE_ID,
                    removeSets = listOf(0),
                ),
            ),
            addExercises = listOf(
                WorkoutExerciseInput(
                    "Cable row",
                    sets = listOf(WorkoutSetInput(targetReps = 12))
                )
            ),
            removeExerciseIds = listOf(WorkoutStubs.SECOND_EXERCISE_ID),
        )

        assertThat(result.date).isEqualTo(LocalDate.of(2026, 11, 9))
        assertThat(result.description).isEqualTo("Adjusted plan")
        assertThat(result.exercises).hasSize(2)
        assertThat(result.exercises.map { it.position }).containsExactly(0, 1)
        assertThat(result.exercises.first().sets.map { it.number }).containsExactly(0, 1, 2)
        assertThat(result.exercises.first().sets.first().targetReps).isEqualTo(12)
        assertThat(result.exercises.last().name).isEqualTo("Cable row")
        assertThat(result.exercises.last().sets.single().targetReps).isEqualTo(12)
        assertThat(workoutService.getWorkout(WorkoutStubs.USER_NAME, WorkoutStubs.WORKOUT_ID).exercises)
            .noneMatch { it.exerciseId == WorkoutStubs.SECOND_EXERCISE_ID }
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan accepts empty changes`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            date = null,
            description = null,
            changeExercises = listOf(
                ChangeExerciseInput(
                    WorkoutStubs.FIRST_EXERCISE_ID,
                    setTargets = emptyList(),
                    addSets = emptyList(),
                    removeSets = emptyList(),
                ),
                ChangeExerciseInput(
                    WorkoutStubs.SECOND_EXERCISE_ID,
                    setTargets = emptyList(),
                    addSets = emptyList(),
                    removeSets = emptyList(),
                ),
                ChangeExerciseInput(WorkoutStubs.FIRST_EXERCISE_ID),
            ),
            addExercises = emptyList(),
            removeExerciseIds = emptyList(),
        )

        assertThat(result).usingRecursiveComparison().isEqualTo(WorkoutStubs.GetWorkout.outputDto)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan rejects target changes and removals for completed sets`() {
        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                listOf(
                    ChangeExerciseInput(
                        WorkoutStubs.FIRST_EXERCISE_ID,
                        setTargets = listOf(SetTargetUpdate(1, targetReps = 9)),
                    ),
                ),
                emptyList(),
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Cannot change targets")
            .hasMessageContaining("set 1")
            .hasMessageContaining("recorded result")
        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                listOf(
                    ChangeExerciseInput(
                        WorkoutStubs.FIRST_EXERCISE_ID,
                        removeSets = listOf(1),
                    ),
                ),
                emptyList(),
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Cannot remove set 1")
            .hasMessageContaining("recorded result")
        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                emptyList(),
                emptyList(),
                listOf(WorkoutStubs.FIRST_EXERCISE_ID),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Cannot remove exercise ${WorkoutStubs.FIRST_EXERCISE_ID}")
            .hasMessageContaining("recorded result")
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan reports unknown exercise identifiers`() {
        val exerciseId = UUID.randomUUID()

        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                listOf(ChangeExerciseInput(exerciseId, setTargets = listOf(SetTargetUpdate(0, targetReps = 10)))),
                emptyList(),
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(exerciseId.toString())
            .hasMessageContaining("not part of the workout")
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan reports unknown exercise identifiers when removing`() {
        val exerciseId = UUID.randomUUID()

        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                emptyList(),
                emptyList(),
                listOf(exerciseId),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(exerciseId.toString())
            .hasMessageContaining("not part of the workout")
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan reports a missing set index`() {
        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                listOf(
                    ChangeExerciseInput(
                        WorkoutStubs.FIRST_EXERCISE_ID,
                        setTargets = listOf(SetTargetUpdate(8, targetReps = 10))
                    )
                ),
                emptyList(),
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Set 8")
            .hasMessageContaining(WorkoutStubs.FIRST_EXERCISE_ID.toString())
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan reports a missing set when removing it`() {
        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.WORKOUT_ID,
                null,
                null,
                listOf(ChangeExerciseInput(WorkoutStubs.FIRST_EXERCISE_ID, removeSets = listOf(8))),
                emptyList(),
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Set 8")
            .hasMessageContaining(WorkoutStubs.FIRST_EXERCISE_ID.toString())
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan reports missing workout`() {
        assertThatThrownBy {
            workoutService.editWorkoutPlan(
                WorkoutStubs.USER_NAME,
                WorkoutStubs.MISSING_WORKOUT_ID,
                LocalDate.of(2026, 11, 9),
                null,
                emptyList(),
                emptyList(),
                emptyList(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(WorkoutStubs.MISSING_WORKOUT_ID.toString())
            .hasMessageContaining(WorkoutStubs.USER_NAME)
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan appends new sets after existing sets`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            null,
            null,
            listOf(
                ChangeExerciseInput(
                    WorkoutStubs.FIRST_EXERCISE_ID,
                    addSets = listOf(
                        WorkoutSetInput(
                            targetReps = 12,
                            targetLoadKg = 25.0,
                            targetDescription = "Back-off set",
                        ),
                    ),
                ),
            ),
            emptyList(),
            emptyList(),
        )

        assertThat(result.exercises.first().sets.map { it.targetReps }).containsExactly(10, 8, 12)
        assertThat(result.exercises.first().sets.map { it.number }).containsExactly(0, 1, 2)
        assertThat(result.exercises.first().sets.last().targetDescription).isEqualTo("Back-off set")
    }

    @Test
    @Sql("classpath:sql/workout/workout-plan-uncompleted.sql")
    fun `edit plan replaces all target fields including nulls`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            null,
            null,
            listOf(
                ChangeExerciseInput(
                    WorkoutStubs.FIRST_EXERCISE_ID,
                    setTargets = listOf(
                        SetTargetUpdate(0, targetLoadKg = 65.0, targetDescription = "Heavy top set"),
                        SetTargetUpdate(1, targetReps = 9),
                        SetTargetUpdate(2),
                    ),
                ),
            ),
            emptyList(),
            emptyList(),
        )

        assertThat(result.exercises.single().sets)
            .usingRecursiveComparison()
            .isEqualTo(WorkoutStubs.EditWorkoutPlan.mixedTargetSets)
    }

    @Test
    @Sql("classpath:sql/workout/workout-plan-uncompleted.sql")
    fun `edit plan removes original set before appending new ones`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            null,
            null,
            listOf(
                ChangeExerciseInput(
                    WorkoutStubs.FIRST_EXERCISE_ID,
                    addSets = listOf(WorkoutSetInput(targetReps = 12)),
                    removeSets = listOf(0),
                ),
            ),
            emptyList(),
            emptyList(),
        )

        assertThat(result.exercises.single().sets.map { it.targetReps }).containsExactly(8, 10, 12)
        assertThat(result.exercises.single().sets.map { it.number }).containsExactly(0, 1, 2)
    }

    @Test
    @Sql("classpath:sql/workout/workout-plan-uncompleted.sql")
    fun `edit plan can remove an exercise without results`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            null,
            null,
            emptyList(),
            emptyList(),
            listOf(WorkoutStubs.FIRST_EXERCISE_ID),
        )

        assertThat(result.exercises).isEmpty()
    }

    @Test
    @Sql("classpath:sql/workout/reset-and-seed.sql")
    fun `edit plan can update only date and preserve description`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            LocalDate.of(2026, 11, 12),
            null,
            emptyList(),
            emptyList(),
            emptyList(),
        )

        assertThat(result.date).isEqualTo(LocalDate.of(2026, 11, 12))
        assertThat(result.description).isEqualTo("Upper body strength")
    }

    @Test
    @Sql("classpath:sql/workout/workout-plan-uncompleted.sql")
    fun `edit plan removes multiple sets and shifts the remaining indexes`() {
        val result = workoutService.editWorkoutPlan(
            WorkoutStubs.USER_NAME,
            WorkoutStubs.WORKOUT_ID,
            null,
            null,
            listOf(
                ChangeExerciseInput(WorkoutStubs.FIRST_EXERCISE_ID, removeSets = listOf(0, 2)),
            ),
            emptyList(),
            emptyList(),
        )

        assertThat(result.exercises.single().sets.map { it.number }).containsExactly(0)
        assertThat(result.exercises.single().sets.single().targetReps).isEqualTo(8)
    }
}
