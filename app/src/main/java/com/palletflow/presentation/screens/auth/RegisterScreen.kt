package com.palletflow.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121212)
private val AccentColor = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val ErrorColor = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Account",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(AccentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PF",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PalletFlow",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create your free account",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            RegisterTextField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = "Full Name",
                keyboardType = KeyboardType.Text,
                error = state.fullNameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterTextField(
                value = state.businessName,
                onValueChange = onBusinessNameChange,
                label = "Business Name",
                keyboardType = KeyboardType.Text,
                error = state.businessNameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email address",
                keyboardType = KeyboardType.Email,
                error = state.emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                error = state.passwordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                error = state.confirmPasswordError
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.White
                ),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = TextSecondary.copy(alpha = 0.2f)
                )
                Text(
                    text = "or continue with",
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = TextSecondary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TextSecondary.copy(alpha = 0.5f))
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google Icon Placeholder
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.White, RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign up with Google",
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    color = AccentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignInClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = ErrorColor)
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordToggle) {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    Icon(imageVector = icon, contentDescription = null, tint = TextSecondary)
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = AccentColor,
            unfocusedLabelColor = TextSecondary,
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
            cursorColor = AccentColor,
            errorBorderColor = ErrorColor,
            errorLabelColor = ErrorColor,
            errorTextColor = TextPrimary
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        state = RegisterUiState(),
        onFullNameChange = {},
        onBusinessNameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onBackClick = {},
        onRegisterClick = {},
        onGoogleClick = {},
        onSignInClick = {}
    )
}
