package com.example.growth.ui.screens

import android.util.Log
// UI components
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// For navigating between screens
import androidx.navigation.NavController
import com.example.growth.R
// Database access
import com.example.growth.database.AppDatabase
// PlantPhoto table
import com.example.growth.model.PlantPhoto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// For loading images
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

// Allows using Material 3 features
@OptIn(ExperimentalMaterial3Api::class)
// Marks this as a Compose UI function
@Composable
fun PlantDetailsScreen(
    plantId: Int, // ID of the plant to show
    navController: NavController, // For navigation between screens
    database: AppDatabase, // Provides access to app data
    hasCameraPermission: Boolean, // Whether camera permission is granted
    onRequestCameraPermission: () -> Unit // Function to request camera permission
) {
    val plantDao = database.plantDao() // Get access to plant data operations
    val scope = rememberCoroutineScope() // Create a coroutine scope for async operations

    // Fetch plant details and photos from database. collectAsState makes these observable.
    // Auto-updates when data changes
    val plant by plantDao.getPlantById(plantId).collectAsState(initial = null)
    // Fetch plant photos, auto-updates when new photos added
    val photos by plantDao.getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    // Tracks whether edit dialog should be shown
    var showEditDialog by remember { mutableStateOf(false) }

    // Creates the screen structure
    //  Top bar with back button and title
    //  Edit button in top-right
    //  Floating Add Photo button
    //  Main content area
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant?.name ?: stringResource(R.string.plant_details)) },
                navigationIcon = { // Back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = { // Edit button
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                }
            )
        },
        floatingActionButton = { // Add Photo button
            Button(
                onClick = {
                    Log.d("AddPhoto", "Add photo button clicked for plant ID: $plantId")
                    if (hasCameraPermission) {
                        try {
                            navController.navigate("camera/$plantId")
                            Log.d("AddPhoto", "Navigating to camera screen with plant ID: $plantId")
                        } catch (e: Exception) {
                            Log.e("AddPhoto", "Navigation error: ${e.message}", e)
                        }
                    } else {
                        Log.d("AddPhoto", "Camera permission not granted, requesting...")
                        onRequestCameraPermission()
                    }
                }
            ) {
                Text(stringResource(R.string.add_photo))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Vertical scrollable column layout
            // Checks if plant exists before displaying
            plant?.let { currentPlant ->
                // Display plant info if found
                // Shows the main plant image with rounded corners
                Image(
                    painter = rememberAsyncImagePainter(currentPlant.photoPath),
                    contentDescription = stringResource(R.string.plant_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Plant information section, displays plant name, species in a column
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Name and species
                    Text(
                        text = currentPlant.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    currentPlant.species?.let { species ->
                        Text(
                            text = species,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Growth stats row, shows days growing and photo count side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            label = stringResource(R.string.days_growing),
                            value = calculateDaysSince(currentPlant.startDate).toString()
                        )
                        StatItem(
                            label = stringResource(R.string.photos),
                            value = photos.size.toString()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Shows time-lapse button only if there are 2+ photos
                    if (photos.size >= 2) {
                        Button(
                            onClick = { navController.navigate("timeLapse/$plantId") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.view_time_lapse))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Shows horizontal scrollable list of plant photos
                    Text(
                        text = stringResource(R.string.growth_timeline),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (photos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_photos_yet),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyRow {
                            items(photos) { photo ->
                                PhotoTimelineItem(photo = photo)
                            }
                        }
                    }
                }
            } ?: run {
                // Plant not found, show not found message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.plant_not_found))
                }
            }
        }
    }

    // Shows edit dialog when requested, updates plant info when saved
    if (showEditDialog && plant != null) {
        EditPlantDialog(
            plant = plant!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedName, updatedSpecies ->
                scope.launch {
                    plantDao.updatePlant(
                        plant!!.copy(
                            name = updatedName,
                            species = updatedSpecies.ifEmpty { null }
                        )
                    )
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
// Displays a statistic with label
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
// Show a photo with date in timeline
private fun PhotoTimelineItem(photo: PlantPhoto) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(photo.photoPath),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = formatDate(photo.dateTaken),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Helper function for date calculations
private fun calculateDaysSince(startDate: Long): Long {
    return (System.currentTimeMillis() - startDate) / (1000 * 60 * 60 * 24)
}

// Helper function for date formatting
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}