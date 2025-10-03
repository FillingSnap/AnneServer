package com.anne.server.domain_temp.user.service

import com.anne.server.domain_temp.user.dao.UserRepository
import com.anne.server.domain_temp.user.domain.User
import com.anne.server.domain_temp.user.dto.UserDto
import com.anne.server.domain_temp.user.dto.response.LoginResponse
import com.anne.server.domain_temp.user.enums.LoginType
import com.anne.server.global.auth.fedCM.FedCMService
import com.anne.server.global.auth.jwt.AuthenticationService
import org.springframework.stereotype.Service

@Service
class UserLoginService (

    private val fedCMService: FedCMService,

    private val userRepository: UserRepository,

    private val authenticationService: AuthenticationService

) {

    fun login(code: String, registrationId: String, type: LoginType): LoginResponse {
        val payload = when (type) {
            LoginType.FEDCM -> fedCMService.getPayload(code, registrationId)
        }

        val uid = payload.uid
        val name = payload.name

        var user = userRepository.findByUidAndProvider(uid, registrationId)
        if (user == null) {
            user = userRepository.save(
                User(
                    name = name,
                    uid = uid,
                    provider = registrationId
                )
            )
        }

        return LoginResponse(
            UserDto.fromEntity(user),
            authenticationService.generateAccessToken(user.id!!.toString(), user.provider, user.uid),
            authenticationService.generateRefreshToken(user.id!!.toString())
        )
    }

}