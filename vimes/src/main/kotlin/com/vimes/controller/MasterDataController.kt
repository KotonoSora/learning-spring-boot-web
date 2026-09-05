package com.vimes.controller

import com.vimes.dto.OrganizationListResponse
import com.vimes.dto.ProductListResponse
import com.vimes.dto.WarehouseListResponse
import com.vimes.service.MasterDataService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/master-data", produces = ["application/json;charset=UTF-8"])
class MasterDataController(
    private val masterDataService: MasterDataService
) {

    @GetMapping("/products")
    fun getProducts(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "20") limit: Int
    ): ResponseEntity<ProductListResponse> {
        val response = masterDataService.getProducts(search, page, limit)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/warehouses")
    fun getWarehouses(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "20") limit: Int
    ): ResponseEntity<WarehouseListResponse> {
        val response = masterDataService.getWarehouses(search, page, limit)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/organizations")
    fun getOrganizations(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "20") limit: Int
    ): ResponseEntity<OrganizationListResponse> {
        val response = masterDataService.getOrganizations(search, page, limit)
        return ResponseEntity.ok(response)
    }
}
