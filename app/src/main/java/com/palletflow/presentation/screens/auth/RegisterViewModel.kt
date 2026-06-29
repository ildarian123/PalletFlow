package com.palletflow.presentation.screens.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import com.palletflow.data.local.entity.UserProfile
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
class RegisterViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _registrationEvent = Channel<Boolean>(Channel.BUFFERED)
    val registrationEvent = _registrationEvent.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun onBusinessNameChange(name: String) {
        _uiState.update { it.copy(businessName = name, businessNameError = null) }
    }

    fun register() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Check if email is already taken
                if (userDao.isEmailTaken(state.email) > 0) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            emailError = "Email is already registered"
                        )
                    }
                    _registrationEvent.trySend(false)
                    return@launch
                }

                val hashedPassword = PasswordHasher.hash(state.password)

                val newUser = UserProfile(
                    email = state.email,
                    passwordHash = hashedPassword,
                    businessName = state.businessName
                )

                userDao.insertUser(newUser)
                saveCredentials(state.email, hashedPassword, newUser.id)
                
                _uiState.update { it.copy(isLoading = false) }
                _registrationEvent.trySend(true)
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Registration failed", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        emailError = "An error occurred during registration. Please try again."
                    )
                }
                _registrationEvent.trySend(false)
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.businessName.isBlank()) {
            _uiState.update { it.copy(businessNameError = "Business Name cannot be empty") }
            isValid = false
        }

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
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
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

    private fun saveCredentials(email: String, hashedPassword: String, userId: String) {
        authPreferences.saveCredentials(email, hashedPassword, userId)
    }
}

data class RegisterUiState(
    val businessName: String = "",
    val businessNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false
)
