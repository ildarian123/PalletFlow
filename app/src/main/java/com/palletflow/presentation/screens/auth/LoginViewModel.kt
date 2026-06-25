package com.palletflow.presentation.screens.auth

import androidx.lifecycle.ViewModel
import com.palletflow.data.datastore.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authPreferences: AuthPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load saved credentials on start
        val savedEmail = authPreferences.getEmail() ?: ""
        val savedPassword = authPreferences.getPassword() ?: ""
        _uiState.update { 
            it.copy(
                email = savedEmail,
                password = savedPassword
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun validate(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password
        
        var isValid = true
        
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        }
        
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }
        
        return isValid
    }

    fun saveCredentials() {
        val state = _uiState.value
        authPreferences.saveCredentials(state.email, state.password)
    }
}

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false
)
