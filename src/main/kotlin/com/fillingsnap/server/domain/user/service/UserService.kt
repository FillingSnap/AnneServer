package com.fillingsnap.server.domain.user.service

import com.fillingsnap.server.domain.user.dao.UserRepository
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.domain.user.dto.UserLoginDto
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserService (

    private val userRepository: UserRepository,

    private val googleService: GoogleService

): UserDetailsService {

    override fun loadUserByUsername(uid: String): UserDetails {
        // todo: 예외처리(해당 uid를 가진 유저가 없는 경우)
        return userRepository.findByUid(uid)!!
    }

    fun login(code: String): UserLoginDto {
        val oAuthTokenVo = googleService.googleOAuthLogin(code)

        // todo: jwt 토큰을 직접 만들어야할 지 고민 필요
        val googleIdTokenVerifier = GoogleIdTokenVerifier(NetHttpTransport(), GsonFactory())
        val googleUserInfo = googleIdTokenVerifier.verify(oAuthTokenVo.idToken)

        val user = userRepository.findByUid(googleUserInfo.payload.subject)
        if (user == null) {
            userRepository.save(User(
                name = googleUserInfo.payload.unknownKeys["name"].toString(),
                uid = googleUserInfo.payload.subject
            ))
        }

        return UserLoginDto(oAuthTokenVo.idToken, oAuthTokenVo.refreshToken)
    }

}