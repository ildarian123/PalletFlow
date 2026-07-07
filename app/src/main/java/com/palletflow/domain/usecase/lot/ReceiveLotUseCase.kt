package com.palletflow.domain.usecase.lot

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotStatus
import com.palletflow.domain.repository.LotRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReceiveLotUseCase @Inject constructor(
    private val lotRepository: LotRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        supplierId: String,
        warehouseId: String,
        purchasePrice: Double,
        notes: String?,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        // Validate
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
            return Result.failure(Exception("Purchase price must be greater than 0"))
        }

        val lot = Lot(
            userId = userId,
            lotNumber = generateLotNumber(),
            supplierId = supplierId,
            warehouseId = warehouseId,
            purchasePrice = purchasePrice,
            status = LotStatus.COMPLETED,
            notes = notes?.trim()
        )

        return lotRepository.createLotWithItems(lot, items, expenses)
    }

    private fun generateLotNumber(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "LOT-$date-$random"
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
