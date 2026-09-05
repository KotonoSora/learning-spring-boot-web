package com.vimes.repository

import com.vimes.domain.port.InventoryBalanceRepositoryPort
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class InventoryBalanceRepository : InventoryBalanceRepositoryPort {

    private val balances = ConcurrentHashMap<Pair<UUID, UUID>, Double>()

    @Synchronized
    override fun adjustBalance(warehouseId: UUID, productId: UUID, deltaQuantity: Double): Double {
        val key = Pair(warehouseId, productId)
        val current = balances.getOrDefault(key, 0.0)
        val updated = current + deltaQuantity
        balances[key] = updated
        return updated
    }

    override fun getBalance(warehouseId: UUID, productId: UUID): Double {
        return balances.getOrDefault(Pair(warehouseId, productId), 0.0)
    }
}
