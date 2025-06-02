package com.example.growth.ui.screens

// For displaying images
import androidx.compose.foundation.Image
// Layout components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
// For grid layouts
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
// Material icons
import androidx.compose.material.icons.Icons
// The + icon
import androidx.compose.material.icons.filled.Add
// Material Design 3 components
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
// For state management
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// For navigating between screens
import androidx.navigation.NavController
// Database
import com.example.growth.database.AppDatabase // Our database
import com.example.growth.model.Plant // Plant table
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
// Loads images from storage
import coil.compose.rememberAsyncImagePainter

// Allows newer Material 3 features
@OptIn(ExperimentalMaterial3Api::class)
// Marks this as a UI function
@Composable
// navController handles navigation to other screens
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current // Gets context
    val database = remember { AppDatabase.getDatabase(context) } // Database instance
    // Load plants from database (auto-updates when data changes)
    // produceState fetches plants from the database and updates when data changes
    val plants by produceState<List<Plant>>(initialValue = emptyList()) {
        // Launch a coroutine in the produceState scope
        database.plantDao().getAllPlants()
            .collect { plantsList ->
                value = plantsList
            }
    }

    Scaffold( // Material 3 layout structure
        topBar = {
            TopAppBar( // Header at top
                title = { Text("Your Plants") }, // Screen title
                actions = { // Top-right button
                    IconButton(onClick = { navController.navigate("addPlant") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plant")
                    }
                }
            )
        },
        floatingActionButton = { // + button at bottom-right
            FloatingActionButton(onClick = { navController.navigate("addPlant") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Plant")
            }
        }
    ) { padding -> // Avoids overlapping with system UI
        if (plants.isEmpty()) { // Shows if no plants
            EmptyState(
                onAddPlant = { navController.navigate("addPlant") },
                modifier = Modifier.padding(padding)
            )
        } else { // Shows plant grid
            PlantGrid(plants, padding, navController)
        }
    }
}

@Composable
fun EmptyState(
    onAddPlant: () -> Unit, // Called when + button is clicked
    modifier: Modifier = Modifier
) {
    Column( // Centers content vertically/horizontally
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No plants yet",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = onAddPlant,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp)) // Adds space between icon and text
            Text("Add your first plant")
        }
    }
}

@Composable
// plants: List of plants to display
// padding: Avoids overlapping with app bars
fun PlantGrid(plants: List<Plant>, padding: PaddingValues, navController: NavController) {
    // Grid layout that only loads visible items
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        contentPadding = padding,
        modifier = Modifier.fillMaxSize()
    ) {
        // Converts each plant to a card
        items(plants) {
            plant -> PlantCard(plant, navController)
        }
    }
}

@Composable
fun PlantCard(plant: Plant, navController: NavController) {
    // Material 3 card with shadow/border
    Card(
        onClick = { navController.navigate("plantDetails/${plant.id}") }, // Opens details
        modifier = Modifier
            .padding(8.dp) // Adds space around cards
            .fillMaxWidth() // Takes full column width
    ) {
        Column {
            // Plant image
            Image(
                painter = rememberAsyncImagePainter(plant.photoPath), // Loads image
                contentDescription = plant.name, // For accessibility
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Square image
                contentScale = ContentScale.Crop // Crops to fill space
            )
            // Plant Name
            Text(
                text = plant.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}