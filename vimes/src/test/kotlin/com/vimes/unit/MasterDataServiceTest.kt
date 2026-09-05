package com.vimes.unit

import com.vimes.repository.MasterDataRepository
import com.vimes.service.MasterDataService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MasterDataServiceTest {

    private lateinit var masterDataRepository: MasterDataRepository
    private lateinit var masterDataService: MasterDataService

    @BeforeEach
    fun setup() {
        masterDataRepository = MasterDataRepository()
        masterDataService = MasterDataService(masterDataRepository)
    }

    @Test
    fun `test getProducts returns pre-seeded products with search and pagination`() {
        // Default pagination (page 1, limit 20)
        val allProducts = masterDataService.getProducts(search = null)
        assertTrue(allProducts.success)
        assertEquals(20, allProducts.data.size)
        assertNotNull(allProducts.pagination)
        assertEquals(1, allProducts.pagination?.page)
        assertEquals(20, allProducts.pagination?.limit)

        // With search "thep" (unaccented Vietnamese fuzzy match)
        val searchResults = masterDataService.getProducts(search = "thep")
        assertTrue(searchResults.data.isNotEmpty())
        assertTrue(searchResults.data.all { p ->
            p.name.contains("Thép", ignoreCase = true) || p.code.contains("thep", ignoreCase = true)
        })

        // With pagination (page 1, limit 8)
        val page1 = masterDataService.getProducts(search = null, page = 1, limit = 8)
        assertEquals(8, page1.data.size)
        assertNotNull(page1.pagination)
        assertEquals(1, page1.pagination?.page)
        assertEquals(8, page1.pagination?.limit)
        assertTrue((page1.pagination?.totalPages ?: 0) >= 3)

        // Page 2
        val page2 = masterDataService.getProducts(search = null, page = 2, limit = 8)
        assertEquals(8, page2.data.size)
        assertEquals(2, page2.pagination?.page)
    }

    @Test
    fun `test getWarehouses and getOrganizations return master data`() {
        val warehouses = masterDataService.getWarehouses(search = "ha noi")
        assertTrue(warehouses.data.isNotEmpty())
        assertTrue(warehouses.data.any { it.name.contains("Hà Nội") })

        val orgs = masterDataService.getOrganizations(search = "vimes")
        assertTrue(orgs.data.isNotEmpty())
        assertTrue(orgs.data.any { it.name.contains("VIMES") })
    }
}
