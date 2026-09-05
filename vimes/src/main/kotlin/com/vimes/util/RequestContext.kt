package com.vimes.util

import java.util.UUID

object RequestContext {
    private val requestIdThreadLocal = ThreadLocal<String>()

    fun getRequestId(): String {
        return requestIdThreadLocal.get() ?: UUID.randomUUID().toString()
    }

    fun setRequestId(requestId: String) {
        requestIdThreadLocal.set(requestId)
    }

    fun clear() {
        requestIdThreadLocal.remove()
    }
}
