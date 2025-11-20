package com.example.laporbang

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.laporbang.presentation.view.auth.ForgotPasswordScreen
import com.example.laporbang.presentation.view.auth.LoginScreen
import com.example.laporbang.presentation.view.auth.OTPVerificationScreen
import com.example.laporbang.presentation.view.auth.RegisterScreen
@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            AnimatedSplashScreen(navController = navController)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoogleLoginClick = { /* TODO */ },
                onSignUpClick = {
                    navController.navigate(Screen.Register.route)
                },
                onForgetPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onSendCodeClick = {
                    navController.navigate(Screen.OTP.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.OTP.route) {
            OTPVerificationScreen(
                email = "user@example.com",
                onVerifyClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onResendClick = {
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Home.route) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(text = "Welcome Home!")
            }
        }
    }
}