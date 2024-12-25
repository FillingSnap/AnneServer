package com.anne.server.domain.user.service

import com.anne.server.domain.user.dao.UserRepository
import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService (

    private val userRepository: UserRepository

) {

    @Transactional
    fun getUserById(id: Long): UserDto {
        val user = userRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return UserDto.fromEntity(user)
    }

    @Transactional(readOnly = true)
    fun getStyleList(): List<String> {
        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        return userDto.styleList
    }

    @Transactional
    fun updateStyleList(styleList: List<String>) {
        if (styleList.size > 5) {
            throw CustomException(ErrorCode.TOO_MANY_STYLES)
        }

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        val user = UserDto.toEntity(userDto)
        user.styleList = styleList
        userRepository.save(user)
    }

}