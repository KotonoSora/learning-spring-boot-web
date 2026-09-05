package com.vimes.service

import com.vimes.dto.*
import com.vimes.repository.MasterDataRepository
import com.vimes.util.RequestContext
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class MasterDataService(
    private val masterDataRepository: MasterDataRepository
) {

    fun getProducts(search: String?, page: Int = 1, limit: Int = 20): ProductListResponse {
        val allProducts = masterDataRepository.getProducts(search).map { p ->
            ProductData(
                id = p.id,
                code = p.code,
                name = p.name,
                unit = p.unit,
                defaultPrice = p.defaultPrice
            )
        }

        val safePage = if (page < 1) 1 else page
        val safeLimit = if (limit < 1) 20 else if (limit > 100) 100 else limit
        val totalItems = allProducts.size
        val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / safeLimit).toInt()

        val fromIndex = (safePage - 1) * safeLimit
        val sliced = if (fromIndex >= totalItems) {
            emptyList()
        } else {
            val toIndex = minOf(fromIndex + safeLimit, totalItems)
            allProducts.subList(fromIndex, toIndex)
        }

        return ProductListResponse(
            success = true,
            requestId = RequestContext.getRequestId(),
            data = sliced,
            pagination = PaginationInfo(
                page = safePage,
                limit = safeLimit,
                totalItems = totalItems,
                totalPages = totalPages
            )
        )
    }

    fun getWarehouses(search: String? = null, page: Int = 1, limit: Int = 20): WarehouseListResponse {
        val allWarehouses = masterDataRepository.getWarehouses(search).map { w ->
            WarehouseData(
                id = w.id,
                code = w.code,
                name = w.name,
                location = w.location
            )
        }

        val safePage = if (page < 1) 1 else page
        val safeLimit = if (limit < 1) 20 else if (limit > 100) 100 else limit
        val totalItems = allWarehouses.size
        val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / safeLimit).toInt()

        val fromIndex = (safePage - 1) * safeLimit
        val sliced = if (fromIndex >= totalItems) {
            emptyList()
        } else {
            val toIndex = minOf(fromIndex + safeLimit, totalItems)
            allWarehouses.subList(fromIndex, toIndex)
        }

        return WarehouseListResponse(
            success = true,
            requestId = RequestContext.getRequestId(),
            data = sliced,
            pagination = PaginationInfo(
                page = safePage,
                limit = safeLimit,
                totalItems = totalItems,
                totalPages = totalPages
            )
        )
    }

    fun getOrganizations(search: String? = null, page: Int = 1, limit: Int = 20): OrganizationListResponse {
        val allOrgs = masterDataRepository.getOrganizations(search).map { o ->
            OrganizationData(
                id = o.id,
                code = o.code,
                name = o.name,
                department = o.department
            )
        }

        val safePage = if (page < 1) 1 else page
        val safeLimit = if (limit < 1) 20 else if (limit > 100) 100 else limit
        val totalItems = allOrgs.size
        val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / safeLimit).toInt()

        val fromIndex = (safePage - 1) * safeLimit
        val sliced = if (fromIndex >= totalItems) {
            emptyList()
        } else {
            val toIndex = minOf(fromIndex + safeLimit, totalItems)
            allOrgs.subList(fromIndex, toIndex)
        }

        return OrganizationListResponse(
            success = true,
            requestId = RequestContext.getRequestId(),
            data = sliced,
            pagination = PaginationInfo(
                page = safePage,
                limit = safeLimit,
                totalItems = totalItems,
                totalPages = totalPages
            )
        )
    }
}
