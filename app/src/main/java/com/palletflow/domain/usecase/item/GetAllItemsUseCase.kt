package com.palletflow.domain.usecase.item

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllItemsUseCase @Inject constructor(
    private val repository: ItemRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(): Flow<List<Item>> {
        val userId = authPreferences.getUserId()
            ?: return flow { emit(emptyList()) }
        return repository.getAllItems(userId)
    }
}
