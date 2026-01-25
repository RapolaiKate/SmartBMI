package com.example.smartbmi.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [BMIEntity::class], version = 2)
abstract class BMIDatabase : RoomDatabase() {

    abstract fun bmiDao(): BMIDao
    companion object {
        @Volatile
        private var INSTANCE: BMIDatabase? = null

        fun getDatabase(context: Context): BMIDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, BMIDatabase::class.java,
                    "bmi_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}