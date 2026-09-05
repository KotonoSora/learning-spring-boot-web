package com.vimes.e2e

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
class GoodsReceiptE2ETest {

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
    fun `e2e test create, read, update, delete goods receipt lifecycle`() {
        // 1. Create Goods Receipt (POST /goods-receipts)
        val customRequestId = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
        val createPayload = mapOf(
            "receiptNumber" to "PNK-E2E-001",
            "receiptDate" to "2026-08-18",
            "actualReceivedDate" to "2026-08-18",
            "organizationId" to "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "warehouseId" to "c9a646d3-9c61-4cd7-bf5b-9b4dc257850a",
            "receiptType" to "PURCHASE",
            "description" to "Nhập kho thép cuộn E2E Test",
            "delivererName" to "Nguyễn Văn E2E",
            "docReference" to "HĐ-E2E-001",
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

        val createResponse = mockMvc.perform(
            post("/goods-receipts")
                .header("X-Request-Id", customRequestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload))
        )
            .andExpect(status().isCreated)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(header().string("X-Request-Id", customRequestId))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Lập phiếu nhập kho thành công (Mẫu 01 - VT)"))
            .andExpect(jsonPath("$.data.receiptId").exists())
            .andExpect(jsonPath("$.data.totalAmount").value(1477500.0))
            .andReturn()

        val responseJson = objectMapper.readTree(createResponse.response.contentAsString)
        val createdId = responseJson.get("data").get("receiptId").asString()

        // 2. Read Detail (GET /goods-receipts/{id})
        mockMvc.perform(
            get("/goods-receipts/$createdId")
                .header("X-Request-Id", customRequestId)
        )
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(header().string("X-Request-Id", customRequestId))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId as Any))
            .andExpect(jsonPath("$.data.receiptNumber").value("PNK-E2E-001"))
            .andExpect(jsonPath("$.data.delivererName").value("Nguyễn Văn E2E"))
            .andExpect(jsonPath("$.data.organization.name").value("CÔNG TY CỔ PHẦN VIMES"))
            .andExpect(jsonPath("$.data.warehouse.name").value("Kho Tổng Hà Nội"))
            .andExpect(jsonPath("$.data.items[0].productName").value("Thép cuộn Phi 6"))
            .andExpect(jsonPath("$.data.items[0].amount").value(1477500.0))

        // 3. Search and Read List (GET /goods-receipts?search=...)
        mockMvc.perform(get("/goods-receipts?search=E2E&page=1&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize<Any>(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.data[0].receiptNumber").value("PNK-E2E-001"))

        // 4. Update (PUT /goods-receipts/{id})
        val updatePayload = mapOf(
            "receiptDate" to "2026-08-18",
            "organizationId" to "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "warehouseId" to "c9a646d3-9c61-4cd7-bf5b-9b4dc257850a",
            "delivererName" to "Nguyễn Văn E2E Updated",
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

        // 5. Delete/Cancel (DELETE /goods-receipts/{id})
        mockMvc.perform(delete("/goods-receipts/$createdId"))
            .andExpect(status().isOk)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Đã hủy chứng từ nhập kho và hoàn trả tồn kho thành công (Stock Reversal)"))
            .andExpect(jsonPath("$.data.action").value("CANCELLED_AND_REVERSED"))

        // 6. Attempt Update on Cancelled Receipt -> 422 Unprocessable Entity
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

    @Test
    fun `e2e test HTTP error scenarios 400, 404, 409`() {
        // 1. HTTP 409 Conflict: Duplicate receipt number
        val duplicatePayload = mapOf(
            "receiptNumber" to "PNK-2026-0001",
            "receiptDate" to "2026-08-18",
            "organizationId" to "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "warehouseId" to "c9a646d3-9c61-4cd7-bf5b-9b4dc257850a",
            "delivererName" to "Nguyễn Văn Test",
            "items" to listOf(
                mapOf(
                    "productId" to "e7d2b8a0-1234-4567-89ab-cdef01234567",
                    "productNameSnapshot" to "Thép cuộn Phi 6",
                    "unitSnapshot" to "Kg",
                    "docQty" to 10.0,
                    "actualQty" to 10.0,
                    "unitPrice" to 15000.0
                )
            )
        )

        mockMvc.perform(
            post("/goods-receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicatePayload))
        )
            .andExpect(status().isConflict)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(containsString("PNK-2026-0001 đã tồn tại")))

        // 2. HTTP 400 Bad Request: Missing required field
        val invalidPayload = mapOf(
            "receiptNumber" to "",
            "receiptDate" to "2026-08-18",
            "organizationId" to "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            "warehouseId" to "c9a646d3-9c61-4cd7-bf5b-9b4dc257850a",
            "delivererName" to "",
            "items" to emptyList<Any>()
        )

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

        // 3. HTTP 404 Not Found: Non-existent UUID
        val randomUuid = "a0000000-0000-0000-0000-000000000000"
        mockMvc.perform(get("/goods-receipts/$randomUuid"))
            .andExpect(status().isNotFound)
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Không tìm thấy phiếu nhập kho với ID đã cung cấp."))
    }
}
