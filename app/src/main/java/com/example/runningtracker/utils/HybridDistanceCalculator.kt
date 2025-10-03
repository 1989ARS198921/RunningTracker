// File: app/src/main/java/com/example/runningtracker/utils/HybridDistanceCalculator.kt
package com.example.runningtracker.utils

import android.location.Location
import kotlin.math.max

class HybridDistanceCalculator(
    private val stepLength: Double = 0.5, // длина шага в метрах (0.5 м = 1 шаг -> 0.5 м)
    private val gpsWeight: Double = 0.7,   // вес GPS (0.7 = 70% от GPS, 30% от шагов)
    private val minStepThreshold: Int = 1  // минимальное количество шагов для учёта (уменьшено)
) {
    private var totalDistance = 0.0 // Общее расстояние в метрах
    private var lastGpsLocation: Location? = null
    private var lastStepCount = 0
    private var stepBasedDistance = 0.0 // Расстояние только от шагов в метрах

    fun addGpsLocation(location: Location) {
        lastGpsLocation?.let { lastLocation ->
            if (location.hasAccuracy() && location.accuracy < 20) {
                val distance = lastLocation.distanceTo(location).toDouble()
                if (distance > 0.1) { // Уменьшенный порог
                    totalDistance += distance * gpsWeight
                }
            }
        }
        lastGpsLocation = location
    }

    fun updateSteps(stepCount: Int) {
        val stepsDelta = max(0, stepCount - lastStepCount)
        if (stepsDelta >= minStepThreshold) {
            // Рассчитываем расстояние, пройденное за шаги, используя длину шага
            val stepDistance = stepsDelta * stepLength
            stepBasedDistance += stepDistance
            // Добавляем расстояние, взвешенное по шагам (остаток от 1 - gpsWeight)
            totalDistance += stepDistance * (1 - gpsWeight)
        }
        lastStepCount = stepCount
    }

    fun getTotalDistance(): Double = totalDistance / 1000.0 // возвращаем в км

    // --- НОВЫЙ МЕТОД: получить общее расстояние, рассчитанное по шагам ---
    fun getTotalStepBasedDistance(): Double = stepBasedDistance / 1000.0 // возвращаем в км
    // --- КОНЕЦ НОВОГО МЕТОДА ---

    fun reset() {
        totalDistance = 0.0
        lastGpsLocation = null
        lastStepCount = 0
        stepBasedDistance = 0.0
    }

    // Метод для получения расстояния, рассчитанного только по GPS (для отладки)
    fun getGpsBasedDistance(): Double {
        // Для упрощения, возвращаем приблизительное значение
        return (totalDistance * gpsWeight) / 1000.0
    }
}