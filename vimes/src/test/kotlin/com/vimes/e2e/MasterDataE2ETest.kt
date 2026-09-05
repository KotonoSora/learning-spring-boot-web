package com.vimes.e2e

import org.hamcrest.Matchers.*
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
import java.nio.charset.StandardCharsets

@SpringBootTest
class MasterDataE2ETest {

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
    fun `e2e test master data endpoints with search, pagination and UTF-8`() {
        // 1. GET /master-data/products
        mockMvc.perform(
            get("/master-data/products")
                .param("search", "thep")
                .param("page", "1")
                .param("limit", "5")
        )
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.pagination.page").value(1))
            .andExpect(jsonPath("$.pagination.limit").value(5))

        // 2. GET /master-data/warehouses
        mockMvc.perform(
            get("/master-data/warehouses")
                .param("search", "ha noi")
        )
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].name", hasItem("Kho Tổng Hà Nội")))
            .andExpect(jsonPath("$.data[*].location", hasItem("KCN Sài Đồng, Long Biên, Hà Nội")))

        // 3. GET /master-data/organizations
        mockMvc.perform(
            get("/master-data/organizations")
                .param("search", "vimes")
        )
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].name", hasItem("CÔNG TY CỔ PHẦN VIMES")))
            .andExpect(jsonPath("$.data[*].department", hasItem("Kế toán - Quản lý Vật tư")))
    }
}
