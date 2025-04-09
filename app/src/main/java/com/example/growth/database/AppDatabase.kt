package com.example.growth.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.growth.model.Plant
import com.example.growth.model.PlantPhoto
import com.example.growth.database.PlantDao

@Database(entities = [Plant::class, PlantPhoto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "growth_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}