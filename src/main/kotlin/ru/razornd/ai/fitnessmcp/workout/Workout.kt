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

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "workout")
@EntityListeners(AuditingEntityListener::class)
class Workout(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @field:Column(name = "user_name", nullable = false)
    @Suppress("unused")
    var userName: String,
    @field:Column(nullable = false)
    var date: LocalDate,
    @field:Column(columnDefinition = "text")
    var description: String? = null,
    @field:CreatedDate
    @field:Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
    @field:OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @field:JoinColumn(name = "workout_id", nullable = false)
    @field:OrderColumn(name = "position")
    var exercises: MutableList<WorkoutExercise> = arrayListOf(),
)

@Entity
@Table(name = "workout_exercise")
class WorkoutExercise(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @field:Column(nullable = false)
    var name: String,
    @field:Column(columnDefinition = "text")
    var instructions: String? = null,
    @field:OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @field:JoinColumn(name = "workout_exercise_id", nullable = false)
    @field:OrderColumn(name = "number")
    var sets: MutableList<WorkoutSet> = arrayListOf(),
)

@Entity
@Table(name = "workout_set")
class WorkoutSet(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @field:Column(name = "target_reps")
    var targetReps: Int? = null,
    @field:Column(name = "target_load_kg")
    var targetLoadKg: Double? = null,
    @field:Column(name = "target_description", columnDefinition = "text")
    var targetDescription: String? = null,
    @field:Column(name = "actual_reps")
    var actualReps: Int? = null,
    @field:Column(name = "actual_load_kg")
    var actualLoadKg: Double? = null,
    var effort: Int? = null,
    @field:Column(columnDefinition = "text")
    var feedback: String? = null,
) {
    fun hasResult(): Boolean = actualReps != null || actualLoadKg != null || effort != null || feedback != null
}
