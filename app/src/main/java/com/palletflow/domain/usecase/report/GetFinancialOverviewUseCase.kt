package com.palletflow.domain.usecase.report

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetFinancialOverviewUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        startDate: Long,
        endDate: Long,
        warehouseId: String? = null
    ): Result<FinancialOverview> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val transactions = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate).first()
            
            val filteredTransactions = if (warehouseId != null) {
                transactions.filter { it.warehouseFromId == warehouseId || it.warehouseToId == warehouseId }
            } else {
                transactions
            }

            val incomingTransactions = filteredTransactions.filter { it.type == TransactionType.INCOMING }
            val salesTransactions = filteredTransactions.filter { it.type == TransactionType.SALE }
            
            val totalIncoming = incomingTransactions.sumOf { it.amount }
            val totalRevenue = salesTransactions.sumOf { it.amount }
            val totalProfit = totalRevenue - totalIncoming
            val profitMargin = if (totalRevenue > 0) (totalProfit / totalRevenue * 100) else 0.0

            Result.success(
                FinancialOverview(
                    totalIncoming = totalIncoming,
                    totalRevenue = totalRevenue,
                    totalProfit = totalProfit,
                    profitMargin = profitMargin,
                    transactionCount = filteredTransactions.size
                )
            )
        } catch (e: Exception) {
            Log.e("GetFinancialOverviewUseCase", "Failed to get overview", e)
            Result.failure(e)
        }
    }
}

data class FinancialOverview(
    val totalIncoming: Double,
    val totalRevenue: Double,
    val totalProfit: Double,
    val profitMargin: Double,
    val transactionCount: Int
)
