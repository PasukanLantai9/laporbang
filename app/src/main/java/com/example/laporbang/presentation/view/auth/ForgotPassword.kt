package com.example.laporbang.presentation.view.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laporbang.R
import com.example.laporbang.data.model.AuthResult
import com.example.laporbang.presentation.viewmodel.AuthViewModel


@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel(),
    onSendCodeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    val context = LocalContext.current
    val emailState = remember { mutableStateOf("") }

    LaunchedEffect(forgotPasswordState) {
        when (val result = forgotPasswordState) {
            is AuthResult.Success -> {
                Toast.makeText(context, "Link reset password dikirim ke email!", Toast.LENGTH_LONG).show()
                onSendCodeClick() // Pindah halaman/Kembali
                viewModel.resetForgotPasswordState()
            }
            is AuthResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetForgotPasswordState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(COLORS_BG)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = COLORS_TEXT,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            ForgotPasswordHeaderSection(
                title = "Forgot Password?",
                subtitle = "Don't worry! Enter your email address and we'll send you a link to reset your password.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
            )

            ForgotPasswordFormSection(
                emailState = emailState,
                isLoading = forgotPasswordState is AuthResult.Loading, // Kirim status loading
                onSendCodeClick = {
                    viewModel.sendPasswordResetEmail(emailState.value)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun ForgotPasswordHeaderSection(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = COLORS_TEXT
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = COLORS_TEXT_SECONDARY,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.width(IntrinsicSize.Max)
        )
    }
}

@Composable
fun ForgotPasswordFormSection(
    emailState: MutableState<String>,
    isLoading: Boolean,
    onSendCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        InputGroup(label = "Email") {
            CustomTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                placeholder = "Enter your email",
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.email),
                        contentDescription = "Email Icon",
                        tint = COLORS_TEXT_SECONDARY,
                        modifier = Modifier.size(20.dp)
                    )
                },
            )
        }

        // --- BUTTON MODIFIED ---
        Button(
            onClick = onSendCodeClick,
            enabled = !isLoading, // Disable saat loading
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .padding(top = 10.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = COLORS_PRIMARY.copy(alpha = 0.3f),
                    spotColor = COLORS_PRIMARY.copy(alpha = 0.3f)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoading) COLORS_SURFACE else COLORS_PRIMARY,
                disabledContainerColor = COLORS_SURFACE
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = COLORS_PRIMARY,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Send Link", // Ubah teks jadi Send Link biar sesuai Firebase
                    color = COLORS_BG,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.ArrowRight,
                    contentDescription = null,
                    tint = COLORS_BG,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun ForgotPasswordScreenPreview() {
    // ForgotPasswordScreen()
}