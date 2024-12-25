package com.anne.server.global.exception

import com.anne.server.global.exception.dto.ExceptionResponse
import com.anne.server.global.validation.dto.ValidationErrorField
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

    @ExceptionHandler(CustomException::class)
    fun handlerCustomException(
        e: CustomException,
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<String>> {
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
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<String>> {
        val errorCode = ErrorCode.WRONG_URL

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
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<String>> {

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
    protected fun handlerMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<List<ValidationErrorField>>> {
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
        request: HttpServletRequest,
    ): ResponseEntity<ExceptionResponse<String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ExceptionResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                requestUri = request.requestURI,
                data = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
            )
        )
    }

}