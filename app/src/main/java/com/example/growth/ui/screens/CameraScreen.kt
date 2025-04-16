package com.example.growth.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint.Align
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

@Composable
fun CameraScreen(
    plantId: Int,
    onPhotoTaken: (String) -> Unit,
    onCancel: () -> Unit,
    hasCameraPermission: Boolean,
    savePhoto: suspend (Int, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var flashOn by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(Unit) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = withContext(Dispatchers.IO) {
                cameraProviderFuture.get()
            }

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageCapture = capture

            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                capture
            )
            // Handle flash mode
            camera.cameraControl.enableTorch(flashOn)

        } catch (e: Exception) {
            cameraError = "Failed to start camera: ${e.message}"
        }
    }

    if (!hasCameraPermission) {
        PermissionDeniedView(onCancel = onCancel)
        return
    }

    if (cameraError != null) {
        ErrorView(error = cameraError, onCancel = onCancel)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { flashOn = !flashOn }) {
                    Icon(
                        if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash"
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Close Camera")
                }
            }

            IconButton(
                onClick = {
                    val captureInstance = imageCapture
                    if (captureInstance == null) {
                        cameraError = "Camera not ready"
                        return@IconButton
                    }
                    val photoFile = createImageFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    captureInstance.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                scope.launch {
                                    savePhoto(plantId, photoFile.absolutePath)
                                    onPhotoTaken(photoFile.absolutePath)
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                cameraError = "Failed to take photo: ${exception.message}"
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Take Photo",
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}


private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    ).also { it.createNewFile() }
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