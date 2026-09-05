package com.vimes.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class MetricsService {
    // Map of (method, route, statusCode) -> count
    private val httpRequests = ConcurrentHashMap<String, AtomicLong>()
    private val dbTransactionErrors = ConcurrentHashMap<String, AtomicLong>()

    fun recordHttpRequest(method: String, route: String, statusCode: Int) {
        val key = """method="$method",route="$route",status_code="$statusCode""""
        httpRequests.computeIfAbsent(key) { AtomicLong(0) }.incrementAndGet()
    }

    fun recordDbTransactionError(operation: String) {
        val key = """operation="$operation""""
        dbTransactionErrors.computeIfAbsent(key) { AtomicLong(0) }.incrementAndGet()
    }

    fun generatePrometheusMetrics(): String {
        val sb = StringBuilder()
        
        sb.append("# HELP vimes_http_requests_total Tổng số lượng HTTP Requests\n")
        sb.append("# TYPE vimes_http_requests_total counter\n")
        if (httpRequests.isEmpty()) {
            sb.append("""vimes_http_requests_total{method="POST",route="/api/v1/goods-receipts",status_code="201"} 0""").append("\n")
        } else {
            httpRequests.forEach { (labels, count) ->
                sb.append("vimes_http_requests_total{$labels} ${count.get()}\n")
            }
        }

        sb.append("# HELP vimes_db_transaction_errors_total Tổng số giao dịch Database Transaction bị Rollback\n")
        sb.append("# TYPE vimes_db_transaction_errors_total counter\n")
        if (dbTransactionErrors.isEmpty()) {
            sb.append("""vimes_db_transaction_errors_total{operation="CREATE_RECEIPT"} 0""").append("\n")
        } else {
            dbTransactionErrors.forEach { (labels, count) ->
                sb.append("vimes_db_transaction_errors_total{$labels} ${count.get()}\n")
            }
        }

        return sb.toString()
    }
}
