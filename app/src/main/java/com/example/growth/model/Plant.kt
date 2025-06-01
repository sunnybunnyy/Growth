package com.example.growth.model

// @Entity tells Room that this class is a database table
// @PrimaryKey marks which field is the unique ID for the table
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
// A special Kotlin class meant for holding data
data class Plant(
    // id is the unique identifier for each plant
    // The database will auto-assign IDs chronologically
    // The default value is 0
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // The plant's name, required
    val species: String? = null, // The plant's type, optional, defaults to null
    val startDate: Long = System.currentTimeMillis(), // When the plant was added, defaults to current time
    val photoPath: String // File path to a photo of the plant, required
)