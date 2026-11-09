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

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import java.util.*

@Service
@Validated
class WorkoutService(private val workoutRepository: WorkoutRepository) {

    @Transactional
    fun createWorkout(
        @NotBlank userName: String,
        date: LocalDate?,
        description: String?,
        @NotEmpty exercises: List<@Valid WorkoutExerciseInput>,
    ): WorkoutDto {
        val workout = Workout(userName = userName, date = date ?: LocalDate.now(), description = description)
        exercises.forEach { input -> workout.exercises.add(input.toExercise()) }
        return workoutRepository.save(workout).toDto()
    }

    @Transactional(readOnly = true)
    fun getWorkout(
        @NotBlank userName: String,
        workoutId: UUID,
    ): WorkoutDto = findWorkout(userName, workoutId).toDto()

    @Transactional(readOnly = true)
    fun getWorkoutHistory(
        @NotBlank userName: String,
        @Min(1) @Max(MAX_HISTORY_LIMIT.toLong()) limit: Int?,
    ): WorkoutHistoryDto {
        val boundedLimit = (limit ?: MAX_HISTORY_LIMIT).coerceIn(1, MAX_HISTORY_LIMIT)
        val workouts = workoutRepository.findAllByUserNameOrderByDateDescCreatedAtDesc(
            userName = userName,
            pageable = PageRequest.of(0, boundedLimit)
        )
        return WorkoutHistoryDto(workouts.map { it.toDto() })
    }

    @Transactional
    fun recordSetResults(
        @NotBlank userName: String,
        workoutId: UUID,
        @NonRepeated changes: List<@Valid SetResultChangeInput>,
    ): WorkoutDto {

        val workout = findWorkout(userName, workoutId)

        val exercisesById = workout.exerciseMapById()

        changes.forEach { exercisesById[it.exerciseId].applyChange(it) }

        return workout.toDto()
    }

    @Transactional
    fun editWorkoutPlan(
        @NotBlank userName: String,
        workoutId: UUID,
        date: LocalDate?,
        description: String?,
        @NonRepeated changeExercises: List<@Valid ChangeExerciseInput>,
        addExercises: List<@Valid WorkoutExerciseInput>,
        removeExerciseIds: List<UUID>,
    ): WorkoutDto {
        val workout = findWorkout(userName, workoutId).apply {
            date?.let { this.date = it }
            description?.let { this.description = it }
        }

        val exercisesById = workout.exerciseMapById()

        changeExercises.forEach { exercisesById[it.exerciseId].applyChange(it) }
        workout.exercises.removeAll(removeExerciseIds.map { id -> exercisesById[id].also { requireHasNoChange(it) } })
        workout.exercises.addAll(addExercises.map { it.toExercise() })

        workoutRepository.flush()

        return workout.toDto()
    }

    private fun requireHasNoChange(exercise: WorkoutExercise) {
        require(exercise.hasSetsWithResult()) { "Cannot remove exercise ${exercise.id} because it has a recorded result." }
    }

    private fun findWorkout(userName: String, workoutId: UUID): Workout =
        requireNotNull(workoutRepository.findByIdAndUserName(workoutId, userName)) {
            "Workout $workoutId was not found for authenticated user '$userName'."
        }

    private fun WorkoutExerciseInput.toExercise() = WorkoutExercise(
        name = name,
        instructions = instructions,
        sets = sets.mapTo(ArrayList()) { it.toSet() }
    )

    private fun WorkoutSetInput.toSet() = WorkoutSet(
        targetReps = targetReps,
        targetLoadKg = targetLoadKg,
        targetDescription = targetDescription,
    )

    private fun Workout.toDto() = WorkoutDto(
        workoutId = checkNotNull(id),
        date = date,
        description = description,
        exercises = exercises.mapIndexed { exerciseIndex, exercise ->
            WorkoutExerciseDto(
                exerciseId = checkNotNull(exercise.id),
                position = exerciseIndex,
                name = exercise.name,
                instructions = exercise.instructions,
                sets = exercise.sets.mapIndexed { setIndex, set ->
                    WorkoutSetDto(
                        number = setIndex,
                        targetReps = set.targetReps,
                        targetLoadKg = set.targetLoadKg,
                        targetDescription = set.targetDescription,
                        actualReps = set.actualReps,
                        actualLoadKg = set.actualLoadKg,
                        effort = set.effort,
                        feedback = set.feedback,
                    )
                },
            )
        },
    )

    private fun Workout.exerciseMapById() = ExercisesMap(
        checkNotNull(id) { "Workout $this should be persisted." },
        exercises.associateBy { checkNotNull(it.id) { "Workout $id should be persisted." } }
    )

    private fun WorkoutExercise.applyChange(change: ChangeExerciseInput) {
        change.setTargets.orEmpty().forEach { update ->
            this[update.setNumber].applyChange(update)
        }
        val removedSets = change.removeSets.orEmpty()
            .distinct()
            .sortedDescending()
            .map { index ->
                this[index].also { require(!it.hasResult()) { "Cannot remove set $index because recorded result." } }
            }
        sets.removeAll(removedSets.toSet())
        sets.addAll(change.addSets.orEmpty().map { it.toWorkoutSet() })
    }

    private fun WorkoutSetInput.toWorkoutSet(): WorkoutSet =
        WorkoutSet(
            targetReps = targetReps,
            targetLoadKg = targetLoadKg,
            targetDescription = targetDescription,
        )

    private fun WorkoutExercise.applyChange(change: SetResultChangeInput) =
        this[change.setNumber].applyChange(change)

    private operator fun WorkoutExercise.get(setNumber: Int): WorkoutSet {
        require(setNumber in sets.indices) { "Set $setNumber does not exist in exercise ${id}." }
        return sets[setNumber]
    }

    private fun WorkoutSet.applyChange(change: SetResultChangeInput) {
        actualReps = change.actualReps
        actualLoadKg = change.actualLoadKg
        effort = change.effort
        feedback = change.feedback
    }

    private fun WorkoutSet.applyChange(change: SetTargetUpdate) {
        require(!hasResult()) { "Cannot change targets for set ${change.setNumber} because recorded result." }
        targetReps = change.targetReps
        targetLoadKg = change.targetLoadKg
        targetDescription = change.targetDescription
    }

    private fun WorkoutExercise.hasSetsWithResult(): Boolean = sets.none { it.hasResult() }

    class ExercisesMap(
        private val workoutId: UUID,
        private val exercisesById: Map<UUID, WorkoutExercise>
    ) : Map<UUID, WorkoutExercise> by exercisesById {
        override fun get(key: UUID) = requireNotNull(exercisesById[key]) {
            "Exercise $key not part of the workout $workoutId."
        }
    }

    private companion object {
        const val MAX_HISTORY_LIMIT = 100
    }
}
