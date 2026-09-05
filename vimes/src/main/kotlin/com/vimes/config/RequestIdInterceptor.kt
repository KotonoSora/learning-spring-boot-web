package com.vimes.config

import com.vimes.service.MetricsService
import com.vimes.util.RequestContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import java.util.UUID

@Component
class RequestIdInterceptor(
    private val metricsService: MetricsService
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val incomingRequestId = request.getHeader("X-Request-Id")
        val requestId = if (!incomingRequestId.isNullOrBlank()) {
            incomingRequestId
        } else {
            UUID.randomUUID().toString()
        }
        RequestContext.setRequestId(requestId)
        response.setHeader("X-Request-Id", requestId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val method = request.method
        val route = (request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String)
            ?: request.requestURI
        val statusCode = response.status
        metricsService.recordHttpRequest(method, route, statusCode)
        RequestContext.clear()
    }
}
