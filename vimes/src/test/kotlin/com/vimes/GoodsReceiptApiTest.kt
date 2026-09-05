package com.vimes

import tools.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import java.nio.charset.StandardCharsets

@SpringBootTest
class GoodsReceiptApiTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    fun `test Vietnamese UTF-8 encoding in JSON responses`() {
        // Test UTF-8 Vietnamese character support in Master Data
        mockMvc.perform(get("/master-data/products?search=Thép"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value(containsString("Thép cuộn Phi 6")))

        mockMvc.perform(get("/master-data/warehouses").param("search", "ha noi"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.data[*].name", hasItem("Kho Tổng Hà Nội")))
            .andExpect(jsonPath("$.data[*].location", hasItem("KCN Sài Đồng, Long Biên, Hà Nội")))

        mockMvc.perform(get("/master-data/organizations").param("search", "vimes"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.data[*].name", hasItem("CÔNG TY CỔ PHẦN VIMES")))
            .andExpect(jsonPath("$.data[*].department", hasItem("Kế toán - Quản lý Vật tư")))
    }

    @Test
    fun `test pagination across 3 pages for goods receipts and master data`() {
        // 1. Goods Receipts Pagination: Page 1, Page 2, Page 3 (limit = 10)
        mockMvc.perform(get("/goods-receipts?page=1&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(10)))
            .andExpect(jsonPath("$.pagination.page").value(1))
            .andExpect(jsonPath("$.pagination.limit").value(10))
            .andExpect(jsonPath("$.pagination.totalItems", greaterThanOrEqualTo(30)))
            .andExpect(jsonPath("$.pagination.totalPages", greaterThanOrEqualTo(3)))

        mockMvc.perform(get("/goods-receipts?page=2&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(10)))
            .andExpect(jsonPath("$.pagination.page").value(2))

        mockMvc.perform(get("/goods-receipts?page=3&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(10))))
            .andExpect(jsonPath("$.pagination.page").value(3))

        // 2. Master Data Products Pagination: Page 1, Page 2, Page 3 (limit = 8)
        mockMvc.perform(get("/master-data/products?page=1&limit=8"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(8)))
            .andExpect(jsonPath("$.pagination.page").value(1))

        mockMvc.perform(get("/master-data/products?page=2&limit=8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(8)))
            .andExpect(jsonPath("$.pagination.page").value(2))

        mockMvc.perform(get("/master-data/products?page=3&limit=8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(8))))
            .andExpect(jsonPath("$.pagination.page").value(3))
    }

    @Test
    fun `test query URL params for filtering and CORS headers`() {
        // Test filtering by date range, warehouseId, and status
        mockMvc.perform(
            get("/goods-receipts?fromDate=2026-08-01&toDate=2026-08-31&warehouseId=c9a646d3-9c61-4cd7-bf5b-9b4dc257850a&status=CONFIRMED&page=1&limit=5")
        )
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.pagination.page").value(1))
            .andExpect(jsonPath("$.pagination.limit").value(5))

        // Test CORS preflight request with headers and query params
        mockMvc.perform(
            options("/goods-receipts")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
        )
            .andExpect(status().isOk)
            .andExpect(header().exists("Access-Control-Allow-Origin"))
    }

    @Test
    fun `test fuzzy search and unaccented Vietnamese search`() {
        // 1. Unaccented search for "thep" (should match "Thép cuộn Phi 6", "Thép cây", etc.)
        mockMvc.perform(get("/master-data/products?search=thep"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(1))))

        // 2. Fuzzy search for "nguyen" on Goods Receipts (should match "Nguyễn Văn A")
        mockMvc.perform(get("/goods-receipts?search=nguyen"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(1))))

        // 3. Search receipt number "PNK-2026-0001"
        mockMvc.perform(get("/goods-receipts?search=pnk-2026-0001"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.data[0].receiptNumber").value("PNK-2026-0001"))
    }

    @Test
    fun `test complete Goods Receipt lifecycle and master data endpoints`() {
        // 1. Check System Endpoints
        mockMvc.perform(get("/healthz"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))

        mockMvc.perform(get("/ready"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("READY"))

        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("vimes_http_requests_total")))

        // 2. Create New Goods Receipt with Vietnamese UTF-8 text
        val receiptPayload = mapOf(
            "receiptNumber" to "PNK-2026-NEW01",
            "receiptDate" to "2026-08-18",
            "actualReceivedDate" to "2026-08-18",
            "organizationId" to "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "warehouseId" to "c9a646d3-9c61-4cd7-bf5b-9b4dc257850a",
            "receiptType" to "PURCHASE",
            "description" to "Nhập kho thép cuộn phục vụ dự án VIMES Tower theo hóa đơn số 99882",
            "delivererName" to "Nguyễn Văn A",
            "docReference" to "HĐ-TEST",
            "docDate" to "2026-08-17",
            "docOrigin" to "Công ty Thép Việt Nhật",
            "debitAccount" to "152",
            "creditAccount" to "331",
            "totalAmountWords" to "Một triệu bốn trăm bảy mươi bảy nghìn năm trăm đồng",
            "attachedDocCount" to "1 hóa đơn GTGT gốc",
            "creatorName" to "Lê Văn Lập",
            "storekeeperName" to "Trần Văn Kho",
            "chiefAccountantName" to "Phạm Thị Trưởng",
            "status" to "CONFIRMED",
            "items" to listOf(
                mapOf(
                    "productId" to "e7d2b8a0-1234-4567-89ab-cdef01234567",
                    "productNameSnapshot" to "Thép cuộn Phi 6",
                    "unitSnapshot" to "Kg",
                    "docQty" to 100.0,
                    "actualQty" to 98.5,
                    "unitPrice" to 15000.0,
                    "debitAccount" to "152",
                    "creditAccount" to "331",
                    "note" to "Hao hụt 1.5kg do vận chuyển"
                )
            )
        )

        val createResult = mockMvc.perform(
            post("/goods-receipts")
                .header("X-Request-Id", "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiptPayload))
        )
            .andExpect(status().isCreated)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(header().string("X-Request-Id", "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Lập phiếu nhập kho thành công (Mẫu 01 - VT)"))
            .andExpect(jsonPath("$.data.receiptId").exists())
            .andExpect(jsonPath("$.data.totalAmount").value(1477500.0))
            .andReturn()

        val responseJson = objectMapper.readTree(createResult.response.contentAsString)
        val createdId = responseJson.get("data").get("receiptId").asString()

        // 3. Duplicate Receipt Number -> HTTP 409
        mockMvc.perform(
            post("/goods-receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiptPayload))
        )
            .andExpect(status().isConflict)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(false))

        // 4. Validation Error -> HTTP 400
        val invalidPayload = receiptPayload.toMutableMap().apply {
            put("receiptNumber", "")
        }
        mockMvc.perform(
            post("/goods-receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Lỗi xác thực dữ liệu đầu vào (Validation Error)"))
            .andExpect(jsonPath("$.errors", hasSize<Any>(greaterThanOrEqualTo(1))))

        // 5. Get Detail
        mockMvc.perform(get("/goods-receipts/$createdId"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId as Any))
            .andExpect(jsonPath("$.data.receiptNumber").value("PNK-2026-NEW01"))
            .andExpect(jsonPath("$.data.delivererName").value("Nguyễn Văn A"))
            .andExpect(jsonPath("$.data.totalAmountWords").value("Một triệu bốn trăm bảy mươi bảy nghìn năm trăm đồng"))
            .andExpect(jsonPath("$.data.totalAmount").value(1477500.0))
            .andExpect(jsonPath("$.data.items", hasSize<Any>(1)))

        // 6. Update Goods Receipt
        val updatePayload = mapOf(
            "receiptDate" to "2026-08-18",
            "organizationId" to "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "warehouseId" to "c9a646d3-9c61-4cd7-bf5b-9b4dc257850a",
            "delivererName" to "Nguyễn Văn A Cập Nhật",
            "items" to listOf(
                mapOf(
                    "productId" to "e7d2b8a0-1234-4567-89ab-cdef01234567",
                    "productNameSnapshot" to "Thép cuộn Phi 6",
                    "unitSnapshot" to "Kg",
                    "docQty" to 100.0,
                    "actualQty" to 100.0,
                    "unitPrice" to 15000.0
                )
            )
        )

        mockMvc.perform(
            put("/goods-receipts/$createdId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        )
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Cập nhật phiếu nhập kho và điều chỉnh tồn kho thành công"))
            .andExpect(jsonPath("$.data.totalAmount").value(1500000.0))

        // 7. Cancel Goods Receipt (Stock Reversal)
        mockMvc.perform(delete("/goods-receipts/$createdId"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Đã hủy chứng từ nhập kho và hoàn trả tồn kho thành công (Stock Reversal)"))
            .andExpect(jsonPath("$.data.action").value("CANCELLED_AND_REVERSED"))

        // 8. Update CANCELLED Goods Receipt -> HTTP 422
        mockMvc.perform(
            put("/goods-receipts/$createdId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        )
            .andExpect(status().is4xxClientError)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Không thể chỉnh sửa phiếu nhập đã ở trạng thái CANCELLED."))
    }
}
