package com.example.growth.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.growth.model.Plant
import com.example.growth.model.PlantPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Insert
    suspend fun insertPlant(plant: Plant): Long

    @Insert
    suspend fun insertPlantPhoto(photo: PlantPhoto)

    @Query("SELECT * FROM Plant")
    suspend fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM PlantPhoto WHERE plantId = :plantId ORDER BY dateTaken ASC")
    suspend fun getPhotosForPlant(plantId: Int): List<PlantPhoto>

    // Add other queries
}