package com.anne.server.domain.user.service

import com.anne.server.domain.user.dao.UserRepository
import com.anne.server.domain.user.domain.User
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
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