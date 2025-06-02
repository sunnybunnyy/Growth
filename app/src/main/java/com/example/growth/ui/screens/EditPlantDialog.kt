package com.example.growth.ui.screens

// Makes elements take full width
import androidx.compose.foundation.layout.fillMaxWidth
// Pre-made dialog box
import androidx.compose.material3.AlertDialog
// Text input with border
import androidx.compose.material3.OutlinedTextField
// Marks as a UI function
import androidx.compose.runtime.Composable
// For state management
import androidx.compose.runtime.getValue
// For changeable UI state
import androidx.compose.runtime.mutableStateOf
// For translated text
import androidx.compose.ui.res.stringResource
// Plant table
import com.example.growth.model.Plant
// Keeps state across recompositions
import androidx.compose.runtime.remember
// For updating state
import androidx.compose.runtime.setValue
// For displaying text
import androidx.compose.material3.Text
// For accessing app resources (strings)
import com.example.growth.R
// Arranges items vertically
import androidx.compose.foundation.layout.Column
// For styling/layout
import androidx.compose.ui.Modifier
// Adds empty space
import androidx.compose.foundation.layout.Spacer
// Sets height of spacer
import androidx.compose.foundation.layout.height
// Density-independent pixels
import androidx.compose.ui.unit.dp
// Clickable button
import androidx.compose.material3.Button

@Composable
fun EditPlantDialog(
    plant: Plant, // The plant being edited, contains the current plant data
    onDismiss: () -> Unit, // Called when dialog is closed
    onSave: (String, String) -> Unit // Called when saving (passes new name/species)
) {
    // remember keeps values when the UI refreshes
    // mutableStateOf makes the UI update when the value changes
    var name by remember { mutableStateOf(plant.name) } // Stores the plant's name
    // Stores the plant's species (defaults to empty string if null)
    var species by remember { mutableStateOf(plant.species ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss, // Closes dialog when tapping outside
        title = { Text(stringResource(R.string.edit_plant)) }, // Dialog title
        text = { // Main content
            Column { // Arranges children vertically
                // OutlinedTextField is a text input field with a border
                // stringResource fetches translated strings from res/values/strings.xml
                // Plant Name field
                OutlinedTextField(
                    value = name, // Binds to name state
                    onValueChange = { name = it }, // Updates name when typed
                    label = { Text(stringResource(R.string.plant_name)) }, // Hint text
                    modifier = Modifier.fillMaxWidth() // Takes full width
                )
                Spacer(modifier = Modifier.height(8.dp)) // Adds 8dp vertical space
                // Species field
                OutlinedTextField(
                    value = species, // Binds to species state
                    onValueChange = { species = it }, // Updates species when typed
                    label = { Text(stringResource(R.string.species_optional)) }, // Hint text
                    modifier = Modifier.fillMaxWidth() // Takes full width
                )
            }
        },
        confirmButton = { // Save button
            Button(onClick = { onSave(name, species) }) { // Passes new name/species to parent
                Text(stringResource(R.string.save)) // Button text that says Save
            }
        },
        dismissButton = { // Cancel button
            Button(onClick = onDismiss) { // Calls onDismiss to close
                Text(stringResource(R.string.cancel)) // Button text that says Cancel
            }
        }
    )
}