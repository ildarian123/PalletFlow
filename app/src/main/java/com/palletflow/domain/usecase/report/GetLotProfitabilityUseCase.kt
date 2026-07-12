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
            val lots = lotRepository.getAllLots().first().filter { it.userId == userId }
            val transactions = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate).first()
            val allSaleItems = transactionRepository.getAllSaleItems()
            
            val filteredLots = if (warehouseId != null) {
                lots.filter { it.warehouseId == warehouseId }
            } else {
                lots
            }

            val lotProfits = filteredLots.map { lot ->
                val incomingTransaction = transactions.find { it.lotId == lot.id && it.type == TransactionType.INCOMING }
                val salesForLot = allSaleItems.filter { it.lotId == lot.id }
                
                val cost = incomingTransaction?.amount ?: 0.0
                val revenue = salesForLot.sumOf { it.sellingPrice * it.quantity }
                val profit = revenue - cost
                val roi = if (cost > 0) (profit / cost * 100) else 0.0

                LotProfitData(
                    lotNumber = lot.lotNumber,
                    date = lot.createdAt,
                    cost = cost,
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
