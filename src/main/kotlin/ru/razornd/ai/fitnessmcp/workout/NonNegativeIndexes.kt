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

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NonNegativeIndexesValidator::class])
annotation class NonNegativeIndexes(
    val message: String = "must contain only non-negative indexes",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out jakarta.validation.Payload>> = [],
)

class NonNegativeIndexesValidator : ConstraintValidator<NonNegativeIndexes, List<Int>?> {

    override fun isValid(
        value: List<Int>?,
        context: ConstraintValidatorContext
    ) = value == null || value.all { it >= 0 }
}
