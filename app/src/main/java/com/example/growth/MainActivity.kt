package com.example.growth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.growth.database.AppDatabase
import com.example.growth.model.Plant
import com.example.growth.model.PlantPhoto
import com.example.growth.ui.screens.AddPlantScreen
import com.example.growth.ui.screens.CameraScreen
import com.example.growth.ui.screens.HomeScreen
import com.example.growth.ui.screens.OnboardingScreen
import com.example.growth.ui.screens.PlantDetailsScreen
import com.example.growth.ui.screens.TimeLapseScreen
import com.example.growth.ui.theme.GrowthTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var showPermissionDeniedDialog by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            val shouldShowRationale = requiredPermissions.any { permission ->
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
                val hasPermissions = remember { mutableStateOf(checkAllPermissions()) }

                // Then use LaunchedEffect separately at the composable level
                LaunchedEffect(Unit) {
                    hasPermissions.value = checkAllPermissions()
                } // Tracks if we have permissions
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                // Permission handling
                LaunchedEffect(Unit) {
                    if (!hasPermissions.value) {
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


                // Database initialization

                val database = remember {
                    try {
                        AppDatabase.getDatabase(context)
                    } catch (e: Exception) {
                        // Temporary null to avoid crashing, remove this after fixing
                        null
                    }
                }


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        navController = navController,
                        hasPermissions = hasPermissions.value,
                        database = database!!,
                        scope = scope,
                        requestPermission = {
                            requestPermissionLauncher.launch(requiredPermissions)
                        }
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    hasPermissions: Boolean,
    database: AppDatabase,
    scope: CoroutineScope,
    requestPermission: () -> Unit
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

        composable("plantDetails/{plantId}") { backStackEntry ->
            PlantDetailsScreen(
                plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull() ?: 0,
                navController = navController,
                database = database,
                hasCameraPermission = hasPermissions,
                onRequestCameraPermission = { requestPermission() }
            )
        }

        composable("camera/{plantId}") { backStackEntry ->
            val plantIdString = backStackEntry.arguments?.getString("plantId")
            val plantId = plantIdString?.toIntOrNull() ?: 0

            CameraScreen(
                plantId = plantId,
                onPhotoTaken = { photoPath ->
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                hasCameraPermission = hasPermissions,
                savePhoto = { pid, path ->
                    // Save photo to database
                    scope.launch {
                        database.plantDao().insertPlantPhoto(
                            PlantPhoto(
                                plantId = pid,
                                photoPath = path,
                                dateTaken = System.currentTimeMillis()
                            )
                        )
                    }
                }
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
                database = AppDatabase.getDatabase(LocalContext.current),
                scope = TODO(),
                requestPermission = TODO()
            )
        }
    }
}