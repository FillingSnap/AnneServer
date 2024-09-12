package com.anne.server.domain.user.service

import com.anne.server.domain.user.dao.UserRepository
import com.anne.server.domain.user.domain.User
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService (

    private val userRepository: UserRepository

) {

    fun getUserById(id: Long): User {
        return userRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
    }

    fun getStyleList(): List<String> {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        return user.styleList
    }

    fun updateStyleList(styleList: List<String>) {
        if (styleList.size > 5) {
            throw CustomException(ErrorCode.TOO_MANY_STYLES)
        }

        val user = SecurityContextHolder.getContext().authentication.principal as User
        user.styleList = styleList
        userRepository.save(user)
    }

}