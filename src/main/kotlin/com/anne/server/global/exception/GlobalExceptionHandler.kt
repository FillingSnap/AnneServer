package com.anne.server.global.exception

import com.anne.server.global.exception.dto.ExceptionResponse
import com.anne.server.global.validation.dto.ValidationErrorField
import com.anne.server.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = logger()

    @ExceptionHandler(CustomException::class)
    fun handlerCustomException(
        e: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        log.error("{} - {}", getClientIpAddr(request), e.errorCode.message)

        return ResponseEntity.status(e.errorCode.status).body(
            ExceptionResponse(
                status = e.errorCode.status,
                requestUri = request.requestURI,
                data = e.errorCode.message
            )
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handlerNoResourceFoundExceptionHandler(
        e: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        val errorCode = ErrorCode.WRONG_URL
        log.error("{} - {} ({})", getClientIpAddr(request), errorCode.message, request.requestURI)

        return ResponseEntity.status(errorCode.status).body(
            ExceptionResponse(
                status = errorCode.status,
                requestUri = request.requestURI,
                data = errorCode.message
            )
        )
    }

    // HTTP Not Readable
    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handlerHttpMessageNotReadableException(
        e: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        log.error("{} - NoResourceFoundException", getClientIpAddr(request))

        println(request.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ExceptionResponse(
                status = HttpStatus.BAD_REQUEST,
                requestUri = request.requestURI,
                data = "HTTP 요청을 읽을 수 없습니다"
            )
        )
    }

    // 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handlerMethodArgumentTypeMismatchException(
        e: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<ValidationErrorField>> {
        log.error("{} - 타입 불일치", getClientIpAddr(request))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ExceptionResponse(
                status = HttpStatus.BAD_REQUEST,
                requestUri = request.requestURI,
                data = ValidationErrorField(e.name, "${e.requiredType} 타입이 필요합니다")
            )
        )
    }

    // Validation Error
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handlerMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<List<ValidationErrorField>>> {
        log.error("{} - 유효성 검증 실패", getClientIpAddr(request))
        val errors = e.bindingResult.fieldErrors.map { ValidationErrorField(it.field, it.defaultMessage!!) }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ExceptionResponse(
                status = HttpStatus.BAD_REQUEST,
                requestUri = request.requestURI,
                data = errors
            )
        )
    }

    // 보안용
//    @ExceptionHandler(Exception::class)
//    protected fun handlerException(
//        e: Exception,
//        request: HttpServletRequest
//    ): ResponseEntity<ExceptionResponse<String>> {
//        log.error("{} - {}", getClientIpAddr(request), e.message)
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//            ExceptionResponse(
//                status = HttpStatus.INTERNAL_SERVER_ERROR,
//                requestUri = request.requestURI,
//                data = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
//            )
//        )
//    }

    private fun getClientIpAddr(request: HttpServletRequest): String {
        var ip = request.getHeader("X-Forwarded-For")

        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }

        return ip
    }

}