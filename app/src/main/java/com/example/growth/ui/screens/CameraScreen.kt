package com.example.growth.ui.screens

import android.content.Context // System context
import android.os.Environment
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
// Layout components
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

@Composable
fun CameraScreen(
    plantId: Int, // ID of the plant being photographed
    onPhotoTaken: (String) -> Unit, // Callback when photo is taken (returns file path)
    onCancel: () -> Unit, // Callback when user cancels
    hasCameraPermission: Boolean, // Checks if camera permission is granted
    savePhoto: suspend (Int, String) -> Unit // Saves photo to database in the background
) {
    val context = LocalContext.current // App context
    // Ties camera to screen lifecycle
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope() // For background tasks

    // State variables
    var flashOn by remember { mutableStateOf(false) } // Tracks flash state (on/
    // Camera capture instance
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    // Stores errors
    var cameraError by remember { mutableStateOf<String?>(null) }

    val previewView = remember {
        PreviewView(context).apply {
            // Fallback for older devices
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(Unit) { // Runs once when screen opens
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = withContext(Dispatchers.IO) { // Runs in background
                cameraProviderFuture.get() // Gets camera provider
            }

            // Configures preview (what you see before taking a photo)
            val preview = Preview.Builder().build().also {
                // Attaches to PreviewView
                it.surfaceProvider = previewView.surfaceProvider
            }

            // Configures photo capture
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // Faster capture
                .build()

            imageCapture = capture // Saves for later use

            // Binds camera to screen lifecycle
            cameraProvider.unbindAll() // Clears previous camera sessions
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, // Ties camera to screen lifecycle
                CameraSelector.DEFAULT_BACK_CAMERA, // Uses rear camera
                preview, // Preview setup
                capture // Capture setup
            )
            // Handle flash mode
            camera.cameraControl.enableTorch(flashOn) // Turns flash on/off

        } catch (e: Exception) {
            cameraError = "Failed to start camera: ${e.message}" // Handles errors
        }
    }

    if (!hasCameraPermission) {
        PermissionDeniedView(onCancel = onCancel) // Shows error message
        return
    }

    if (cameraError != null) {
        ErrorView(error = cameraError, onCancel = onCancel) // Shows error message
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Embeds the Android PreviewView in Compose
        AndroidView(
            factory = { previewView }, // The camera preview
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI (buttons)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Spaces items evenly
        ) {
            // Top row (flash + close buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Aligns to right
            ) {
                // Flash toggle button
                IconButton(onClick = { flashOn = !flashOn }) {
                    Icon(
                        if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash"
                    )
                }
                // Close button
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Close Camera")
                }
            }

            // Camera capture button (bottom center)
            IconButton(
                onClick = {
                    val captureInstance = imageCapture
                    if (captureInstance == null) {
                        cameraError = "Camera not ready"
                        return@IconButton
                    }
                    val photoFile = createImageFile(context) // Creates a temp file
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    // Takes the photo
                    captureInstance.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context), // Runs on main thread
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                scope.launch {
                                    savePhoto(plantId, photoFile.absolutePath) // Saves to DB
                                    onPhotoTaken(photoFile.absolutePath) // Notifies parent
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                cameraError = "Failed to take photo: ${exception.message}"
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // Centers the button
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Take Photo",
                    modifier = Modifier.size(64.dp) // Large camera icon
                )
            }
        }
    }
}


private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    // Saves to app-specific folder
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", // Filename prefix (eg. JPEG_20250602_153421_)
        ".jpg", // File extension
        storageDir // Save location
    ).also { it.createNewFile() } // Makes sure the file exists
}

@Composable
private fun PermissionDeniedView(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera permission required",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCancel) {
            Text("Go Back")
        }
    }
}

@Composable
private fun ErrorView(error: String?, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error ?: "Unknown error",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCancel) {
            Text("Go Back")
        }
    }
}