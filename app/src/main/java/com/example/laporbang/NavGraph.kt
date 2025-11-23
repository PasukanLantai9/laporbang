package com.example.laporbang

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.laporbang.presentation.view.ReportListScreen
import com.example.laporbang.presentation.view.detection.DetectionResultScreen
import com.example.laporbang.presentation.view.detection.CameraScreen
import com.example.laporbang.presentation.view.detection.LocationPickerScreen
import com.example.laporbang.presentation.view.map.MapScreen
import com.example.laporbang.presentation.view.auth.ForgotPasswordScreen
import com.example.laporbang.presentation.view.auth.LoginScreen
import com.example.laporbang.presentation.view.auth.OTPVerificationScreen
import com.example.laporbang.presentation.view.auth.RegisterScreen
import com.example.laporbang.presentation.view.detection.CameraScreen
import com.example.laporbang.presentation.view.detection.DetectionResultScreen
import com.example.laporbang.presentation.view.detection.LocationPickerScreen
import com.example.laporbang.presentation.view.detection.ReportSuccessScreen
import com.example.laporbang.presentation.view.map.MapScreen
import com.example.laporbang.presentation.view.map.ReportDetailScreen

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
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoogleLoginClick = { },
                onSignUpClick = { navController.navigate(Screen.Register.route) },
                onForgetPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onSendCodeClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
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
                onResendClick = { },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Home.route) {
            MapScreen(
                onCameraClick = { navController.navigate(Screen.CreateReport.route) },
                onNotificationClick = { },
                onViewAllStats = { navController.navigate(Screen.ReportList.route) },

                onReportClick = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                }
            )
        }

        composable(route = Screen.ReportList.route) {
            ReportListScreen(
                onBackClick = { navController.popBackStack() },
                onItemClick = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                }
            )
        }

        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(androidx.navigation.navArgument("reportId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""

            ReportDetailScreen(
                reportId = reportId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.CreateReport.route) { backStackEntry ->

            CameraScreen(
                onBackClick = { navController.popBackStack() },
                onCapturePhoto = {
                    val lokasiSaatIni = "Jl. Simulasi No. 1, Jakarta"

                    navController.navigate(Screen.DetectionResult.createRoute(lokasiSaatIni))
                },
                onLocationClick = { navController.navigate(Screen.LocationPicker.route) }
            )
        }

        composable(
            route = Screen.DetectionResult.route,
            arguments = listOf(navArgument("location") { defaultValue = "Lokasi tidak diketahui" })
        ) { backStackEntry ->
            val initialLocation = backStackEntry.arguments?.getString("location") ?: ""
            val savedStateHandle = backStackEntry.savedStateHandle
            val updatedLocation = savedStateHandle.get<String>("location_address")
            val finalLocation = updatedLocation ?: initialLocation

            DetectionResultScreen(
                initialLocation = finalLocation,
                onBackClick = { navController.popBackStack() },
                onChangeLocationClick = { navController.navigate(Screen.LocationPicker.route) },
                onUploadClick = {
                    navController.navigate(Screen.ReportSuccess.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(route = Screen.ReportSuccess.route) {
            ReportSuccessScreen(
                onBackToMap = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onCreateNewReport = {
                    navController.navigate(Screen.CreateReport.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }



        composable(route = Screen.LocationPicker.route) {
            LocationPickerScreen(
                onBackClick = { navController.popBackStack() },
                onLocationSelected = { address, lat, lng ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("location_address", address)
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.MapScreen.route) { MapScreen() }
    }
}