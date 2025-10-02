// File: app/src/main/java/com/example/runningtracker/utils/HybridDistanceCalculator.kt
package com.example.runningtracker.utils

import android.location.Location
import kotlin.math.max

class HybridDistanceCalculator(
    private val stepLength: Double = 0.75, // длина шага в метрах
    private val gpsWeight: Double = 0.7,   // вес GPS (0.7 = 70% от GPS, 30% от шагов)
    private val minStepThreshold: Int = 1  // Уменьшено! минимальное количество шагов для учёта
) {
    private var totalDistance = 0.0 // Теперь в метрах для внутреннего расчета
    private var lastGpsLocation: Location? = null
    private var lastStepCount = 0
    private var stepBasedDistance = 0.0 // Расстояние только от шагов в метрах

    fun addGpsLocation(location: Location) {
        lastGpsLocation?.let { lastLocation ->
            // Проверяем точность (необязательно, но может помочь)
            if (location.hasAccuracy() && location.accuracy < 20) {
                val distance = lastLocation.distanceTo(location).toDouble()
                // Уменьшаем порог для GPS с 1 метра до 0.1 метра (или даже 0, если хотите учитывать все изменения)
                if (distance > 0.1) { // <-- Уменьшенный порог
                    // Добавляем расстояние, взвешенное по GPS
                    totalDistance += distance * gpsWeight
                }
            }
        }
        lastGpsLocation = location
    }

    fun updateSteps(stepCount: Int) {
        val stepsDelta = max(0, stepCount - lastStepCount)
        // Порог шагов теперь 1, так как minStepThreshold = 1
        if (stepsDelta >= minStepThreshold) { // Это условие теперь будет выполняться чаще
            val stepDistance = stepsDelta * stepLength
            stepBasedDistance += stepDistance
            // Добавляем расстояние, взвешенное по шагам (остаток от 1 - gpsWeight)
            totalDistance += stepDistance * (1 - gpsWeight)
        }
        lastStepCount = stepCount
    }

    fun getTotalDistance(): Double = totalDistance / 1000.0 // возвращаем в км

    fun reset() {
        totalDistance = 0.0
        lastGpsLocation = null
        lastStepCount = 0
        stepBasedDistance = 0.0
    }

    // Метод для получения расстояния, рассчитанного только по шагам (для отладки)
    fun getStepBasedDistance(): Double = stepBasedDistance / 1000.0

    // Метод для получения расстояния, рассчитанного только по GPS (для отладки)
    fun getGpsBasedDistance(): Double {
        // Для упрощения, возвращаем приблизительное значение
        return (totalDistance * gpsWeight) / 1000.0
    }
}