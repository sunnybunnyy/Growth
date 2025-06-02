package com.example.growth.ui.screens

import com.example.growth.utils.TimeLapseHelper
import android.net.Uri
// UI components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
// Video playback library (ExoPlayer)
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
// Database access
import com.example.growth.database.AppDatabase
import androidx.compose.runtime.collectAsState
import androidx.core.content.FileProvider
// For file operations
import java.io.File
import java.io.IOException

// We're using Material 3 features
@OptIn(ExperimentalMaterial3Api::class)
// Marks this as a function to build UI
@Composable
// Main function that creates the time-lapse screen
// plantId: ID of the plant whose photos will be used
// onBack: Callback function for back button
fun TimeLapseScreen(plantId: Int, onBack: () -> Unit) {
    val context = LocalContext.current // Get context
    // Gets reference to the database and remembers it across recompositions
    val database = remember { AppDatabase.getDatabase(context) }
    // Gets all photos for this plant from database and converts to a Compose state
    val photos by database.plantDao().getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    // State to hold the URI of the generated time-lapse video
    var timeLapseUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) } // State to track loading status
    // State to hold error messages
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Runs this block when the photos list changes
    LaunchedEffect(photos) {
        // Checks if there are at least 2 photos and no time-lapse URI exists yet
        if (photos.size >= 2 && timeLapseUri == null) {
            // Sets loading state to true and clears and previous errors
            isLoading = true
            errorMessage = null

            // Creates an output file in cache directory for the time-lapse video
            val outputFile = File(context.cacheDir, "timelapse_$plantId.mp4").apply {
                // Delete existing file if any
                if (exists()) delete()
            }

            println("Attempting to create timelapse at: ${outputFile.absolutePath}")
            // Tries to create the time-lapse using TimeLapseHelper
            val success = try {
                TimeLapseHelper.createTimeLapse(context, photos, outputFile)
            } catch (e: Exception) {
                errorMessage = "Failed to create timelapse: ${e.localizedMessage}"
                false
            }
            println("Creation result: $success, file exists: ${outputFile.exists()}, size: ${outputFile.length()} bytes")

            // Checks if the file was successfully created and has content
            if (success && outputFile.exists() && outputFile.length() > 0) {
                println("File exists: ${outputFile.exists()}")
                // Creates a content URI for the file
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
            TopAppBar( // Top navigation bar with title of Time-Lapse and back button using arrow
                title = { Text("Time-Lapse") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box( // The main content that fills available space
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Shows different content based on state
            //  Shows loading spinner if isLoading is true
            //  Shows video player if time-lapse URI exists
            //  Shows message if less than 2 photos
            //  Shows error message ir present
            //  Shows generic error as fallback
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
// Function to play videos
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember { // Creates and remembers media player
        ExoPlayer.Builder(context).build().apply {
            try {
                // Verify URI is accessible first
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    setMediaItem(MediaItem.fromUri(uri)) // The video to play
                    prepare() // Prepares the player
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
                useController = true // Enables built-in controls
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    DisposableEffect( // Cleans up when the player is no longer needed by releasing resources
        Unit
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

