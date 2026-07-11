package com.palletflow.domain.usecase.transaction

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val lotRepository: LotRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> {
        val userId = authPreferences.getUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val transaction = transactionRepository.getTransactionById(transactionId, userId)
                ?: return Result.failure(Exception("Transaction not found"))

            when (transaction.type) {
                TransactionType.INCOMING -> {
                    if (transaction.lotId != null) {
                        lotRepository.deleteLot(transaction.lotId, userId)
                    } else {
                        transactionRepository.deleteTransaction(transactionId, userId)
                    }
                }
                TransactionType.SALE -> {
                    transactionRepository.deleteSale(transactionId, userId)
                }
                TransactionType.TRANSFER -> {
                    transactionRepository.deleteTransfer(transactionId, userId)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
