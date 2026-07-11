package com.palletflow.domain.usecase.report

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPlatformSalesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val lotRepository: LotRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        startDate: Long,
        endDate: Long,
        warehouseId: String? = null
    ): Result<List<PlatformSalesData>> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val transactions = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate).first()
            
            val filteredTransactions = if (warehouseId != null) {
                transactions.filter { it.warehouseFromId == warehouseId || it.warehouseToId == warehouseId }
            } else {
                transactions
            }
            
            val salesTransactions = filteredTransactions.filter { it.type == TransactionType.SALE }
            
            // Map to store item costs by Lot ID
            val lotCostsCache = mutableMapOf<String, Map<String, Double>>()

            val platformData = salesTransactions
                .groupBy { it.platform ?: "Unknown" }
                .map { (platform, sales) ->
                    val count = sales.size
                    val revenue = sales.sumOf { it.amount }
                    val avgPrice = if (count > 0) revenue / count else 0.0
                    
                    var totalCost = 0.0
                    for (sale in sales) {
                        val saleItems = transactionRepository.getSaleItems(sale.id)
                        val saleExpenses = transactionRepository.getSaleExpenses(sale.id)
                        
                        var saleCost = saleExpenses.sumOf { it.amount }
                        
                        for (item in saleItems) {
                            // Find the cost of this item from its lot
                            // In this app, SaleItem currently doesn't store the lotId it was sold from if it's a mixed sale?
                            // Wait, SaleItem.kt has lotId!
                            val lotId = item.lotId
                            val itemCosts = lotCostsCache.getOrPut(lotId) {
                                lotRepository.getLotItemsSync(lotId).associate { it.itemId to (it.unitPrice ?: 0.0) }
                            }
                            val unitCost = itemCosts[item.itemId] ?: 0.0
                            saleCost += unitCost * item.quantity
                        }
                        totalCost += saleCost
                    }
                    
                    val profit = revenue - totalCost

                    PlatformSalesData(
                        platform = platform,
                        salesCount = count,
                        revenue = revenue,
                        avgPrice = avgPrice,
                        profit = profit
                    )
                }

            Result.success(platformData.sortedByDescending { it.revenue })
        } catch (e: Exception) {
            Log.e("GetPlatformSalesUseCase", "Failed to get platform sales", e)
            Result.failure(e)
        }
    }
}

data class PlatformSalesData(
    val platform: String,
    val salesCount: Int,
    val revenue: Double,
    val avgPrice: Double,
    val profit: Double
)
