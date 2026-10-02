package com.gamehub.controller

import com.gamehub.dto.ErrorResponse
import com.gamehub.model.NotFoundException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun notFound(e: NotFoundException) = error(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun invalidBody(e: MethodArgumentNotValidException) =
        error(HttpStatus.BAD_REQUEST, e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" })

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun invalidParams(e: HandlerMethodValidationException) =
        error(HttpStatus.BAD_REQUEST, e.allErrors.joinToString { it.defaultMessage.orEmpty() })

    @ExceptionHandler(DataAccessException::class)
    fun redisError(e: DataAccessException) = error(HttpStatus.SERVICE_UNAVAILABLE, e.mostSpecificCause.message)

    private fun error(status: HttpStatus, message: String?) =
        ResponseEntity.status(status).body(ErrorResponse(status.value(), message))
}
