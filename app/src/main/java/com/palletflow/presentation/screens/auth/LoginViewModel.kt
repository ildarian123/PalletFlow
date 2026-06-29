package com.palletflow.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = MutableStateFlow<Boolean?>(null)
    val loginEvent = _loginEvent.asStateFlow()

    init {
        // Pre-fill email with the last successful login email
        val lastEmail = authPreferences.getLastEmail() ?: ""
        _uiState.update { 
            it.copy(email = lastEmail)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun login() {
        if (!validate()) return

        val email = _uiState.value.email
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Check if user exists (ignoring password hash for now as requested - just checking existence)
            val user = userDao.getUserByEmail(email)
            
            if (user == null) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        emailError = "User not found. Please register first."
                    )
                }
                _loginEvent.value = false
            } else {
                // If user exists, check credentials
                val authenticatedUser = userDao.getUserByCredentials(email, password) // Note: Should use hash in real app
                if (authenticatedUser != null) {
                    saveCredentials()
                    _uiState.update { it.copy(isLoading = false) }
                    _loginEvent.value = true
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            passwordError = "Incorrect password"
                        )
                    }
                    _loginEvent.value = false
                }
            }
        }
    }

    fun resetLoginEvent() {
        _loginEvent.value = null
    }

    private fun validate(): Boolean {
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

    private fun saveCredentials() {
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
