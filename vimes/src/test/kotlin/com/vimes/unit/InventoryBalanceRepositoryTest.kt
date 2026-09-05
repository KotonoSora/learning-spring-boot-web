package com.vimes.unit

import com.vimes.repository.InventoryBalanceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InventoryBalanceRepositoryTest {

    private lateinit var repository: InventoryBalanceRepository

    @BeforeEach
    fun setup() {
        repository = InventoryBalanceRepository()
    }

    @Test
    fun `test adjustBalance increments and decrements balance accurately`() {
        val warehouseId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        assertEquals(0.0, repository.getBalance(warehouseId, productId))

        // Add 100
        val b1 = repository.adjustBalance(warehouseId, productId, 100.0)
        assertEquals(100.0, b1)
        assertEquals(100.0, repository.getBalance(warehouseId, productId))

        // Subtract 40
        val b2 = repository.adjustBalance(warehouseId, productId, -40.0)
        assertEquals(60.0, b2)
        assertEquals(60.0, repository.getBalance(warehouseId, productId))

        // Subtract 60
        val b3 = repository.adjustBalance(warehouseId, productId, -60.0)
        assertEquals(0.0, b3)
        assertEquals(0.0, repository.getBalance(warehouseId, productId))
    }
}
