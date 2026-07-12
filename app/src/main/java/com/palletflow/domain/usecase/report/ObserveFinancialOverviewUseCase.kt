package com.palletflow.domain.usecase.report

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ObserveFinancialOverviewUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(
        startDate: Long,
        endDate: Long
    ): Flow<FinancialOverview> {
        val userId = authPreferences.getUserId() ?: return flowOf(FinancialOverview(0.0, 0.0, 0.0, 0.0, 0))

        return combine(
            transactionRepository.getTransactionsByDateRange(userId, startDate, endDate),
            transactionRepository.getAllSaleItemsFlow()
        ) { transactions, allSaleItems ->
            val incomingTransactions = transactions.filter { it.type == TransactionType.INCOMING }
            val salesTransactions = transactions.filter { it.type == TransactionType.SALE }
            
            val totalIncoming = incomingTransactions.sumOf { it.amount }
            val totalRevenue = salesTransactions.sumOf { it.amount }
            val totalProfit = totalRevenue - totalIncoming
            val profitMargin = if (totalRevenue > 0) (totalProfit / totalRevenue * 100) else 0.0
            
            // Count total items sold from sale items that belong to the sales transactions in range
            val saleIdsInRange = salesTransactions.map { it.id }.toSet()
            val totalItemsSold = allSaleItems
                .filter { it.transactionId in saleIdsInRange }
                .sumOf { it.quantity }

            FinancialOverview(
                totalIncoming = totalIncoming,
                totalRevenue = totalRevenue,
                totalProfit = totalProfit,
                profitMargin = profitMargin,
                transactionCount = totalItemsSold
            )
        }
    }
}
