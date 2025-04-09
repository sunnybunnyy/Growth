package com.example.growth.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val species: String? = null,
    val startDate: Long = System.currentTimeMillis(),
    val photoPath: String
)