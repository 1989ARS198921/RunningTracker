package com.example.runningtracker.utils

import android.location.Location
import kotlin.math.max

class HybridDistanceCalculator(
    private val stepLength: Double = 0.75, // длина шага в метрах
    private val gpsWeight: Double = 0.7,   // вес GPS (0.7 = 70% от GPS, 30% от шагов)
    private val minStepThreshold: Int = 5  // минимальное количество шагов для учёта
) {
    private var totalDistance = 0.0
    private var lastGpsLocation: Location? = null
    private var lastStepCount = 0
    private var stepBasedDistance = 0.0

    fun addGpsLocation(location: Location) {
        lastGpsLocation?.let { lastLocation ->
            if (location.hasAccuracy() && location.accuracy < 20) {
                val distance = lastLocation.distanceTo(location).toDouble()
                if (distance > 5) { // игнорировать шум
                    totalDistance += distance * gpsWeight
                }
            }
        }
        lastGpsLocation = location
    }

    fun updateSteps(stepCount: Int) {
        val stepsDelta = max(0, stepCount - lastStepCount)
        if (stepsDelta >= minStepThreshold) {
            val stepDistance = stepsDelta * stepLength
            stepBasedDistance += stepDistance
            totalDistance += stepDistance * (1 - gpsWeight)
        }
        lastStepCount = stepCount
    }

    fun getTotalDistance(): Double = totalDistance / 1000.0 // в км

    fun reset() {
        totalDistance = 0.0
        lastGpsLocation = null
        lastStepCount = 0
        stepBasedDistance = 0.0
    }
}