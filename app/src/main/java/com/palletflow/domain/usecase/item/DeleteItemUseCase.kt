package com.palletflow.domain.usecase.item

import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item): Result<Unit> {
        return repository.deleteItem(item, deletePhoto = true)
    }
}
