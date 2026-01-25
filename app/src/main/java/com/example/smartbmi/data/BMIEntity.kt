package com.example.smartbmi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bmi_table")
     data class BMIEntity(@PrimaryKey(autoGenerate = true)

        val id: Int = 0,
        val bmi: Double,
        val category: String,
        val date: Long
)

