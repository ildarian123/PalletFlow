package com.palletflow.domain.usecase.item

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.domain.repository.ItemRepository
import javax.inject.Inject

class CreateItemUseCase @Inject constructor(
    private val repository: ItemRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        name: String,
        type: ItemType,
        sku: String?,
        notes: String?,
        photoPath: String?
    ): Result<Item> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        val item = Item(
            userId = userId,
            name = name.trim(),
            type = type,
            sku = sku?.trim().takeIf { !it.isNullOrBlank() },
            notes = notes?.trim().takeIf { !it.isNullOrBlank() },
            photoPath = photoPath
        )
        return repository.createItem(item)
    }
}
