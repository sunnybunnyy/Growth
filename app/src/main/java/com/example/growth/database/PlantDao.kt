package com.example.growth.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM PlantPhoto WHERE plantId = :plantId ORDER BY dateTaken ASC")
    fun getPhotosForPlant(plantId: Int): Flow<List<PlantPhoto>>

    @Query("SELECT * FROM Plant WHERE id = :plantId")
    fun getPlantById(plantId: Int): Flow<Plant?>

    @Update
    suspend fun updatePlant(plant: Plant)

    @Query("DELETE FROM Plant WHERE id = :plantId")
    suspend fun deletePlant(plantId: Int)
}