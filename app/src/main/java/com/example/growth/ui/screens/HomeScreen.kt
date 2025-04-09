package com.example.growth.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.growth.database.AppDatabase
import com.example.growth.model.Plant
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val plants by produceState<List<Plant>>(initialValue = emptyList()) {
        // Launch a coroutine in the produceState scope
        database.plantDao().getAllPlants()
            .collect { plantsList ->
                value = plantsList
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Plants") },
                actions = {
                    IconButton(onClick = { navController.navigate("addPlant") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plant")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addPlant") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Plant")
            }
        }
    ) { padding ->
        if (plants.isEmpty()) {
            EmptyState(
                onAddPlant = { navController.navigate("addPlant") },
                modifier = Modifier.padding(padding)
            )
        } else {
            PlantGrid(plants, padding, navController)
        }
    }
}

@Composable
fun EmptyState(
    onAddPlant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
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
            Spacer(Modifier.width(8.dp))
            Text("Add your first plant")
        }
    }
}

@Composable
fun PlantGrid(plants: List<Plant>, padding: PaddingValues, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = padding,
        modifier = Modifier.fillMaxSize()
    ) {
        items(plants) {
            plant -> PlantCard(plant, navController)
        }
    }
}

@Composable
fun PlantCard(plant: Plant, navController: NavController) {
    Card(
        onClick = { navController.navigate("plantDetails/${plant.id}") },
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column {
            // Load plant image
            Image(
                painter = rememberAsyncImagePainter(plant.photoPath),
                contentDescription = plant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = plant.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            // Add progress info
        }
    }
}