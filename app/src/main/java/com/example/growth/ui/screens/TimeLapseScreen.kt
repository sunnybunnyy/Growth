package com.example.growth.ui.screens

import com.example.growth.utils.TimeLapseHelper
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.growth.database.AppDatabase
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLapseScreen(plantId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val photos by database.plantDao().getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    var timeLapseUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(photos) {
        if (photos.size >= 2 && timeLapseUri == null) {
            isLoading = true
            errorMessage = null

            val outputFile = File(context.cacheDir, "timelapse_$plantId.mp4").apply {
                // Delete existing file if any
                if (exists()) delete()
            }

            println("Attempting to create timelapse at: ${outputFile.absolutePath}")
            val success = try {
                TimeLapseHelper.createTimeLapse(context, photos, outputFile)
            } catch (e: Exception) {
                errorMessage = "Failed to create timelapse: ${e.localizedMessage}"
                false
            }
            println("Creation result: $success, file exists: ${outputFile.exists()}, size: ${outputFile.length()} bytes")

            if (success && outputFile.exists() && outputFile.length() > 0) {
                println("File exists: ${outputFile.exists()}")
                timeLapseUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
                println("URI set to: $timeLapseUri")
                // Verify we can actually open the file
                context.contentResolver.openFileDescriptor(timeLapseUri!!, "r")?.use {
                    // File is accessible
                }
            } else {
                errorMessage = "Failed to create timelapse. Please try again."
                println("File creation failed - exists: ${outputFile.exists()}, size: ${outputFile.length()}")
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Time-Lapse") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                timeLapseUri != null -> VideoPlayer(timeLapseUri!!)
                photos.size < 2 -> Text("Need at least 2 photos to create a time-lapse")
                errorMessage != null -> Text(errorMessage!!)
                else -> Text("Error creating time-lapse")
            }
        }
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            try {
                // Verify URI is accessible first
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    setMediaItem(MediaItem.fromUri(uri))
                    prepare()
                } ?: run {
                    // File not accessible
                    throw IOException("Cannot access file at $uri")
                }
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    DisposableEffect(
        Unit
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

