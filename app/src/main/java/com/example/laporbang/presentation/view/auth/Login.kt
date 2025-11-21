    package com.example.laporbang.presentation.view.auth

    import android.app.Activity
    import android.widget.Toast
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
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
    import androidx.compose.material.icons.filled.ArrowRight
    import androidx.compose.material.icons.filled.Lock
    import androidx.compose.material3.Button
    import androidx.compose.material.icons.filled.Warning
    import androidx.compose.material3.AlertDialog
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.Divider
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextField
    import androidx.compose.material3.TextFieldDefaults
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.MutableState
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.draw.shadow
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.example.laporbang.R
    import com.example.laporbang.*
    import com.example.laporbang.data.model.AuthResult
    import com.example.laporbang.presentation.viewmodel.AuthViewModel
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.rememberCoroutineScope
    import com.example.laporbang.data.model.GoogleSignInResult
    import kotlinx.coroutines.launch
    import androidx.activity.result.IntentSenderRequest

    val COLORS_BG = Color(0xFF101828)
    val COLORS_SURFACE = Color(0xFF1D2939)
    val COLORS_PRIMARY = Color(0xFFF2C94C)
    val COLORS_TEXT = Color(0xFFFFFFFF)
    val COLORS_TEXT_SECONDARY = Color(0xFF98A2B3)
    val COLORS_INPUT_BORDER = Color(0xFF344054)
    val COLORS_ERROR = Color(0xFFF04438)

    @Composable
    fun LoginScreen(
        viewModel: AuthViewModel = viewModel(),

        onLoginSuccess: () -> Unit = {},
        onGoogleLoginClick: () -> Unit = {},
        onSignUpClick: () -> Unit = {},
        onForgetPassword: () -> Unit = {},
    ) {
        val authState by viewModel.authState.collectAsState()

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val emailState = remember { mutableStateOf("") }
        val passwordState = remember { mutableStateOf("") }
        val isPasswordVisibleState = remember { mutableStateOf(false) }

        val googleAuthClient = remember { GoogleAuthClient(context) }

        val errorMessage = remember { mutableStateOf<String?>(null) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val data = result.data
                    if (data != null) {
                        val token = googleAuthClient.getSignInCredentialFromIntent(data)
                        if (token != null) {
                            viewModel.loginWithGoogle(token)
                        } else {
                            Toast.makeText(context, "Google Sign In Failed: No Token", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


        LaunchedEffect(authState) {
            when (val result = authState) {
                is AuthResult.Success -> {
                    onLoginSuccess()
                    viewModel.resetAuthState()
                }
                is AuthResult.Error -> {
                    errorMessage.value = result.message
                    viewModel.resetAuthState()
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
                verticalArrangement = Arrangement.Center
            ) {

                HeaderSection(
                    title = "LaporBang!",
                    subtitle = "Welcome back, please login to your account.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp)
                )

                FormSection(
                    emailState = emailState,
                    passwordState = passwordState,
                    isPasswordVisibleState = isPasswordVisibleState,
                    isLoading = authState is AuthResult.Loading,
                    onForgetPassword = onForgetPassword,
                    onLoginClick = {
                        viewModel.login(emailState.value, passwordState.value)
                    },
                    onGoogleLoginClick = {
                        scope.launch {
                            val signInResult = googleAuthClient.signIn()
                            when (signInResult) {
                                is GoogleSignInResult.Success -> {
                                    launcher.launch(
                                        IntentSenderRequest.Builder(signInResult.intentSender).build()
                                    )
                                }
                                is GoogleSignInResult.Error -> {
                                    errorMessage.value = signInResult.message
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                FooterSection(
                    onSignUpClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                )
                if (errorMessage.value != null) {
                    CustomErrorDialog(
                        message = errorMessage.value!!,
                        onDismiss = { errorMessage.value = null } // Tutup dialog
                    )
                }
            }
        }
    }

    @Composable
    fun CustomErrorDialog(
        message: String,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = COLORS_SURFACE, // Sesuaikan warna background dialog
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = COLORS_ERROR,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Login Failed",
                    color = COLORS_TEXT,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = message,
                    color = COLORS_TEXT_SECONDARY,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = COLORS_PRIMARY),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Try Again",
                        color = COLORS_BG,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }


    @Composable
    fun HeaderSection(title: String, subtitle: String, modifier: Modifier = Modifier) {
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
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        }
    }

    @Composable
    fun FormSection(
        emailState: MutableState<String>,
        passwordState: MutableState<String>,
        isPasswordVisibleState: MutableState<Boolean>,
        isLoading: Boolean, // Parameter baru
        onForgetPassword: () -> Unit,
        onLoginClick: () -> Unit,
        onGoogleLoginClick: () -> Unit,
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

            InputGroup(label = "Password") {
                val isVisible = isPasswordVisibleState.value
                CustomTextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    placeholder = "Enter your password",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = COLORS_TEXT_SECONDARY,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        val icon = if (isVisible) R.drawable.hide else R.drawable.watch
                        IconButton(onClick = { isPasswordVisibleState.value = !isVisible }) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = if (isVisible) "Hide password" else "Show password",
                                tint = COLORS_TEXT_SECONDARY,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
                )
            }

            Text(
                text = "Forgot Password?",
                color = COLORS_PRIMARY,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 24.dp)
                    .clickable(onClick = onForgetPassword)
            )

            Button(
                onClick = onLoginClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(bottom = 20.dp)
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
                        text = "Login",
                        color = COLORS_BG,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ArrowRight,
                        contentDescription = null,
                        tint = COLORS_BG,
                        modifier = Modifier.padding(start = 8.dp).size(20.dp)
                    )
                }
            }

            DividerWithText(text = "Or continue with")

            Button(
                onClick = onGoogleLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = COLORS_SURFACE
                ),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(COLORS_INPUT_BORDER)
                )
            ) {

                Text(
                    text = "G",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = COLORS_TEXT,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = "Login with Google",
                    color = COLORS_TEXT,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    fun FooterSection(onSignUpClick: () -> Unit, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                color = COLORS_TEXT_SECONDARY,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Sign Up",
                color = COLORS_PRIMARY,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSignUpClick)
            )
        }
    }

    @Composable
    fun InputGroup(label: String, content: @Composable () -> Unit) {
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = COLORS_TEXT,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }

    @Composable
    fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        visualTransformation: VisualTransformation = VisualTransformation.None
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = COLORS_TEXT_SECONDARY, fontSize = 16.sp) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = COLORS_SURFACE,
                unfocusedContainerColor = COLORS_SURFACE,
                disabledContainerColor = COLORS_SURFACE,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = COLORS_PRIMARY,
                focusedTextColor = COLORS_TEXT,
                unfocusedTextColor = COLORS_TEXT,
                focusedSupportingTextColor = COLORS_INPUT_BORDER,
                unfocusedSupportingTextColor = COLORS_INPUT_BORDER,
            )
        )
    }

    @Composable
    fun DividerWithText(text: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                color = COLORS_INPUT_BORDER,
                modifier = Modifier.weight(1f).height(1.dp)
            )
            Text(
                text = text,
                color = COLORS_TEXT_SECONDARY,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Divider(
                color = COLORS_INPUT_BORDER,
                modifier = Modifier.weight(1f).height(1.dp)
            )
        }
    }

    @Preview
    @Composable
    fun LoginScreenPreview() {
         LoginScreen()
    }