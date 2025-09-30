package com.example.runningtracker.data.model

data class Run(
    val id: Long = 0,
    val distance: Double, // в км
    val time: String,     // HH:MM:SS
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis()
)