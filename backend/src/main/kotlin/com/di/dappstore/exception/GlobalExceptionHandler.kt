package com.di.dappstore.exception

import com.di.dappstore.model.vo.ApiResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): Mono<ResponseEntity<ApiResponse<Nothing>>> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString("; ")

        logger.warn { "Validation error: $errors" }

        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error(errors, 400))
        )
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleInputException(ex: ServerWebInputException): Mono<ResponseEntity<ApiResponse<Nothing>>> {
        logger.warn { "Input error: ${ex.message}" }

        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.reason ?: "请求参数错误", 400))
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): Mono<ResponseEntity<ApiResponse<Nothing>>> {
        logger.warn { "Business error: ${ex.message}" }

        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.message ?: "操作失败", 400))
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<ResponseEntity<ApiResponse<Nothing>>> {
        logger.warn { "Argument error: ${ex.message}" }

        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.message ?: "参数错误", 400))
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundException(ex: NoSuchElementException): Mono<ResponseEntity<ApiResponse<Nothing>>> {
        logger.warn { "Not found: ${ex.message}" }

        return Mono.just(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.message ?: "资源不存在", 404))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): Mono<ResponseEntity<ApiResponse<Nothing>>> {
        logger.error(ex) { "Unexpected error: ${ex.message}" }

        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("服务器内部错误", 500))
        )
    }
}
