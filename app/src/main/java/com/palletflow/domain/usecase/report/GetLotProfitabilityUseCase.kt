package com.palletflow.domain.usecase.report

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetLotProfitabilityUseCase @Inject constructor(
    private val lotRepository: LotRepository,
    private val transactionRepository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        startDate: Long,
        endDate: Long,
        warehouseId: String? = null
    ): Result<List<LotProfitData>> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val lots = lotRepository.getAllLots(userId).first()
            
            // Get all incoming transactions to get the REAL cost of each lot
            val incomingTransactions = transactionRepository.getTransactionsByType(userId, TransactionType.INCOMING).first()
            
            // Get sale items for the selected period
            val saleItemsInRange = transactionRepository.getSaleItemsByDateRange(userId, startDate, endDate)
            
            val filteredLots = if (warehouseId != null) {
                lots.filter { it.warehouseId == warehouseId }
            } else {
                lots
            }

            val lotProfits = filteredLots.map { lot ->
                val incomingTransaction = incomingTransactions.find { it.lotId == lot.id }
                val salesForLot = saleItemsInRange.filter { it.lotId == lot.id }
                
                val totalLotCost = incomingTransaction?.amount ?: lot.purchasePrice
                val totalLotUnits = incomingTransaction?.quantity ?: 1 // Avoid division by zero
                val costPerUnit = totalLotCost / if (totalLotUnits > 0) totalLotUnits else 1
                
                val soldQuantity = salesForLot.sumOf { it.quantity }
                val revenue = salesForLot.sumOf { it.sellingPrice * it.quantity }
                
                // COGS (Cost of Goods Sold) = Cost of the specific units that were sold
                val cogs = costPerUnit * soldQuantity
                val profit = revenue - cogs
                
                val roi = if (cogs > 0) (profit / cogs * 100) else 0.0

                LotProfitData(
                    lotNumber = lot.lotNumber,
                    date = lot.createdAt,
                    cost = totalLotCost, // Still show total lot cost for reference
                    revenue = revenue,
                    profit = profit,
                    roi = roi,
                    status = lot.status.name
                )
            }

            Result.success(lotProfits.sortedByDescending { it.date })
        } catch (e: Exception) {
            Log.e("GetLotProfitabilityUseCase", "Failed to get lot profits", e)
            Result.failure(e)
        }
    }
}

data class LotProfitData(
    val lotNumber: String,
    val date: Long,
    val cost: Double,
    val revenue: Double,
    val profit: Double,
    val roi: Double,
    val status: String
)
