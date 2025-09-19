package com.anne.server.global.exception.handler

import com.anne.server.global.exception.dto.ExceptionResponse
import com.anne.server.global.exception.enums.ErrorCode
import com.anne.server.global.exception.exceptions.CustomException
import com.anne.server.global.validation.dto.ValidationErrorField
import com.anne.server.logger
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = logger()

    // Custom Exception
    @ExceptionHandler(CustomException::class)
    private fun handlerCustomException(
        e: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        MDC.put("alert", e.errorCode.alert.toString())

        return ResponseEntity.status(e.errorCode.status).body(
            ExceptionResponse(
                status = e.errorCode.status,
                requestUri = request.requestURI,
                data = e.errorCode.message
            )
        )
    }

    // Resource Not Found
    @ExceptionHandler(NoResourceFoundException::class)
    private fun handlerNoResourceFoundExceptionHandler(
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        val errorCode = ErrorCode.WRONG_URL
        MDC.put("alert", errorCode.alert.toString())

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
    private fun handlerHttpMessageNotReadableException(
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        MDC.put("alert", false.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ExceptionResponse(
                status = HttpStatus.BAD_REQUEST,
                requestUri = request.requestURI,
                data = "HTTP 요청을 읽을 수 없습니다"
            )
        )
    }

    // Multifile 용량 제어
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    private fun handlerMaxUploadSizeExceededException(
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>?> {
        val errorCode = ErrorCode.TOO_LARGE_MULTIFILE

        return ResponseEntity.status(errorCode.status).body(
            ExceptionResponse(
                status = errorCode.status,
                requestUri = request.requestURI,
                data = errorCode.message
            )
        )
    }

    // 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    private fun handlerMethodArgumentTypeMismatchException(
        e: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<ValidationErrorField>> {
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
    private fun handlerMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<List<ValidationErrorField>>> {
        val errors = e.bindingResult.fieldErrors.map {
            ValidationErrorField(it.field, it.defaultMessage!!)
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ExceptionResponse(
                status = HttpStatus.BAD_REQUEST,
                requestUri = request.requestURI,
                data = errors
            )
        )
    }

    // 보안용
    @ExceptionHandler(Exception::class)
    private fun handlerException(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<String>> {
        log.error(e.message)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ExceptionResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                requestUri = request.requestURI,
                data = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
            )
        )
    }

}