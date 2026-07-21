package com.palletflow.domain.usecase.lot

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.repository.LotRepository
import javax.inject.Inject

class DeleteLotUseCase @Inject constructor(
    private val repository: LotRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(lotId: String): Result<Unit> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))
            
        return repository.deleteLot(lotId, userId)
    }
}
