package com.anne.server.presentation.security.auth

object AuthHeaderUtil {

    private const val BEARER_PREFIX = "Bearer "

    fun extractBearer(header: String?): String? =
        header?.trim()
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)
            ?.takeIf { it.isNotBlank() }

}