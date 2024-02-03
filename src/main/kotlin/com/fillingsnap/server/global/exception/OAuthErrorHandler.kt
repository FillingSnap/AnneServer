package com.fillingsnap.server.global.exception

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler

class OAuthErrorHandler: DefaultResponseErrorHandler() {

    override fun handleError(response: ClientHttpResponse) {
        throw CustomException(ErrorCode.GOOGLE_UNAUTHORIZED)
    }

}