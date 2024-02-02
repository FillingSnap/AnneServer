package com.fillingsnap.server.domain.user.service

import com.fillingsnap.server.domain.user.dao.UserRepository
import com.fillingsnap.server.domain.user.domain.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService (

    private val userRepository: UserRepository

) {

    fun loadUserById(id: Long): User? {
        return userRepository.findByIdOrNull(id)
    }

}