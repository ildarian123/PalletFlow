package com.palletflow.domain.usecase.item

import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import javax.inject.Inject

class UpdateItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item): Result<Item> {
        return repository.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
    }
}
