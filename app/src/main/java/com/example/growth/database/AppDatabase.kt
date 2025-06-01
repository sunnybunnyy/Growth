// This file belongs to the com.example.growth.database folder
package com.example.growth.database

// Allows access to app resources
import android.content.Context
// Part of the Room library
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
// Tables
import com.example.growth.model.Plant
import com.example.growth.model.PlantPhoto
// Interface that lets us query the database
import com.example.growth.database.PlantDao

// A room database with two tables, one for Plant and one
// for PlantPhoto
@Database(entities = [Plant::class, PlantPhoto::class], version = 1)
// abstract so the class can't be directly instantiated, Room takes care of it
// RoomDatabase() is the base class for all Room databases
abstract class AppDatabase : RoomDatabase() {
    // DAO: Data access object
    // This function returns a PlantDao, which lets you interact with the database
    abstract fun plantDao(): PlantDao

    // A companion object is like a static block, it lets you call functions without creating the class
    companion object {
        // Ensures thread safety, which means multiple threads won't corrupt the database
        @Volatile
        // Stores the database instance so we don't create multiple copies
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is null we create it
            // synchronized(this) prevents multiple threads from creating the database at the same time
            return INSTANCE ?: synchronized(this) {
                // Database configuration
                // context is needed for access to files
                // AppDatabase::class.java is the class that defines the database
                // growth_database is the name of the database file
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "growth_database"
                ).build() // .build() creates the database
                INSTANCE = instance // Saves the database for future use
                instance
            }
        }
    }
}