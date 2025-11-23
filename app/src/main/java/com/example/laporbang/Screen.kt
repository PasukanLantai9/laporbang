    package com.example.laporbang

    sealed class Screen(val route: String) {
        object Splash : Screen("splash_screen")
        object Home : Screen("home_screen?reportId={reportId}") {
            fun createRoute(reportId: String? = null): String {
                return if (reportId != null) "home_screen?reportId=$reportId" else "home_screen"
            }
        }
        object Login : Screen("login_screen")
        object Register : Screen("register_screen")
        object ForgotPassword : Screen("forgot_password_screen")
        object OTP : Screen("otp_screen")
        object NewPassword : Screen("new_password_screen?code={code}") {
            fun createRoute(code: String) = "new_password_screen?code=$code"
        }

        object MapScreen : Screen("map_screen")

        object CreateReport : Screen("create_report_screen")
        object LocationPicker : Screen("location_picker_screen")
        object DetectionResult : Screen("detection_result_screen?location={location}") {
            fun createRoute(location: String): String {
                return "detection_result_screen?location=$location"
            }
        }
        object ReportSuccess : Screen("report_success_screen")
        object ReportList : Screen("report_list_screen")
        object ReportDetail : Screen("report_detail_screen/{reportId}") {
            fun createRoute(reportId: String) = "report_detail_screen/$reportId"
        }


    }