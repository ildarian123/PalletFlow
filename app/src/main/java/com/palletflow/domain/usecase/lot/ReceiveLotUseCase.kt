package com.palletflow.domain.usecase.lot

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotStatus
import com.palletflow.domain.repository.LotRepository
import java.util.*
import javax.inject.Inject

class ReceiveLotUseCase @Inject constructor(
    private val lotRepository: LotRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        lotNumber: String,
        supplierId: String,
        warehouseId: String,
        purchasePrice: Double,
        notes: String?,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>,
        lotId: String? = null
    ): Result<Lot> {
        val userId = authPreferences.getUserId()
        if (userId == null) {
            android.util.Log.e("ReceiveLotUseCase", "User ID is null! User not logged in.")
            return Result.failure(Exception("User not logged in"))
        }

        // Validate
        if (lotNumber.isBlank()) {
            return Result.failure(Exception("Lot number is required"))
        }
        if (supplierId.isBlank()) {
            return Result.failure(Exception("Supplier is required"))
        }
        if (warehouseId.isBlank()) {
            return Result.failure(Exception("Warehouse is required"))
        }
        if (items.isEmpty()) {
            return Result.failure(Exception("At least one item is required"))
        }
        if (purchasePrice <= 0) {
            // Log but don't fail for test if needed? No, let's keep it strict but ensured by VM
            return Result.failure(Exception("Purchase price must be greater than 0"))
        }

        val lot = Lot(
            id = lotId ?: UUID.randomUUID().toString(),
            userId = userId,
            lotNumber = lotNumber,
            supplierId = supplierId,
            warehouseId = warehouseId,
            purchasePrice = purchasePrice,
            status = LotStatus.COMPLETED,
            notes = notes?.trim()
        )

        android.util.Log.d("ReceiveLotUseCase", "Proceeding to repository with lot: ${lot.lotNumber}, userId: $userId, isUpdate: ${lotId != null}")
        
        return if (lotId != null) {
            lotRepository.updateLotWithItems(lot, items, expenses)
        } else {
            lotRepository.createLotWithItems(lot, items, expenses)
        }
    }

}

data class ItemToReceive(
    val itemId: String,
    val quantity: Int
)

data class ExpenseToAdd(
    val name: String,
    val amount: Double
)
