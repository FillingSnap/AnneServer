package com.anne.server.global.auth.oauth

import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler

class OAuthErrorHandler: DefaultResponseErrorHandler() {

    override fun handleError(response: ClientHttpResponse) {
        throw CustomException(ErrorCode.GOOGLE_UNAUTHORIZED)
    }

}