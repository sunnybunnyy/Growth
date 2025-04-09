package com.example.growth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.growth.ui.OnboardingScreen
import com.example.growth.ui.theme.GrowthTheme

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                // Permissions granted, continue
                // TODO: Trigger a recomposition here or navigate to home
            } else {
                // TODO: Explain or disable features
            }
        }
        setContent {
            GrowthTheme {
                val navController = rememberNavController()
                val permissions = remember { mutableStateOf(false) }

                // Check permissions
                LaunchedEffect(Unit) {
                    val allGranted = requiredPermissions.all {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }

                    if (!allGranted) {
                        // Request permissions if not all granted
                        requestPermissionLauncher.launch(requiredPermissions)
                    }

                    permissions.value = allGranted
                }

                NavHost(
                    navController = navController,
                    startDestination = if (permissions.value) "home" else "onboarding"
                ) {
                    composable("onboarding") { OnboardingScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    // TODO: Add other destinations
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    // TODO: Implement home screen
    Text("Home Screen")
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
    GrowthTheme {
        Greeting("Android")
    }
}