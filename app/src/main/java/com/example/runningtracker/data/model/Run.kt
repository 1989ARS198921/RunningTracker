// File: app/src/main/java/com/example/runningtracker/data/model/Run.kt
package com.example.runningtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs")
data class Run(
    @PrimaryKey val id: Long = 0, // или используйте autoGenerate = true в @PrimaryKey, если ID генерируется автоматически
    val distance: Double, // в км
    val time: String,     // HH:MM:SS
    val calories: Int,
    val steps: Int = 0,   // <-- ДОБАВЛЕНО: поле для шагов
    val timestamp: Long = System.currentTimeMillis() // Временная метка
)