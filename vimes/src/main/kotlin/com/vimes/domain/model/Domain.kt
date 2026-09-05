package com.vimes.domain.model

import java.time.LocalDate
import java.util.UUID

enum class ReceiptType {
    PURCHASE,
    INTERNAL_PRODUCTION,
    OUTSOURCED_PROCESSING,
    CAPITAL_CONTRIBUTION,
    INVENTORY_SURPLUS
}

enum class ReceiptStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED
}

enum class DeleteAction {
    HARD_DELETED,
    CANCELLED_AND_REVERSED
}

data class Product(
    val id: UUID,
    val code: String,
    val name: String,
    val unit: String,
    val defaultPrice: Double
)

data class Warehouse(
    val id: UUID,
    val code: String,
    val name: String,
    val location: String
)

data class Organization(
    val id: UUID,
    val code: String,
    val name: String,
    val department: String
)

data class ReceiptItem(
    val id: UUID = UUID.randomUUID(),
    var lineNo: Int,
    val productId: UUID,
    val productNameSnapshot: String,
    val unitSnapshot: String,
    val docQty: Double,
    val actualQty: Double,
    val unitPrice: Double,
    val amount: Double = actualQty * unitPrice,
    val debitAccount: String? = null,
    val creditAccount: String? = null,
    val note: String? = null
)

// Aggregate Root
data class GoodsReceipt(
    val id: UUID = UUID.randomUUID(),
    val receiptNumber: String,
    val receiptDate: LocalDate,
    val actualReceivedDate: LocalDate?,
    val organizationId: UUID,
    val warehouseId: UUID,
    val receiptType: ReceiptType,
    val description: String?,
    val delivererName: String,
    val docReference: String?,
    val docDate: LocalDate?,
    val docOrigin: String?,
    val debitAccount: String?,
    val creditAccount: String?,
    val totalAmount: Double,
    val totalAmountWords: String?,
    val attachedDocCount: String?,
    val creatorName: String?,
    val storekeeperName: String?,
    val chiefAccountantName: String?,
    var status: ReceiptStatus,
    val items: List<ReceiptItem>
) {
    fun cancel(): DeleteAction {
        return if (status == ReceiptStatus.DRAFT) {
            DeleteAction.HARD_DELETED
        } else {
            status = ReceiptStatus.CANCELLED
            DeleteAction.CANCELLED_AND_REVERSED
        }
    }
}

data class InventoryBalance(
    val warehouseId: UUID,
    val productId: UUID,
    var quantity: Double
)

// Domain Event
data class GoodsReceiptCreatedEvent(
    val receiptId: UUID,
    val receiptNumber: String,
    val totalAmount: Double
)
