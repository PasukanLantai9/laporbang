package com.example.laporbang.presentation.view.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laporbang.R
import com.example.laporbang.data.model.AuthResult
import com.example.laporbang.presentation.viewmodel.AuthViewModel

@Composable
fun NewPasswordScreen(
    oobCode: String,
    viewModel: AuthViewModel = viewModel(),
    onResetSuccess: () -> Unit = {}
) {
    val resetState by viewModel.resetPasswordState.collectAsState()
    val context = LocalContext.current

    val newPasswordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }
    val isPasswordVisible = remember { mutableStateOf(false) }
    val isConfirmVisible = remember { mutableStateOf(false) }

    LaunchedEffect(resetState) {
        when (val result = resetState) {
            is AuthResult.Success -> {
                Toast.makeText(context, "Password berhasil diubah! Silakan Login.", Toast.LENGTH_LONG).show()
                onResetSuccess()
                viewModel.resetResetPasswordState()
            }
            is AuthResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetResetPasswordState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(COLORS_BG)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold, color = COLORS_TEXT, fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your new password below.",
                color = COLORS_TEXT_SECONDARY,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            InputGroup("New Password") {
                val isVisible = isPasswordVisible.value
                CustomTextField(
                    value = newPasswordState.value,
                    onValueChange = { newPasswordState.value = it },
                    placeholder = "New Password",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = COLORS_TEXT_SECONDARY) },
                    trailingIcon = {
                        val icon = if (isVisible) R.drawable.hide else R.drawable.watch
                        IconButton(onClick = { isPasswordVisible.value = !isVisible }) {
                            Icon(painterResource(id = icon), null, tint = COLORS_TEXT_SECONDARY, modifier = Modifier.size(20.dp))
                        }
                    },
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
                )
            }

            // Confirm Password Field
            InputGroup("Confirm Password") {
                val isVisible = isConfirmVisible.value
                CustomTextField(
                    value = confirmPasswordState.value,
                    onValueChange = { confirmPasswordState.value = it },
                    placeholder = "Confirm Password",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = COLORS_TEXT_SECONDARY) },
                    trailingIcon = {
                        val icon = if (isVisible) R.drawable.hide else R.drawable.watch
                        IconButton(onClick = { isConfirmVisible.value = !isVisible }) {
                            Icon(painterResource(id = icon), null, tint = COLORS_TEXT_SECONDARY, modifier = Modifier.size(20.dp))
                        }
                    },
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.confirmPasswordReset(oobCode, newPasswordState.value, confirmPasswordState.value)
                },
                enabled = resetState !is AuthResult.Loading,
                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(8.dp, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = COLORS_PRIMARY),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (resetState is AuthResult.Loading) {
                    CircularProgressIndicator(color = COLORS_BG, modifier = Modifier.size(24.dp))
                } else {
                    Text("Change Password", color = COLORS_BG, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}