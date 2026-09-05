package com.vimes.service

import com.vimes.domain.model.*
import com.vimes.domain.port.GoodsReceiptRepositoryPort
import com.vimes.domain.port.InventoryBalanceRepositoryPort
import com.vimes.domain.port.MasterDataRepositoryPort
import com.vimes.dto.*
import com.vimes.exception.DuplicateReceiptNumberException
import com.vimes.exception.InvalidReceiptStatusException
import com.vimes.exception.ResourceNotFoundException
import com.vimes.util.RequestContext
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID
import kotlin.math.ceil

@Service
class GoodsReceiptService(
    private val goodsReceiptRepository: GoodsReceiptRepositoryPort,
    private val masterDataRepository: MasterDataRepositoryPort,
    private val inventoryBalanceRepository: InventoryBalanceRepositoryPort
) {

    fun createGoodsReceipt(request: CreateGoodsReceiptRequest): CreateGoodsReceiptSuccessResponse {
        val existing = goodsReceiptRepository.findByReceiptNumber(request.receiptNumber)
        if (existing != null) {
            throw DuplicateReceiptNumberException("Số phiếu ${request.receiptNumber} đã tồn tại trên hệ thống.")
        }

        val mappedItems = request.items.mapIndexed { index, itemInput ->
            ReceiptItem(
                id = UUID.randomUUID(),
                lineNo = index + 1,
                productId = itemInput.productId,
                productNameSnapshot = itemInput.productNameSnapshot,
                unitSnapshot = itemInput.unitSnapshot,
                docQty = itemInput.docQty,
                actualQty = itemInput.actualQty,
                unitPrice = itemInput.unitPrice,
                amount = itemInput.actualQty * itemInput.unitPrice,
                debitAccount = itemInput.debitAccount,
                creditAccount = itemInput.creditAccount,
                note = itemInput.note
            )
        }

        val totalAmount = mappedItems.sumOf { it.amount }

        val receipt = GoodsReceipt(
            id = UUID.randomUUID(),
            receiptNumber = request.receiptNumber,
            receiptDate = request.receiptDate,
            actualReceivedDate = request.actualReceivedDate,
            organizationId = request.organizationId,
            warehouseId = request.warehouseId,
            receiptType = request.receiptType,
            description = request.description,
            delivererName = request.delivererName,
            docReference = request.docReference,
            docDate = request.docDate,
            docOrigin = request.docOrigin,
            debitAccount = request.debitAccount,
            creditAccount = request.creditAccount,
            totalAmount = totalAmount,
            totalAmountWords = request.totalAmountWords,
            attachedDocCount = request.attachedDocCount,
            creatorName = request.creatorName,
            storekeeperName = request.storekeeperName,
            chiefAccountantName = request.chiefAccountantName,
            status = request.status,
            items = mappedItems
        )

        goodsReceiptRepository.save(receipt)

        if (receipt.status == ReceiptStatus.CONFIRMED) {
            receipt.items.forEach { item ->
                inventoryBalanceRepository.adjustBalance(receipt.warehouseId, item.productId, item.actualQty)
            }
        }

        return CreateGoodsReceiptSuccessResponse(
            success = true,
            message = "Lập phiếu nhập kho thành công (Mẫu 01 - VT)",
            requestId = RequestContext.getRequestId(),
            data = CreateReceiptData(
                receiptId = receipt.id,
                totalAmount = totalAmount
            )
        )
    }

    fun getGoodsReceipts(
        search: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        warehouseId: UUID?,
        status: ReceiptStatus?,
        page: Int = 1,
        limit: Int = 20
    ): GoodsReceiptListResponse {
        val safePage = if (page < 1) 1 else page
        val safeLimit = if (limit < 1) 20 else if (limit > 100) 100 else limit

        val allMatching = goodsReceiptRepository.findAll(search, fromDate, toDate, warehouseId, status)
        val totalItems = allMatching.size
        val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / safeLimit).toInt()

        val fromIndex = (safePage - 1) * safeLimit
        val sliced = if (fromIndex >= totalItems) {
            emptyList()
        } else {
            val toIndex = minOf(fromIndex + safeLimit, totalItems)
            allMatching.subList(fromIndex, toIndex)
        }

        val listItems = sliced.map { receipt ->
            val warehouse = masterDataRepository.getWarehouseById(receipt.warehouseId)
            GoodsReceiptListItem(
                id = receipt.id,
                receiptNumber = receipt.receiptNumber,
                receiptDate = receipt.receiptDate,
                warehouseName = warehouse?.name ?: "",
                delivererName = receipt.delivererName,
                totalAmount = receipt.totalAmount,
                status = receipt.status.name
            )
        }

        return GoodsReceiptListResponse(
            success = true,
            requestId = RequestContext.getRequestId(),
            data = listItems,
            pagination = PaginationInfo(
                page = safePage,
                limit = safeLimit,
                totalItems = totalItems,
                totalPages = totalPages
            )
        )
    }

    fun getGoodsReceiptById(id: UUID): GoodsReceiptDetailResponse {
        val receipt = goodsReceiptRepository.findById(id)
            ?: throw ResourceNotFoundException("Không tìm thấy phiếu nhập kho với ID đã cung cấp.")

        val org = masterDataRepository.getOrganizationById(receipt.organizationId)
        val warehouse = masterDataRepository.getWarehouseById(receipt.warehouseId)

        val itemDetails = receipt.items.map { item ->
            val product = masterDataRepository.getProductById(item.productId)
            ReceiptItemDetailData(
                id = item.id,
                lineNo = item.lineNo,
                productId = item.productId,
                productCode = product?.code,
                productName = item.productNameSnapshot,
                unit = item.unitSnapshot,
                docQty = item.docQty,
                actualQty = item.actualQty,
                unitPrice = item.unitPrice,
                amount = item.amount,
                debitAccount = item.debitAccount,
                creditAccount = item.creditAccount,
                note = item.note
            )
        }

        val detailData = GoodsReceiptDetailData(
            id = receipt.id,
            receiptNumber = receipt.receiptNumber,
            receiptDate = receipt.receiptDate,
            actualReceivedDate = receipt.actualReceivedDate,
            receiptType = receipt.receiptType.name,
            description = receipt.description,
            organization = OrganizationSummary(
                id = receipt.organizationId,
                name = org?.name ?: "",
                department = org?.department
            ),
            warehouse = WarehouseSummary(
                id = receipt.warehouseId,
                name = warehouse?.name ?: "",
                location = warehouse?.location
            ),
            delivererName = receipt.delivererName,
            docReference = receipt.docReference,
            docDate = receipt.docDate,
            docOrigin = receipt.docOrigin,
            debitAccount = receipt.debitAccount,
            creditAccount = receipt.creditAccount,
            totalAmount = receipt.totalAmount,
            totalAmountWords = receipt.totalAmountWords,
            attachedDocCount = receipt.attachedDocCount,
            signatures = Signatures(
                creatorName = receipt.creatorName,
                storekeeperName = receipt.storekeeperName,
                chiefAccountantName = receipt.chiefAccountantName
            ),
            status = receipt.status.name,
            items = itemDetails
        )

        return GoodsReceiptDetailResponse(
            success = true,
            requestId = RequestContext.getRequestId(),
            data = detailData
        )
    }

    fun updateGoodsReceipt(id: UUID, request: UpdateGoodsReceiptRequest): UpdateGoodsReceiptSuccessResponse {
        val existing = goodsReceiptRepository.findById(id)
            ?: throw ResourceNotFoundException("Không tìm thấy phiếu nhập kho với ID đã cung cấp.")

        if (existing.status == ReceiptStatus.CANCELLED) {
            throw InvalidReceiptStatusException("Không thể chỉnh sửa phiếu nhập đã ở trạng thái CANCELLED.")
        }

        if (existing.status == ReceiptStatus.CONFIRMED) {
            // Reverse old stock
            existing.items.forEach { item ->
                inventoryBalanceRepository.adjustBalance(existing.warehouseId, item.productId, -item.actualQty)
            }
        }

        val updatedItems = request.items.mapIndexed { index, itemInput ->
            ReceiptItem(
                id = UUID.randomUUID(),
                lineNo = index + 1,
                productId = itemInput.productId,
                productNameSnapshot = itemInput.productNameSnapshot,
                unitSnapshot = itemInput.unitSnapshot,
                docQty = itemInput.docQty,
                actualQty = itemInput.actualQty,
                unitPrice = itemInput.unitPrice,
                amount = itemInput.actualQty * itemInput.unitPrice,
                debitAccount = itemInput.debitAccount,
                creditAccount = itemInput.creditAccount,
                note = itemInput.note
            )
        }

        val newTotalAmount = updatedItems.sumOf { it.amount }
        val targetStatus = request.status ?: existing.status

        val updatedReceipt = existing.copy(
            receiptDate = request.receiptDate ?: existing.receiptDate,
            actualReceivedDate = request.actualReceivedDate ?: existing.actualReceivedDate,
            organizationId = request.organizationId,
            warehouseId = request.warehouseId,
            receiptType = request.receiptType ?: existing.receiptType,
            description = request.description ?: existing.description,
            delivererName = request.delivererName,
            docReference = request.docReference ?: existing.docReference,
            docDate = request.docDate ?: existing.docDate,
            docOrigin = request.docOrigin ?: existing.docOrigin,
            debitAccount = request.debitAccount ?: existing.debitAccount,
            creditAccount = request.creditAccount ?: existing.creditAccount,
            totalAmount = newTotalAmount,
            totalAmountWords = request.totalAmountWords ?: existing.totalAmountWords,
            attachedDocCount = request.attachedDocCount ?: existing.attachedDocCount,
            creatorName = request.creatorName ?: existing.creatorName,
            storekeeperName = request.storekeeperName ?: existing.storekeeperName,
            chiefAccountantName = request.chiefAccountantName ?: existing.chiefAccountantName,
            status = targetStatus,
            items = updatedItems
        )

        goodsReceiptRepository.save(updatedReceipt)

        if (targetStatus == ReceiptStatus.CONFIRMED) {
            updatedReceipt.items.forEach { item ->
                inventoryBalanceRepository.adjustBalance(updatedReceipt.warehouseId, item.productId, item.actualQty)
            }
        }

        return UpdateGoodsReceiptSuccessResponse(
            success = true,
            message = "Cập nhật phiếu nhập kho và điều chỉnh tồn kho thành công",
            requestId = RequestContext.getRequestId(),
            data = UpdateReceiptData(
                receiptId = id,
                totalAmount = newTotalAmount
            )
        )
    }

    fun deleteOrCancelGoodsReceipt(id: UUID): DeleteGoodsReceiptSuccessResponse {
        val existing = goodsReceiptRepository.findById(id)
            ?: throw ResourceNotFoundException("Không tìm thấy phiếu nhập kho với ID đã cung cấp.")

        val action = existing.cancel()

        return when (action) {
            DeleteAction.HARD_DELETED -> {
                goodsReceiptRepository.deleteById(id)
                DeleteGoodsReceiptSuccessResponse(
                    success = true,
                    message = "Đã xóa phiếu nhập kho nháp thành công",
                    requestId = RequestContext.getRequestId(),
                    data = DeleteReceiptData(
                        receiptId = id,
                        action = DeleteAction.HARD_DELETED
                    )
                )
            }
            DeleteAction.CANCELLED_AND_REVERSED -> {
                // Reverse inventory
                existing.items.forEach { item ->
                    inventoryBalanceRepository.adjustBalance(existing.warehouseId, item.productId, -item.actualQty)
                }
                goodsReceiptRepository.save(existing)

                DeleteGoodsReceiptSuccessResponse(
                    success = true,
                    message = "Đã hủy chứng từ nhập kho và hoàn trả tồn kho thành công (Stock Reversal)",
                    requestId = RequestContext.getRequestId(),
                    data = DeleteReceiptData(
                        receiptId = id,
                        action = DeleteAction.CANCELLED_AND_REVERSED
                    )
                )
            }
        }
    }
}
