package com.example.laporbang.presentation.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
@Preview
fun OTPVerificationScreen(
    email: String = "example@email.com",
    onVerifyClick: () -> Unit = {},
    onResendClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    var timeLeft by remember { mutableStateOf(60) }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
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

            OTPHeaderSection(
                title = "OTP Verification",
                subtitle = "Enter the 6-digit code we sent to",
                email = email,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
            )

            OTPFormSection(
                otpValues = otpValues,
                onOtpChange = { index, value ->
                    otpValues = otpValues.toMutableList().apply {
                        this[index] = value
                    }
                },
                onVerifyClick = onVerifyClick,
                modifier = Modifier.fillMaxWidth()
            )

            OTPFooterSection(
                timeLeft = timeLeft,
                onResendClick = {
                    timeLeft = 60
                    onResendClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
    }
}


@Composable
fun OTPHeaderSection(
    title: String,
    subtitle: String,
    email: String,
    modifier: Modifier = Modifier
) {
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
            )
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = COLORS_PRIMARY,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun OTPFormSection(
    otpValues: List<String>,
    onOtpChange: (Int, String) -> Unit,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OTPInputFields(
            otpValues = otpValues,
            onOtpChange = onOtpChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
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
                text = "Verify",
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

@Composable
fun OTPInputFields(
    otpValues: List<String>,
    onOtpChange: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        otpValues.forEachIndexed { index, value ->
            OTPDigitBox(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                        onOtpChange(index, newValue)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            if (index < otpValues.size - 1) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun OTPDigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .height(64.dp)
            .background(COLORS_SURFACE, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (value.isNotEmpty()) COLORS_PRIMARY else COLORS_INPUT_BORDER,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = COLORS_TEXT,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = COLORS_TEXT_SECONDARY.copy(alpha = 0.3f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun OTPFooterSection(
    timeLeft: Int,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (timeLeft > 0) {
            Text(
                text = "Resend code in ${timeLeft}s",
                color = COLORS_TEXT_SECONDARY,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Didn't receive the code? ",
                    color = COLORS_TEXT_SECONDARY,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Resend",
                    color = COLORS_PRIMARY,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onResendClick)
                )
            }
        }
    }
}