package com.example.laporbang.presentation.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laporbang.R

@Composable
@Preview
fun ForgotPasswordScreen(
    onSendCodeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val emailState = remember { mutableStateOf("") }

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

            // Back Button
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
                subtitle = "Don't worry! Enter your email address and we'll send you a code to reset your password.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
            )

            ForgotPasswordFormSection(
                emailState = emailState,
                onSendCodeClick = onSendCodeClick,
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
    emailState: androidx.compose.runtime.MutableState<String>,
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

        Button(
            onClick = onSendCodeClick,
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
                containerColor = COLORS_PRIMARY
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Send Code",
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