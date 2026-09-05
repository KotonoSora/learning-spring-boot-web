package com.vimes.exception

import com.vimes.dto.ErrorResponse
import com.vimes.dto.FieldErrorDetail
import com.vimes.dto.ValidationErrorResponse
import com.vimes.util.RequestContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            val formattedPath = fieldError.field
                .replace("[", ".")
                .replace("]", "")
            FieldErrorDetail(
                path = formattedPath,
                message = fieldError.defaultMessage ?: "Dữ liệu không hợp lệ"
            )
        }
        val response = ValidationErrorResponse(
            success = false,
            message = "Lỗi xác thực dữ liệu đầu vào (Validation Error)",
            requestId = RequestContext.getRequestId(),
            errors = fieldErrors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            success = false,
            message = ex.message ?: "Không tìm thấy tài nguyên.",
            requestId = RequestContext.getRequestId()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(DuplicateReceiptNumberException::class)
    fun handleDuplicate(ex: DuplicateReceiptNumberException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            success = false,
            message = ex.message ?: "Số phiếu đã tồn tại.",
            requestId = RequestContext.getRequestId()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(InvalidReceiptStatusException::class)
    fun handleInvalidStatus(ex: InvalidReceiptStatusException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            success = false,
            message = ex.message ?: "Trạng thái chứng từ không hợp lệ.",
            requestId = RequestContext.getRequestId()
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response)
    }

    @ExceptionHandler(RateLimitExceededException::class)
    fun handleRateLimit(ex: RateLimitExceededException): ResponseEntity<ErrorResponse> {
        val headers = HttpHeaders()
        headers.add("Retry-After", ex.retryAfterSeconds.toString())
        val response = ErrorResponse(
            success = false,
            message = ex.message ?: "Tần suất tạo phiếu quá nhanh. Vui lòng chờ trong giây lát.",
            requestId = RequestContext.getRequestId()
        )
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(_ex: Exception): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            success = false,
            message = "Đã xảy ra lỗi trong quá trình xử lý chứng từ.",
            requestId = RequestContext.getRequestId()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
