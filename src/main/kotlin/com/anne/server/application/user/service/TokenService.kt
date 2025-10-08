package com.anne.server.application.user.service

import com.anne.server.application.user.port.`in`.LoginUseCase
import com.anne.server.application.user.port.`in`.TokenRefreshUseCase
import com.anne.server.application.user.dto.LoginInfo
import com.anne.server.application.user.port.out.ThirdPartyAuthProvider
import com.anne.server.application.user.port.out.InMemoryDbRepository
import com.anne.server.application.user.port.out.TokenProvider
import com.anne.server.application.user.port.out.UserRepository
import com.anne.server.domain.User
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class TokenService(

    @Value("\${jwt.token.refresh-expiration-time}")
    private val refreshPeriod: Long,

    private val thirdPartyAuthProvider: ThirdPartyAuthProvider,

    private val userRepository: UserRepository,

    private val tokenProvider: TokenProvider,

    private val inMemoryDbRepository: InMemoryDbRepository

): LoginUseCase, TokenRefreshUseCase {

    @Transactional
    override fun login(code: String, registrationId: String): LoginInfo {
        val payload = thirdPartyAuthProvider.getPayload(code, registrationId)

        val user = userRepository.findByUidAndProvider(payload.uid, registrationId)
            ?: userRepository.save(
                User(
                    name = payload.name,
                    uid = payload.uid,
                    provider = registrationId
                )
            )

        return LoginInfo(
            user,
            tokenProvider.generateAccessToken(user.id!!.toString(), user.provider, user.uid),
            inMemoryDbRepository.setValue(
                user.id!!.toString(),
                tokenProvider.generateRefreshToken(user.id!!.toString()), Duration.ofMillis(refreshPeriod)
            )
        )
    }

    @Transactional(readOnly = true)
    override fun refreshAccessToken(refreshToken: String): String {
        val id = tokenProvider.validateRefreshToken(refreshToken)
            ?: throw CustomException(ErrorCode.INVALID_TOKEN)

        if (refreshToken != inMemoryDbRepository.getValue(id)) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }

        val user = userRepository.findById(id.toLong())
            ?: throw CustomException(ErrorCode.INVALID_TOKEN)

        return tokenProvider.generateAccessToken(id, user.provider, user.uid)
    }

    @Transactional(readOnly = true)
    override fun generateRefreshToken(id: Long): String =
        tokenProvider.generateRefreshToken(id.toString())

}