package com.example.growth.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlantPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val dateTaken: Long = System.currentTimeMillis(),
    val photoPath: String,
    val notes: String? = null
)


