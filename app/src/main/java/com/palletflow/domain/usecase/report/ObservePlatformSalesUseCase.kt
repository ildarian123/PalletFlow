package com.palletflow.domain.usecase.report

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ObservePlatformSalesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val lotRepository: LotRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(
        startDate: Long,
        endDate: Long,
        warehouseId: String? = null
    ): Flow<List<PlatformSalesData>> {
        val userId = authPreferences.getUserId() ?: return flowOf(emptyList())

        return combine(
            transactionRepository.getTransactionsByDateRange(userId, startDate, endDate),
            transactionRepository.getAllSaleItemsFlow(),
            lotRepository.getAllLots(userId),
            transactionRepository.getTransactionsByType(userId, TransactionType.INCOMING)
        ) { transactions, allSaleItems, lots, incomingTransactions ->
            val salesTransactions = transactions.filter { it.type == TransactionType.SALE }
            
            val filteredSales = if (warehouseId != null) {
                salesTransactions.filter { it.warehouseFromId == warehouseId || it.warehouseToId == warehouseId }
            } else {
                salesTransactions
            }

            // Create a lookup for lot unit costs
            val lotCostMap = lots.associate { lot ->
                val incoming = incomingTransactions.find { it.lotId == lot.id }
                val totalCost = incoming?.amount ?: lot.purchasePrice
                val totalQty = incoming?.quantity ?: 1
                lot.id to (totalCost / if (totalQty > 0) totalQty else 1)
            }

            filteredSales.groupBy { it.platform ?: "Direct" }
                .map { (platform, txs) ->
                    val totalRevenue = txs.sumOf { it.amount }
                    val saleIds = txs.map { it.id }.toSet()
                    val items = allSaleItems.filter { it.transactionId in saleIds }
                    val totalQuantity = items.sumOf { it.quantity }
                    
                    val totalCost = items.sumOf { item ->
                        val unitCost = lotCostMap[item.lotId] ?: 0.0
                        unitCost * item.quantity
                    }
                    
                    PlatformSalesData(
                        platform = platform,
                        revenue = totalRevenue,
                        profit = totalRevenue - totalCost,
                        salesCount = totalQuantity,
                        avgPrice = if (totalQuantity > 0) totalRevenue / totalQuantity else 0.0
                    )
                }.sortedByDescending { it.revenue }
        }
    }
}
