package com.example.growth.database

// Room annotations for database operations
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
// Database tables
import com.example.growth.model.Plant
import com.example.growth.model.PlantPhoto
// Allows for real-time data changes
import kotlinx.coroutines.flow.Flow

// A room data access object
@Dao
interface PlantDao {
    // Room auto-generates the SQL to insert a Plant
    // suspend means it runs in a background thread
    // Returns the ID of the inserted plant
    @Insert
    suspend fun insertPlant(plant: Plant): Long

    // Room auto-generates the SQL to insert a PlantPhoto
    // suspend means it runs in a background thread
    @Insert
    suspend fun insertPlantPhoto(photo: PlantPhoto)

    // @Query lets you write custom SQL
    // Gets all rows from the Plant table
    // Flow<List<Plant>> returns a live-updating list of plants, so it data changes the UI updates
    // automatically
    @Query("SELECT * FROM Plant")
    fun getAllPlants(): Flow<List<Plant>>

    // Only gets photos for a specific plant and sorts them so that the oldest as first
    @Query("SELECT * FROM PlantPhoto WHERE plantId = :plantId ORDER BY dateTaken ASC")
    fun getPhotosForPlant(plantId: Int): Flow<List<PlantPhoto>>

    // Gets a single plant
    @Query("SELECT * FROM Plant WHERE id = :plantId")
    fun getPlantById(plantId: Int): Flow<Plant?>

    // Room auto-generates the SQL to update a Plant
    @Update
    suspend fun updatePlant(plant: Plant)

    // Deletes a plant by its ID
    @Query("DELETE FROM Plant WHERE id = :plantId")
    suspend fun deletePlant(plantId: Int)
}