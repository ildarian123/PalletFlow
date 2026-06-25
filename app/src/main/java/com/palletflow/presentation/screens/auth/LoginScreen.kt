package com.palletflow.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
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

@Composable
fun LoginScreen(
    emailValue: String,
    emailError: String? = null,
    passwordValue: String,
    passwordError: String? = null,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AccentColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PF",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PalletFlow",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Turn Pallets into Profit",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = emailValue,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(text = emailError, color = ErrorColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentColor,
                    unfocusedLabelColor = TextSecondary,
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = AccentColor,
                    errorBorderColor = ErrorColor,
                    errorLabelColor = ErrorColor,
                    errorTextColor = TextPrimary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordValue,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(text = passwordError, color = ErrorColor)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentColor,
                    unfocusedLabelColor = TextSecondary,
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = AccentColor,
                    errorBorderColor = ErrorColor,
                    errorLabelColor = ErrorColor,
                    errorTextColor = TextPrimary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = TextSecondary.copy(alpha = 0.3f)
                )
                Text(
                    text = "or",
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = TextSecondary.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google Icon Placeholder
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up",
                    color = AccentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        emailValue = "test@example.com",
        passwordValue = "password",
        onEmailChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onSignUpClick = {},
        onGoogleClick = {}
    )
}
