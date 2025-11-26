package com.example.laporbang

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.laporbang.presentation.view.map.MapScreen
import com.example.laporbang.presentation.view.map.ReportDetailScreen
import com.example.laporbang.presentation.view.auth.ForgotPasswordScreen
import com.example.laporbang.presentation.view.auth.LoginScreen
import com.example.laporbang.presentation.view.auth.OTPVerificationScreen
import com.example.laporbang.presentation.view.auth.RegisterScreen
import com.example.laporbang.presentation.view.detection.CameraScreen
import com.example.laporbang.presentation.view.detection.DetectionResultScreen
import com.example.laporbang.presentation.view.detection.LocationPickerScreen
import com.example.laporbang.presentation.view.detection.ReportSuccessScreen
import com.example.laporbang.presentation.view.map.ReportListScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                onGoogleLoginClick = { /* Handle inside LoginScreen */ },
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



        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val focusId = backStackEntry.arguments?.getString("reportId")
            MapScreen(
                onCameraClick = { navController.navigate(Screen.CreateReport.route) },
                onNotificationClick = { },
                onViewAllStats = { navController.navigate(Screen.ReportList.route) },

                onReportClick = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                },

                reportIdToFocus = focusId
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
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""

            ReportDetailScreen(
                reportId = reportId,
                onBackClick = { navController.popBackStack() },

                onViewOnMapClick = {
                    navController.navigate(Screen.Home.createRoute(reportId)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.CreateReport.route) { backStackEntry ->
            val savedStateHandle = backStackEntry.savedStateHandle
            val selectedLocation = savedStateHandle.get<String>("location_address")

            CameraScreen(
                onBackClick = { navController.popBackStack() },
                onCapturePhoto = { imageUri, lat, lng ->
                    val lokasiNama = selectedLocation ?: "Lokasi Saat Ini"

                    val encodedUri = URLEncoder.encode(imageUri, StandardCharsets.UTF_8.toString())

                    navController.currentBackStackEntry?.savedStateHandle?.set("lat", lat)
                    navController.currentBackStackEntry?.savedStateHandle?.set("lng", lng)

                    navController.navigate(
                        Screen.DetectionResult.createRoute(lokasiNama, encodedUri)
                    )
                },
                onLocationClick = { navController.navigate(Screen.LocationPicker.route) },
                initialLocation = selectedLocation
            )
        }

        composable(
            route = Screen.DetectionResult.route,
            arguments = listOf(
                navArgument("location") { defaultValue = "" },
                navArgument("imageUri") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val initialLocation = backStackEntry.arguments?.getString("location") ?: ""
            val imageUriArg = backStackEntry.arguments?.getString("imageUri") ?: ""

            val lat = navController.previousBackStackEntry?.savedStateHandle?.get<Double>("lat") ?: 0.0
            val lng = navController.previousBackStackEntry?.savedStateHandle?.get<Double>("lng") ?: 0.0

            val savedStateHandle = backStackEntry.savedStateHandle
            val updatedLocation = savedStateHandle.get<String>("location_address")
            val updatedLat = savedStateHandle.get<Double>("location_lat")
            val updatedLng = savedStateHandle.get<Double>("location_lng")

            val finalLocation = updatedLocation ?: initialLocation
            val finalLat = updatedLat ?: lat
            val finalLng = updatedLng ?: lng

            DetectionResultScreen(
                initialLocation = finalLocation,
                initialLat = finalLat,
                initialLng = finalLng,
                imageUriString = imageUriArg,
                onBackClick = { navController.popBackStack() },
                onChangeLocationClick = { navController.navigate(Screen.LocationPicker.route) },
                onUploadSuccess = {
                    navController.navigate(Screen.ReportSuccess.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        // 3. Location Picker
        composable(route = Screen.LocationPicker.route) {
            LocationPickerScreen(
                onBackClick = { navController.popBackStack() },
                onLocationSelected = { address, lat, lng ->

                    navController.previousBackStackEntry?.savedStateHandle?.set("location_address", address)
                    navController.previousBackStackEntry?.savedStateHandle?.set("location_lat", lat)
                    navController.previousBackStackEntry?.savedStateHandle?.set("location_lng", lng)
                    navController.popBackStack()
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

        composable(route = Screen.MapScreen.route) { MapScreen() }
    }
}