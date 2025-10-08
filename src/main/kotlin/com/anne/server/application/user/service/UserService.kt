package com.anne.server.application.user.service

import com.anne.server.application.user.port.`in`.AuthUseCase
import com.anne.server.application.user.port.`in`.UserStyleUseCase
import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.application.user.port.out.TokenProvider
import com.anne.server.application.user.port.out.UserRepository
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(

    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository

): AuthUseCase, UserStyleUseCase {

    @Transactional(readOnly = true)
    override fun getPrincipal(token: String): AuthPrincipal? =
        tokenProvider.validateAccessToken(token)

    @Transactional(readOnly = true)
    override fun getStyleList(id: Long): List<String> =
        userRepository.findById(id)?.styleList
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

    @Transactional
    override fun updateStyleList(
        id: Long,
        styleList: List<String>
    ): List<String> {
        if (styleList.size > 5) {
            throw CustomException(ErrorCode.TOO_MANY_STYLES)
        }

        val user = userRepository.findById(id) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        user.styleList = styleList
        return userRepository.save(user).styleList
    }

}