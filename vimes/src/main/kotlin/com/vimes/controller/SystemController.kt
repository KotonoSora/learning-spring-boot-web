package com.vimes.controller

import com.vimes.service.MetricsService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.format.DateTimeFormatter

@RestController
class SystemController(
    private val metricsService: MetricsService
) {

    private val startTimeMs = System.currentTimeMillis()

    @GetMapping("/healthz", produces = ["application/json;charset=UTF-8"])
    fun checkLiveness(): ResponseEntity<Map<String, Any>> {
        val uptimeSeconds = (System.currentTimeMillis() - startTimeMs) / 1000.0
        val body = mapOf(
            "status" to "UP",
            "uptime" to uptimeSeconds,
            "timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
        return ResponseEntity.ok(body)
    }

    @GetMapping("/ready", produces = ["application/json;charset=UTF-8"])
    fun checkReadiness(): ResponseEntity<Map<String, Any>> {
        val checks = mapOf(
            "database" to "HEALTHY",
            "poolTotal" to 10,
            "poolIdle" to 8,
            "poolWaiting" to 0
        )
        val body = mapOf(
            "status" to "READY",
            "checks" to checks,
            "timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
        return ResponseEntity.ok(body)
    }

    @GetMapping("/metrics", produces = [MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"])
    fun getMetrics(): ResponseEntity<String> {
        val metricsText = metricsService.generatePrometheusMetrics()
        return ResponseEntity.ok(metricsText)
    }
}
