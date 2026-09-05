package com.vimes.unit

import com.vimes.domain.model.*
import com.vimes.dto.*
import com.vimes.exception.DuplicateReceiptNumberException
import com.vimes.exception.InvalidReceiptStatusException
import com.vimes.exception.ResourceNotFoundException
import com.vimes.repository.GoodsReceiptRepository
import com.vimes.repository.InventoryBalanceRepository
import com.vimes.repository.MasterDataRepository
import com.vimes.service.GoodsReceiptService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class GoodsReceiptServiceTest {

    private lateinit var masterDataRepository: MasterDataRepository
    private lateinit var goodsReceiptRepository: GoodsReceiptRepository
    private lateinit var inventoryBalanceRepository: InventoryBalanceRepository
    private lateinit var goodsReceiptService: GoodsReceiptService

    @BeforeEach
    fun setup() {
        masterDataRepository = MasterDataRepository()
        goodsReceiptRepository = GoodsReceiptRepository(masterDataRepository)
        inventoryBalanceRepository = InventoryBalanceRepository()
        goodsReceiptService = GoodsReceiptService(
            goodsReceiptRepository,
            masterDataRepository,
            inventoryBalanceRepository
        )
    }

    @Test
    fun `test create goods receipt calculates total amount and updates inventory`() {
        val productId = UUID.fromString("e7d2b8a0-1234-4567-89ab-cdef01234567")
        val warehouseId = UUID.fromString("c9a646d3-9c61-4cd7-bf5b-9b4dc257850a")
        val orgId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")

        val request = CreateGoodsReceiptRequest(
            receiptNumber = "PNK-UNIT-001",
            receiptDate = LocalDate.now(),
            organizationId = orgId,
            warehouseId = warehouseId,
            receiptType = ReceiptType.PURCHASE,
            delivererName = "Nguyễn Văn Test Unit",
            status = ReceiptStatus.CONFIRMED,
            items = listOf(
                ReceiptItemInput(
                    productId = productId,
                    productNameSnapshot = "Thép cuộn Phi 6",
                    unitSnapshot = "Kg",
                    docQty = 100.0,
                    actualQty = 90.0,
                    unitPrice = 15000.0
                )
            )
        )

        val response = goodsReceiptService.createGoodsReceipt(request)

        assertTrue(response.success)
        assertEquals(1350000.0, response.data.totalAmount)

        // Verify inventory balance updated
        val balance = inventoryBalanceRepository.getBalance(warehouseId, productId)
        assertEquals(90.0, balance)
    }

    @Test
    fun `test create goods receipt with duplicate number throws exception`() {
        val warehouseId = UUID.fromString("c9a646d3-9c61-4cd7-bf5b-9b4dc257850a")
        val orgId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")

        val request = CreateGoodsReceiptRequest(
            receiptNumber = "PNK-2026-0001", // Existing number in seed data
            receiptDate = LocalDate.now(),
            organizationId = orgId,
            warehouseId = warehouseId,
            delivererName = "Nguyễn Văn Test",
            items = listOf(
                ReceiptItemInput(
                    productId = UUID.fromString("e7d2b8a0-1234-4567-89ab-cdef01234567"),
                    productNameSnapshot = "Thép cuộn Phi 6",
                    unitSnapshot = "Kg",
                    docQty = 10.0,
                    actualQty = 10.0,
                    unitPrice = 15000.0
                )
            )
        )

        assertThrows(DuplicateReceiptNumberException::class.java) {
            goodsReceiptService.createGoodsReceipt(request)
        }
    }

    @Test
    fun `test update receipt in CANCELLED status throws InvalidReceiptStatusException`() {
        val receiptId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        
        // Cancel the receipt first
        goodsReceiptService.deleteOrCancelGoodsReceipt(receiptId)

        val updateRequest = UpdateGoodsReceiptRequest(
            organizationId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
            warehouseId = UUID.fromString("c9a646d3-9c61-4cd7-bf5b-9b4dc257850a"),
            delivererName = "Lê Văn Cập Nhật",
            items = listOf(
                ReceiptItemInput(
                    productId = UUID.fromString("e7d2b8a0-1234-4567-89ab-cdef01234567"),
                    productNameSnapshot = "Thép cuộn Phi 6",
                    unitSnapshot = "Kg",
                    docQty = 10.0,
                    actualQty = 10.0,
                    unitPrice = 15000.0
                )
            )
        )

        assertThrows(InvalidReceiptStatusException::class.java) {
            goodsReceiptService.updateGoodsReceipt(receiptId, updateRequest)
        }
    }

    @Test
    fun `test delete or cancel confirmed receipt performs stock reversal`() {
        val productId = UUID.fromString("e7d2b8a0-1234-4567-89ab-cdef01234567")
        val warehouseId = UUID.fromString("c9a646d3-9c61-4cd7-bf5b-9b4dc257850a")
        val orgId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")

        // Create a confirmed receipt
        val createRequest = CreateGoodsReceiptRequest(
            receiptNumber = "PNK-CANCEL-001",
            receiptDate = LocalDate.now(),
            organizationId = orgId,
            warehouseId = warehouseId,
            delivererName = "Trần Văn Test",
            status = ReceiptStatus.CONFIRMED,
            items = listOf(
                ReceiptItemInput(
                    productId = productId,
                    productNameSnapshot = "Thép cuộn Phi 6",
                    unitSnapshot = "Kg",
                    docQty = 50.0,
                    actualQty = 50.0,
                    unitPrice = 15000.0
                )
            )
        )
        val created = goodsReceiptService.createGoodsReceipt(createRequest)
        assertEquals(50.0, inventoryBalanceRepository.getBalance(warehouseId, productId))

        // Cancel it
        val cancelResponse = goodsReceiptService.deleteOrCancelGoodsReceipt(created.data.receiptId)
        assertEquals(DeleteAction.CANCELLED_AND_REVERSED, cancelResponse.data.action)

        // Inventory should be reversed to 0
        assertEquals(0.0, inventoryBalanceRepository.getBalance(warehouseId, productId))
    }

    @Test
    fun `test get non-existent receipt throws ResourceNotFoundException`() {
        val nonExistentId = UUID.randomUUID()
        assertThrows(ResourceNotFoundException::class.java) {
            goodsReceiptService.getGoodsReceiptById(nonExistentId)
        }
    }
}
