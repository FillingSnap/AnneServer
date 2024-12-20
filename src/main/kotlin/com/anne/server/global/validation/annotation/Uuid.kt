package com.anne.server.global.validation.annotation

import com.anne.server.global.validation.validator.UuidValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(allowedTargets = [
    AnnotationTarget.FIELD
])
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UuidValidator::class])
annotation class Uuid(

    val message: String = "올바르지 않은 Uuid 값입니다",

    val groups: Array<KClass<*>> = [],

    val payload: Array<KClass<out Payload>> = [],

)
