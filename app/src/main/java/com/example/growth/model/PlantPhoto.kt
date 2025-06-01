package com.example.growth.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlantPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int, // Links the photo to a specific Plant (matches the Plant's id), required
    val dateTaken: Long = System.currentTimeMillis(), // When the photo was taken, default to now
    val photoPath: String, // File path to the actual photo
    val notes: String? = null // Notes about the photo, optional
)


