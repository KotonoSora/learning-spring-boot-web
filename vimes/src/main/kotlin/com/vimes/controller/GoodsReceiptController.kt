package com.vimes.controller

import com.vimes.domain.model.ReceiptStatus
import com.vimes.dto.*
import com.vimes.service.GoodsReceiptService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/goods-receipts", produces = ["application/json;charset=UTF-8"])
class GoodsReceiptController(
    private val goodsReceiptService: GoodsReceiptService
) {

    @PostMapping
    fun createGoodsReceipt(
        @Valid @RequestBody request: CreateGoodsReceiptRequest
    ): ResponseEntity<CreateGoodsReceiptSuccessResponse> {
        val response = goodsReceiptService.createGoodsReceipt(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getGoodsReceipts(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @RequestParam(required = false) warehouseId: UUID?,
        @RequestParam(required = false) status: ReceiptStatus?,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "20") limit: Int
    ): ResponseEntity<GoodsReceiptListResponse> {
        val response = goodsReceiptService.getGoodsReceipts(
            search = search,
            fromDate = fromDate,
            toDate = toDate,
            warehouseId = warehouseId,
            status = status,
            page = page,
            limit = limit
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getGoodsReceiptById(
        @PathVariable id: UUID
    ): ResponseEntity<GoodsReceiptDetailResponse> {
        val response = goodsReceiptService.getGoodsReceiptById(id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateGoodsReceipt(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateGoodsReceiptRequest
    ): ResponseEntity<UpdateGoodsReceiptSuccessResponse> {
        val response = goodsReceiptService.updateGoodsReceipt(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteOrCancelGoodsReceipt(
        @PathVariable id: UUID
    ): ResponseEntity<DeleteGoodsReceiptSuccessResponse> {
        val response = goodsReceiptService.deleteOrCancelGoodsReceipt(id)
        return ResponseEntity.ok(response)
    }
}
