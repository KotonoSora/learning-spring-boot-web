package com.vimes.repository

import com.vimes.domain.model.GoodsReceipt
import com.vimes.domain.model.ReceiptItem
import com.vimes.domain.model.ReceiptStatus
import com.vimes.domain.model.ReceiptType
import com.vimes.domain.port.GoodsReceiptRepositoryPort
import com.vimes.domain.port.MasterDataRepositoryPort
import org.springframework.stereotype.Repository
import java.text.Normalizer
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Repository
class GoodsReceiptRepository(
    private val masterDataRepository: MasterDataRepositoryPort
) : GoodsReceiptRepositoryPort {

    private val receipts = ConcurrentHashMap<UUID, GoodsReceipt>()

    init {
        seedGoodsReceipts()
    }

    private fun seedGoodsReceipts() {
        val products = masterDataRepository.getProducts(null)
        val warehouses = masterDataRepository.getWarehouses()
        val orgs = masterDataRepository.getOrganizations()

        val deliverers = listOf(
            "Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Quốc D", "Hoàng Kim E",
            "Vũ Đình F", "Đỗ Văn G", "Ngô Thị H", "Bùi Văn I", "Đặng Văn K"
        )

        val creators = listOf("Lê Văn Lập", "Nguyễn Thị Hương", "Phạm Minh Tuấn", "Trần Đức Nam")
        val storekeepers = listOf("Trần Văn Kho", "Vũ Văn Khoa", "Đỗ Hữu Thủ")
        val accountants = listOf("Phạm Thị Trưởng", "Hoàng Thị Kế", "Bùi Tấn Toán")

        val receiptTypes = ReceiptType.entries.toTypedArray()
        val statuses = listOf(
            ReceiptStatus.CONFIRMED, ReceiptStatus.CONFIRMED, ReceiptStatus.CONFIRMED,
            ReceiptStatus.DRAFT, ReceiptStatus.CANCELLED
        )

        val baseDate = LocalDate.of(2026, 8, 30)

        for (i in 1..65) {
            val receiptNumStr = String.format("%04d", i)
            val receiptNumber = "PNK-2026-$receiptNumStr"
            val receiptDate = baseDate.minusDays(((i - 1) * 2).toLong())
            val actualDate = receiptDate.plusDays(1)

            val org = orgs[(i - 1) % orgs.size]
            val warehouse = warehouses[(i - 1) % warehouses.size]
            val deliverer = deliverers[(i - 1) % deliverers.size]
            val receiptType = receiptTypes[(i - 1) % receiptTypes.size]
            val status = statuses[(i - 1) % statuses.size]

            val p1 = products[(i - 1) % products.size]
            val p2 = products[(i * 3) % products.size]

            val qty1 = 50.0 + (i * 2)
            val qty2 = 100.0 + (i * 5)

            val item1 = ReceiptItem(
                id = UUID.nameUUIDFromBytes("$receiptNumber-1".toByteArray()),
                lineNo = 1,
                productId = p1.id,
                productNameSnapshot = p1.name,
                unitSnapshot = p1.unit,
                docQty = qty1,
                actualQty = qty1 - (if (i % 5 == 0) 1.5 else 0.0),
                unitPrice = p1.defaultPrice,
                amount = (qty1 - (if (i % 5 == 0) 1.5 else 0.0)) * p1.defaultPrice,
                debitAccount = "152",
                creditAccount = "331",
                note = if (i % 5 == 0) "Hao hụt vận chuyển" else null
            )

            val item2 = ReceiptItem(
                id = UUID.nameUUIDFromBytes("$receiptNumber-2".toByteArray()),
                lineNo = 2,
                productId = p2.id,
                productNameSnapshot = p2.name,
                unitSnapshot = p2.unit,
                docQty = qty2,
                actualQty = qty2,
                unitPrice = p2.defaultPrice,
                amount = qty2 * p2.defaultPrice,
                debitAccount = "152",
                creditAccount = "331"
            )

            val itemList = if (i % 2 == 0) listOf(item1, item2) else listOf(item1)
            val totalAmt = itemList.sumOf { it.amount }

            val fixedId = if (i == 1) UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
            else UUID.nameUUIDFromBytes(receiptNumber.toByteArray())

            val receipt = GoodsReceipt(
                id = fixedId,
                receiptNumber = if (i == 1) "PNK-2026-0001" else receiptNumber,
                receiptDate = receiptDate,
                actualReceivedDate = actualDate,
                organizationId = org.id,
                warehouseId = warehouse.id,
                receiptType = receiptType,
                description = "Nhập kho ${p1.name} phục vụ dự án công trình VIMES - Đợt $i",
                delivererName = deliverer,
                docReference = "HĐ-$i$i$i",
                docDate = receiptDate.minusDays(1),
                docOrigin = "Công ty TNHH Cung ứng Vật tư $i",
                debitAccount = "152",
                creditAccount = "331",
                totalAmount = totalAmt,
                totalAmountWords = null,
                attachedDocCount = "1 hóa đơn GTGT gốc",
                creatorName = creators[(i - 1) % creators.size],
                storekeeperName = storekeepers[(i - 1) % storekeepers.size],
                chiefAccountantName = accountants[(i - 1) % accountants.size],
                status = status,
                items = itemList
            )

            receipts[receipt.id] = receipt
        }
    }

    override fun save(receipt: GoodsReceipt): GoodsReceipt {
        receipts[receipt.id] = receipt
        return receipt
    }

    override fun findById(id: UUID): GoodsReceipt? = receipts[id]

    override fun findByReceiptNumber(receiptNumber: String): GoodsReceipt? {
        return receipts.values.firstOrNull { it.receiptNumber.equals(receiptNumber, ignoreCase = true) }
    }

    override fun deleteById(id: UUID): Boolean {
        return receipts.remove(id) != null
    }

    override fun findAll(
        search: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        warehouseId: UUID?,
        status: ReceiptStatus?
    ): List<GoodsReceipt> {
        return receipts.values.filter { receipt ->
            var matches = true

            if (!search.isNullOrBlank()) {
                val matchNumber = receipt.receiptNumber.matchesFuzzy(search)
                val matchDeliverer = receipt.delivererName.matchesFuzzy(search)
                val matchDescription = receipt.description?.matchesFuzzy(search) ?: false
                val matchDocRef = receipt.docReference?.matchesFuzzy(search) ?: false
                val matchDocOrigin = receipt.docOrigin?.matchesFuzzy(search) ?: false
                val matchCreator = receipt.creatorName?.matchesFuzzy(search) ?: false
                val matchItemName = receipt.items.any { it.productNameSnapshot.matchesFuzzy(search) }

                if (!matchNumber && !matchDeliverer && !matchDescription && !matchDocRef && !matchDocOrigin && !matchCreator && !matchItemName) {
                    matches = false
                }
            }

            if (matches && fromDate != null) {
                if (receipt.receiptDate.isBefore(fromDate)) {
                    matches = false
                }
            }

            if (matches && toDate != null) {
                if (receipt.receiptDate.isAfter(toDate)) {
                    matches = false
                }
            }

            if (matches && warehouseId != null) {
                if (receipt.warehouseId != warehouseId) {
                    matches = false
                }
            }

            if (matches && status != null) {
                if (receipt.status != status) {
                    matches = false
                }
            }

            matches
        }.sortedByDescending { it.receiptDate }
    }

    private fun String.matchesFuzzy(searchTerm: String?): Boolean {
        if (searchTerm.isNullOrBlank()) return true
        val normalizedTarget = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(DIACRITICS_PATTERN.toRegex(), "")
            .replace('đ', 'd').replace('Đ', 'D')
            .lowercase()
        val normalizedSearch = Normalizer.normalize(searchTerm, Normalizer.Form.NFD)
            .replace(DIACRITICS_PATTERN.toRegex(), "")
            .replace('đ', 'd').replace('Đ', 'D')
            .lowercase()
        return normalizedTarget.contains(normalizedSearch)
    }

    companion object {
        private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    }
}
