package com.vimes.e2e

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@SpringBootTest
class ObservabilityE2ETest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilter<DefaultMockMvcBuilder>(
                CharacterEncodingFilter("UTF-8", true)
            )
            .build()
    }

    @Test
    fun `e2e test healthz liveness probe`() {
        mockMvc.perform(get("/healthz"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.uptime").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `e2e test ready readiness probe`() {
        mockMvc.perform(get("/ready"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("READY"))
            .andExpect(jsonPath("$.checks.database").value("HEALTHY"))
            .andExpect(jsonPath("$.checks.poolTotal").value(10))
            .andExpect(jsonPath("$.checks.poolIdle").value(8))
            .andExpect(jsonPath("$.checks.poolWaiting").value(0))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `e2e test prometheus metrics exporter`() {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(containsString("# HELP vimes_http_requests_total")))
            .andExpect(content().string(containsString("# TYPE vimes_http_requests_total counter")))
            .andExpect(content().string(containsString("# HELP vimes_db_transaction_errors_total")))
            .andExpect(content().string(containsString("# TYPE vimes_db_transaction_errors_total counter")))
    }
}
