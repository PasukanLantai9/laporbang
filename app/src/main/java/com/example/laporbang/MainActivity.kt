    package com.example.laporbang

    import android.annotation.SuppressLint
    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
    import androidx.navigation.compose.rememberNavController
    import com.example.laporbang.ui.theme.LaporbangTheme


    class MainActivity : ComponentActivity() {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                LaporbangTheme {
                    val navController = rememberNavController()
                    SetupNavGraph(navController = navController)
                }
            }

            try {
                val info = packageManager.getPackageInfo(
                    packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
                for (signature in info.signatures) {
                    val md = java.security.MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val digest = md.digest()
                    val hexString = StringBuilder()
                    for (b in digest) {
                        hexString.append(String.format("%02X:", b))
                    }
                    android.util.Log.d("CEK_SHA1", "SHA-1 App Kamu: $hexString")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        LaporbangTheme {
            Greeting("Android")
        }
    }