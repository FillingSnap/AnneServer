package com.fillingsnap.server.global.validation

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
