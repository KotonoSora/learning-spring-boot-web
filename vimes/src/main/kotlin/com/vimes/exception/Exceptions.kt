package com.vimes.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class DuplicateReceiptNumberException(message: String) : RuntimeException(message)

class InvalidReceiptStatusException(message: String) : RuntimeException(message)

class RateLimitExceededException(
    message: String = "Tần suất tạo phiếu quá nhanh. Vui lòng chờ trong giây lát.",
    val retryAfterSeconds: Long = 60
) : RuntimeException(message)
