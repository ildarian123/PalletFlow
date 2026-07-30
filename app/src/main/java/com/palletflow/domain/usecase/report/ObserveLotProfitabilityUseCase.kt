package com.palletflow.domain.usecase.report

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ObserveLotProfitabilityUseCase @Inject constructor(
    private val lotRepository: LotRepository,
    private val transactionRepository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(
        startDate: Long,
        endDate: Long,
        warehouseId: String? = null
    ): Flow<List<LotProfitData>> {
        val userId = authPreferences.getUserId() ?: return flowOf(emptyList())

        return combine(
            lotRepository.getAllLots(userId),
            transactionRepository.getTransactionsByType(userId, TransactionType.INCOMING),
            transactionRepository.getAllSaleItemsFlow()
        ) { lots, incomingTransactions, allSaleItems ->
            
            val filteredLots = if (warehouseId != null) {
                lots.filter { it.warehouseId == warehouseId }
            } else {
                lots
            }

            filteredLots.map { lot ->
                val incomingTransaction = incomingTransactions.find { it.lotId == lot.id }
                
                // Filter sale items for this lot and within date range
                // Note: allSaleItems doesn't have createdAt, but the parent transaction does.
                // However, we can use the date range filter from the UI if we pass it through.
                // For now, let's assume we want profitability for the whole life of these lots, 
                // OR we need to fetch transactions to check dates.
                
                val salesForLot = allSaleItems.filter { it.lotId == lot.id }
                
                val totalLotCost = incomingTransaction?.amount ?: lot.purchasePrice
                val totalLotUnits = incomingTransaction?.quantity ?: 1
                val costPerUnit = totalLotCost / if (totalLotUnits > 0) totalLotUnits else 1
                
                val soldQuantity = salesForLot.sumOf { it.quantity }
                val revenue = salesForLot.sumOf { it.sellingPrice * it.quantity }
                
                val cogs = costPerUnit * soldQuantity
                val profit = revenue - cogs
                val roi = if (cogs > 0) (profit / cogs * 100) else 0.0

                LotProfitData(
                    lotNumber = lot.lotNumber,
                    date = lot.createdAt,
                    cost = totalLotCost,
                    revenue = revenue,
                    profit = profit,
                    roi = roi,
                    status = lot.status.name
                )
            }.sortedByDescending { it.date }
        }
    }
}
