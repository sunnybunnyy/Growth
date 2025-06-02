package com.example.growth.ui.screens

import android.content.ContentValues // For storing image metadata
import android.content.Context // System context
import android.content.Intent // For launching camera/gallery
import android.net.Uri // For image file paths
import android.provider.MediaStore // Media storage system
import androidx.compose.foundation.Image // For displaying images
// Layout components
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState // For scrollable content
import androidx.compose.foundation.verticalScroll //  Makes content scrollable
import androidx.compose.material.icons.Icons // Material icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Back arrow icon
// Material Design 3 components
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
// For managing UI state
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier // UI layout modifiers
import androidx.compose.ui.layout.ContentScale // How to crop/scale images
import androidx.compose.ui.platform.LocalContext // Gets context
import androidx.compose.ui.platform.LocalLifecycleOwner // For coroutine lifecycle
import androidx.compose.ui.unit.dp // Pixels for sizing
import androidx.lifecycle.lifecycleScope // For background tasks (coroutines)
import androidx.navigation.NavController // For navigating between screens
import coil.compose.rememberAsyncImagePainter // Image loading library
import com.example.growth.database.AppDatabase // Database
import com.example.growth.model.Plant // Plant data class
import kotlinx.coroutines.launch // For running background tasks

// Allows use of newer Material 3 features
@OptIn(ExperimentalMaterial3Api::class)
@Composable // Marks this as a Jetpack Compose UI function
// navController handles navigation like going back to the previous screen
fun AddPlantScreen(navController: NavController) {
    // remember retains values across UI updates
    // mutableStateOf makes the UI update when these values change
    var plantName by remember { mutableStateOf("") } // Stores the plant's name
    var species by remember { mutableStateOf("") } // Stores the plant's species
    var photoUri by remember { mutableStateOf<Uri?>(null) } // Stores the plant photo's file path
    val context = LocalContext.current // Gets the app context
    val database = remember { AppDatabase.getDatabase(context) } // Gets the database instance
    // For background tasks
    val lifecycleScope = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycleScope

    // App Bar + Content
    Scaffold(
        topBar = {
            TopAppBar( // The top bar with title and back button
                title = { Text("Add New plant") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Back button
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding -> // padding avoids overlapping with the top bar
        Column( // Arranges children vertically
            modifier = Modifier
                .padding(padding) // Adds top bar padding
                .padding(16.dp) // Adds 16dp padding around content
                .verticalScroll(rememberScrollState()) // Makes the content scrollable
        ) {
            OutlinedTextField(
                value = plantName, // Binds to plantName state
                onValueChange = { plantName = it }, // Updates plantName when typed
                label = { Text("Plant Name") }, // Placeholder text
                modifier = Modifier.fillMaxWidth() // Makes the field full-width
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = species, // Binds to species state
                onValueChange = { species = it }, // Updates species when typed
                label = { Text("Species (Optional)") }, // Placeholder text
                modifier = Modifier.fillMaxWidth() //  Makes the field full-width
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoUri), // Loads the image
                    contentDescription = "Plant photo", // For accessibility
                    modifier = Modifier
                        .fillMaxWidth() // Full width
                        .aspectRatio(1f), // Square (1:1 ratio)
                    contentScale = ContentScale.Crop // Crops image to fit
                )
            } else {
                Button(
                    onClick = {
                        // Launch the camera
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // Camera intent
                        photoUri = createImageUri(context) // Creates a file to save the photo
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri) // Tells camera where to save
                        context.startActivity(intent) // Opens the camera
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Initial Photo")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Only save if name/photo exist
                    if (plantName.isNotBlank() && photoUri != null) {
                        // Save to database
                        val plant = Plant( // Creates a new Plant object
                            name = plantName,
                            species = species.ifBlank { null }, // Sets species to null if empty
                            photoPath = photoUri.toString() // Converts URI to string
                        )

                        // Runs in background (database ops shouldn't block UI)
                        lifecycleScope.launch {
                            database.plantDao().insertPlant(plant) // Saves to database
                            navController.popBackStack() // Goes back to previous screen
                        }
                    }
                },
                enabled = plantName.isNotBlank() && photoUri != null, // Disables if fields missing
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Plant")
            }
        }
    }
}

// Create a unique file path like /storage/plant_123456789.jpg for the photo
private fun createImageUri(context: Context): Uri {
    val contentValues = ContentValues().apply { // Metadata for the image
        put(MediaStore.Images.Media.DISPLAY_NAME, "plant_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert( // Creates a file in the device's gallery
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Standard photos location
        contentValues
    )!! // !! means crash if this fails
}