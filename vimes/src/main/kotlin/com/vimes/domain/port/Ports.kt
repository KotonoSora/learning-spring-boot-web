package com.vimes.domain.port

import com.vimes.domain.model.*
import java.time.LocalDate
import java.util.UUID

interface GoodsReceiptRepositoryPort {
    fun save(receipt: GoodsReceipt): GoodsReceipt
    fun findById(id: UUID): GoodsReceipt?
    fun findByReceiptNumber(receiptNumber: String): GoodsReceipt?
    fun deleteById(id: UUID): Boolean
    fun findAll(
        search: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        warehouseId: UUID?,
        status: ReceiptStatus?
    ): List<GoodsReceipt>
}

interface InventoryBalanceRepositoryPort {
    fun adjustBalance(warehouseId: UUID, productId: UUID, deltaQuantity: Double): Double
    fun getBalance(warehouseId: UUID, productId: UUID): Double
}

interface MasterDataRepositoryPort {
    fun getProducts(search: String?): List<Product>
    fun getProductById(id: UUID): Product?
    fun getWarehouses(search: String? = null): List<Warehouse>
    fun getWarehouseById(id: UUID): Warehouse?
    fun getOrganizations(search: String? = null): List<Organization>
    fun getOrganizationById(id: UUID): Organization?
}
