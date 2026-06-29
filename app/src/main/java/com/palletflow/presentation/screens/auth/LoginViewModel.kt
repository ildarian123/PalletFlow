package com.palletflow.presentation.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import com.palletflow.data.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = Channel<Boolean>(Channel.BUFFERED)
    val loginEvent = _loginEvent.receiveAsFlow()

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

        val state = _uiState.value
        val hashedPassword = PasswordHasher.hash(state.password)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val authenticatedUser = userDao.getUserByCredentials(state.email, hashedPassword)
            
            if (authenticatedUser != null) {
                saveCredentials(state.email, hashedPassword, authenticatedUser.id)
                _uiState.update { it.copy(isLoading = false) }
                _loginEvent.trySend(true)
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        emailError = "Invalid email or password"
                    )
                }
                _loginEvent.trySend(false)
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }
        
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }
        
        return isValid
    }

    private fun saveCredentials(email: String, hashedPassword: String, userId: String) {
        authPreferences.saveCredentials(email, hashedPassword, userId)
    }
}

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false
)
