package com.palletflow.presentation.screens.supplier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.repository.SupplierRepository
import com.palletflow.data.datastore.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewSupplierViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewSupplierUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Result<Supplier>>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onContactPersonChange(contact: String) {
        _uiState.update { it.copy(contactPerson = contact) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveSupplier() {
        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Supplier name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authPreferences.getUserId() ?: return@launch
            
            val supplier = Supplier(
                userId = userId,
                name = _uiState.value.name.trim(),
                contactPerson = _uiState.value.contactPerson.trim().ifBlank { null },
                email = _uiState.value.email.trim().ifBlank { null },
                phone = _uiState.value.phone.trim().ifBlank { null },
                address = _uiState.value.address.trim().ifBlank { null },
                notes = _uiState.value.notes.trim().ifBlank { null }
            )

            val result = supplierRepository.createSupplier(supplier)
            _uiState.update { it.copy(isLoading = false) }
            _saveEvent.send(result)
        }
    }
}

data class NewSupplierUiState(
    val name: String = "",
    val nameError: String? = null,
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val isLoading: Boolean = false
)
