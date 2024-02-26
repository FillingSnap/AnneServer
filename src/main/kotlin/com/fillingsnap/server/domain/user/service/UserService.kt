package com.fillingsnap.server.domain.user.service

import com.fillingsnap.server.domain.user.dao.UserRepository
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.domain.user.dto.UserDto
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService (

    private val userRepository: UserRepository

) {

    fun loadUserById(id: Long): User {
        return userRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
    }

}