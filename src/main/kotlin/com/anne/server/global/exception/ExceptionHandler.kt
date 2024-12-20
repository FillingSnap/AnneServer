package com.anne.server.global.exception

import com.anne.server.global.exception.dto.ExceptionResponse
import com.anne.server.global.validation.dto.ValidationErrorField
import com.anne.server.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class ExceptionHandler {

    val log = logger()

    @ExceptionHandler(CustomException::class)
    fun handlerCustomException(
        e: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<String>> {
        log.error(e.errorCode.message)

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
        log.error("{} ({})", errorCode.message, request.requestURI)

        return ResponseEntity.status(errorCode.status).body(
            ExceptionResponse(
                status = errorCode.status,
                requestUri = request.requestURI,
                data = errorCode.message
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handlerMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionResponse<List<ValidationErrorField>>> {
        log.error("MethodArgumentNotValidException")
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
    @ExceptionHandler(Exception::class)
    protected fun handlerException(
        e: Exception,
        request: HttpServletRequest
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