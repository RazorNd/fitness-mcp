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

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(
    validatedBy = [
        NonRepeatedSetResultChangesValidator::class,
        NonRepeatedChangeExercisesValidator::class,
        NonRepeatedSetTargetUpdatesValidator::class,
    ],
)
annotation class NonRepeated(
    val message: String = "must not contain repeated values",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out jakarta.validation.Payload>> = [],
)

class NonRepeatedSetResultChangesValidator : ConstraintValidator<NonRepeated, List<SetResultChangeInput>> {
    override fun isValid(value: List<SetResultChangeInput>?, context: ConstraintValidatorContext): Boolean =
        value == null || value.map { it.exerciseId to it.setNumber }.hasNoDuplicates()
}

class NonRepeatedChangeExercisesValidator : ConstraintValidator<NonRepeated, List<ChangeExerciseInput>> {
    override fun isValid(value: List<ChangeExerciseInput>?, context: ConstraintValidatorContext): Boolean =
        value == null || value.map { it.exerciseId }.hasNoDuplicates()
}

class NonRepeatedSetTargetUpdatesValidator : ConstraintValidator<NonRepeated, List<SetTargetUpdate>> {
    override fun isValid(value: List<SetTargetUpdate>?, context: ConstraintValidatorContext): Boolean =
        value == null || value.map { it.setNumber }.hasNoDuplicates()
}

private fun List<*>.hasNoDuplicates(): Boolean = size == toSet().size
