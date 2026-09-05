package com.vimes.dto

import com.vimes.domain.model.DeleteAction
import com.vimes.domain.model.ReceiptStatus
import com.vimes.domain.model.ReceiptType
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

// Request DTOs

data class ReceiptItemInput(
    @field:NotNull(message = "productId không được để trống")
    val productId: UUID,

    @field:NotBlank(message = "productNameSnapshot không được để trống")
    val productNameSnapshot: String,

    @field:NotBlank(message = "unitSnapshot không được để trống")
    val unitSnapshot: String,

    @field:NotNull(message = "docQty không được để trống")
    @field:Min(value = 0, message = "Số lượng theo chứng từ không được âm")
    val docQty: Double,

    @field:NotNull(message = "actualQty không được để trống")
    @field:Min(value = 0, message = "Số lượng thực nhập không được âm")
    val actualQty: Double,

    @field:NotNull(message = "unitPrice không được để trống")
    @field:Min(value = 0, message = "Đơn giá không được âm")
    val unitPrice: Double,

    val debitAccount: String? = null,
    val creditAccount: String? = null,
    val note: String? = null
)

data class CreateGoodsReceiptRequest(
    @field:NotBlank(message = "Số phiếu nhập không được để trống")
    val receiptNumber: String,

    @field:NotNull(message = "Ngày lập phiếu không được để trống")
    val receiptDate: LocalDate,

    val actualReceivedDate: LocalDate? = null,

    @field:NotNull(message = "organizationId không được để trống")
    val organizationId: UUID,

    @field:NotNull(message = "warehouseId không được để trống")
    val warehouseId: UUID,

    val receiptType: ReceiptType = ReceiptType.PURCHASE,
    val description: String? = null,

    @field:NotBlank(message = "Tên người giao hàng không được để trống")
    val delivererName: String,

    val docReference: String? = null,
    val docDate: LocalDate? = null,
    val docOrigin: String? = null,
    val debitAccount: String? = null,
    val creditAccount: String? = null,
    val totalAmountWords: String? = null,
    val attachedDocCount: String? = null,
    val creatorName: String? = null,
    val storekeeperName: String? = null,
    val chiefAccountantName: String? = null,
    val status: ReceiptStatus = ReceiptStatus.CONFIRMED,

    @field:NotEmpty(message = "Danh sách mặt hàng không được để trống")
    @field:Valid
    val items: List<ReceiptItemInput>
)

data class UpdateGoodsReceiptRequest(
    val receiptDate: LocalDate? = null,
    val actualReceivedDate: LocalDate? = null,

    @field:NotNull(message = "organizationId không được để trống")
    val organizationId: UUID,

    @field:NotNull(message = "warehouseId không được để trống")
    val warehouseId: UUID,

    val receiptType: ReceiptType? = null,
    val description: String? = null,

    @field:NotBlank(message = "Tên người giao hàng không được để trống")
    val delivererName: String,

    val docReference: String? = null,
    val docDate: LocalDate? = null,
    val docOrigin: String? = null,
    val debitAccount: String? = null,
    val creditAccount: String? = null,
    val totalAmountWords: String? = null,
    val attachedDocCount: String? = null,
    val creatorName: String? = null,
    val storekeeperName: String? = null,
    val chiefAccountantName: String? = null,
    val status: ReceiptStatus? = null,

    @field:NotEmpty(message = "Danh sách mặt hàng không được để trống")
    @field:Valid
    val items: List<ReceiptItemInput>
)

// Response DTOs

data class CreateReceiptData(
    val receiptId: UUID,
    val totalAmount: Double
)

data class CreateGoodsReceiptSuccessResponse(
    val success: Boolean = true,
    val message: String = "Lập phiếu nhập kho thành công (Mẫu 01 - VT)",
    val requestId: String,
    val data: CreateReceiptData
)

data class UpdateReceiptData(
    val receiptId: UUID,
    val totalAmount: Double
)

data class UpdateGoodsReceiptSuccessResponse(
    val success: Boolean = true,
    val message: String = "Cập nhật phiếu nhập kho và điều chỉnh tồn kho thành công",
    val requestId: String,
    val data: UpdateReceiptData
)

data class DeleteReceiptData(
    val receiptId: UUID,
    val action: DeleteAction
)

data class DeleteGoodsReceiptSuccessResponse(
    val success: Boolean = true,
    val message: String,
    val requestId: String,
    val data: DeleteReceiptData
)

data class OrganizationSummary(
    val id: UUID,
    val name: String,
    val department: String?
)

data class WarehouseSummary(
    val id: UUID,
    val name: String,
    val location: String?
)

data class Signatures(
    val creatorName: String?,
    val storekeeperName: String?,
    val chiefAccountantName: String?
)

data class ReceiptItemDetailData(
    val id: UUID,
    val lineNo: Int,
    val productId: UUID,
    val productCode: String?,
    val productName: String,
    val unit: String,
    val docQty: Double,
    val actualQty: Double,
    val unitPrice: Double,
    val amount: Double,
    val debitAccount: String?,
    val creditAccount: String?,
    val note: String?
)

data class GoodsReceiptDetailData(
    val id: UUID,
    val receiptNumber: String,
    val receiptDate: LocalDate,
    val actualReceivedDate: LocalDate?,
    val receiptType: String,
    val description: String?,
    val organization: OrganizationSummary,
    val warehouse: WarehouseSummary,
    val delivererName: String,
    val docReference: String?,
    val docDate: LocalDate?,
    val docOrigin: String?,
    val debitAccount: String?,
    val creditAccount: String?,
    val totalAmount: Double,
    val totalAmountWords: String?,
    val attachedDocCount: String?,
    val signatures: Signatures,
    val status: String,
    val items: List<ReceiptItemDetailData>
)

data class GoodsReceiptDetailResponse(
    val success: Boolean = true,
    val requestId: String,
    val data: GoodsReceiptDetailData
)

data class GoodsReceiptListItem(
    val id: UUID,
    val receiptNumber: String,
    val receiptDate: LocalDate,
    val warehouseName: String,
    val delivererName: String,
    val totalAmount: Double,
    val status: String
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val totalItems: Int,
    val totalPages: Int
)

data class GoodsReceiptListResponse(
    val success: Boolean = true,
    val requestId: String,
    val data: List<GoodsReceiptListItem>,
    val pagination: PaginationInfo
)

data class ProductData(
    val id: UUID,
    val code: String,
    val name: String,
    val unit: String,
    val defaultPrice: Double
)

data class ProductListResponse(
    val success: Boolean = true,
    val requestId: String,
    val data: List<ProductData>,
    val pagination: PaginationInfo? = null
)

data class WarehouseData(
    val id: UUID,
    val code: String,
    val name: String,
    val location: String
)

data class WarehouseListResponse(
    val success: Boolean = true,
    val requestId: String,
    val data: List<WarehouseData>,
    val pagination: PaginationInfo? = null
)

data class OrganizationData(
    val id: UUID,
    val code: String,
    val name: String,
    val department: String
)

data class OrganizationListResponse(
    val success: Boolean = true,
    val requestId: String,
    val data: List<OrganizationData>,
    val pagination: PaginationInfo? = null
)

data class FieldErrorDetail(
    val path: String,
    val message: String
)

data class ValidationErrorResponse(
    val success: Boolean = false,
    val message: String = "Lỗi xác thực dữ liệu đầu vào (Validation Error)",
    val requestId: String,
    val errors: List<FieldErrorDetail>
)

data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val requestId: String
)
