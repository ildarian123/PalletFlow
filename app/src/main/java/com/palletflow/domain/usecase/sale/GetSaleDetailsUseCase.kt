package com.palletflow.domain.usecase.sale

import com.palletflow.data.local.entity.SaleExpense
import com.palletflow.data.local.entity.SaleItem
import com.palletflow.data.local.entity.Transaction
import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class GetSaleDetailsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String, userId: String): SaleDetails? {
        val transaction = transactionRepository.getTransactionById(transactionId, userId) ?: return null
        val items = transactionRepository.getSaleItems(transactionId)
        val expenses = transactionRepository.getSaleExpenses(transactionId)
        
        return SaleDetails(transaction, items, expenses)
    }
}

data class SaleDetails(
    val transaction: Transaction,
    val items: List<SaleItem>,
    val expenses: List<SaleExpense>
)
