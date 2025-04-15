package com.example.growth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.growth.database.AppDatabase
import com.example.growth.ui.screens.AddPlantScreen
import com.example.growth.ui.screens.CameraScreen
import com.example.growth.ui.screens.HomeScreen
import com.example.growth.ui.screens.OnboardingScreen
import com.example.growth.ui.screens.TimeLapseScreen
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

        // Initialize the state variable
        var showPermissionDeniedDialog by mutableStateOf(false)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            val shouldShowRationale = requiredPermissions.any { permission ->
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            }

            if (!allGranted && !shouldShowRationale) {
                // User permanently denied permissions
                showPermissionDeniedDialog = true
            }
        }

        setContent {
            GrowthTheme {
                val navController = rememberNavController() // Controls screen navigation
                var hasPermissions by remember { mutableStateOf(checkAllPermissions()) } // Tracks if we have permissions
                val context = LocalContext.current

                // Database initialization
                val database = remember {
                    try {
                        AppDatabase.getDatabase(context)
                    } catch (e: Exception) {
                        // Temporary null to avoid crashing, remove this after fixing
                        null
                    }
                }

                // Permission handling
                LaunchedEffect(Unit) {
                    if (!hasPermissions) {
                        requestPermissionLauncher.launch(requiredPermissions)
                    }
                }

                // Permission denied dialog
                if (showPermissionDeniedDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDeniedDialog = false },
                        title = { Text("Permissions Required") },
                        text = { Text("Growth needs camera and storage permissions to function properly.") },
                        confirmButton = {
                            Button(onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                                showPermissionDeniedDialog = false
                            }) {
                                Text("Open Settings")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showPermissionDeniedDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        navController = navController,
                        hasPermissions = hasPermissions,
                        database = database
                    )
                }
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    hasPermissions: Boolean,
    database: AppDatabase?
) {
    NavHost(
        navController = navController,
        startDestination = if (hasPermissions) "home" else "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                navController = navController,
                onGetStarted = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("addPlant") {
            AddPlantScreen(navController = navController)
        }

        composable("camera/{plantId}") { backStackEntry ->
            CameraScreen(
                plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull() ?: 0,
                onPhotoTaken = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("timeLapse/{plantId}") { backStackEntry ->
            TimeLapseScreen(
                plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull() ?: 0,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PermissionHandler(
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onPermissionsGranted: () -> Unit,
    showDeniedDialog: Boolean,
    onDismissDialog: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Check permissions when composable first launches
    LaunchedEffect(Unit) {
        if(!hasPermissions) {
            onRequestPermissions()
        }
    }

    // Handle permission state changes
    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            onPermissionsGranted()
        }
    }

    // Show dialog if permissions were permanently denied
    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.permission_required_message)) },
            confirmButton = {
                Button(onClick = onOpenSettings) {
                    Text(stringResource(R.string.settings))
                }
            },
            dismissButton = {
                Button(onClick = onDismissDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    GrowthTheme {
        val navController = rememberNavController()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(
                navController = navController,
                hasPermissions = true,
                database = AppDatabase.getDatabase(LocalContext.current)
            )
        }
    }
}