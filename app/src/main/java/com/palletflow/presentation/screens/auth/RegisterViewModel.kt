package com.palletflow.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import com.palletflow.data.local.entity.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _registrationEvent = MutableStateFlow<Boolean?>(null)
    val registrationEvent = _registrationEvent.asStateFlow()

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun register() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Check if email is already taken
            if (userDao.isEmailTaken(state.email) > 0) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        emailError = "Email is already registered"
                    )
                }
                _registrationEvent.value = false
                return@launch
            }

            val newUser = UserProfile(
                email = state.email,
                passwordHash = state.password, // Should be hashed
                businessName = state.fullName // Using full name as business name for now
            )

            userDao.insertUser(newUser)
            saveCredentials()
            
            _uiState.update { it.copy(isLoading = false) }
            _registrationEvent.value = true
        }
    }

    fun resetRegistrationEvent() {
        _registrationEvent.value = null
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = "Full Name cannot be empty") }
            isValid = false
        }

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }

        if (state.confirmPassword.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Please confirm your password") }
            isValid = false
        } else if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        return isValid
    }

    private fun saveCredentials() {
        val state = _uiState.value
        authPreferences.saveCredentials(state.email, state.password)
    }
}

data class RegisterUiState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false
)
