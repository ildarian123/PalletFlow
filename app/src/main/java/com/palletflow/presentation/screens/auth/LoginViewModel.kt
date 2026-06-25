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
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun saveCredentials() {
        val state = _uiState.value
        authPreferences.saveCredentials(state.email, state.password)
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)
