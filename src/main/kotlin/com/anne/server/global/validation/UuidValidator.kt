package com.anne.server.global.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

class UuidValidator: ConstraintValidator<Uuid?, String> {

    private val uuidRegex = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    )

    override fun isValid(value: String, context: ConstraintValidatorContext): Boolean {
        return uuidRegex.matcher(value).matches()
    }

}