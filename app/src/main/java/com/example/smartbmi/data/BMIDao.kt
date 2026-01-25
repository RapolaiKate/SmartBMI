package com.example.smartbmi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BMIDao {
    @Insert
    suspend fun insertBMI(bmi: BMIEntity)

    @Query("SELECT * FROM bmi_table ORDER BY date DESC")
    suspend fun getAllBMI(): List<BMIEntity>

}