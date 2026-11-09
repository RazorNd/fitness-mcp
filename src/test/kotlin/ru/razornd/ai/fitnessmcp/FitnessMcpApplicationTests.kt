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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import ru.razornd.ai.fitnessmcp.workout.WorkoutExerciseInput
import ru.razornd.ai.fitnessmcp.workout.WorkoutRepository
import ru.razornd.ai.fitnessmcp.workout.WorkoutService
import ru.razornd.ai.fitnessmcp.workout.WorkoutSetInput
import java.time.LocalDate
import java.util.*

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class FitnessMcpApplicationTests {

    @Autowired
    private lateinit var workoutService: WorkoutService

    @Autowired
    private lateinit var workoutRepository: WorkoutRepository

    @Test
    fun contextLoads() {
    }

    @Test
    fun `new workout and child roots get database ids and created date`() {
        val userName = "${UUID.randomUUID()}@example.test"
        val workout = workoutService.createWorkout(
            userName,
            LocalDate.of(2026, 11, 8),
            null,
            listOf(
                WorkoutExerciseInput(
                    "Bench press",
                    sets = listOf(WorkoutSetInput(targetReps = 8, targetLoadKg = 50.0)),
                ),
            ),
        )

        assertThat(workout.workoutId).isNotNull()
        assertThat(workout.exercises).hasSize(1)
        assertThat(workout.exercises.single().exerciseId).isNotNull()
        assertThat(workout.exercises.single().sets).hasSize(1)
        assertThat(workoutRepository.findByIdAndUserName(workout.workoutId, userName)?.createdAt).isNotNull()
    }

}
