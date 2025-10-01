package com.example.runningtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.runningtracker.R
import com.example.runningtracker.ui.main.MainActivity
import com.example.runningtracker.utils.HybridDistanceCalculator

class TrackingService : Service() {

    private lateinit var locationManager: LocationManager
    private var isTracking = false
    private lateinit var hybridCalculator: HybridDistanceCalculator
    private var lastLocation: Location? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            hybridCalculator.addGpsLocation(location)
            lastLocation = location

            // Отправка broadcast с обновлением местоположения
            val broadcastIntent = Intent("LOCATION_UPDATE").apply {
                putExtra("distance", hybridCalculator.getTotalDistance())
                putExtra("location", location)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sendBroadcast(broadcastIntent, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                @Suppress("DEPRECATION")
                sendBroadcast(broadcastIntent)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate() {
        super.onCreate()
        hybridCalculator = HybridDistanceCalculator(stepLength = 0.75)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification().build())
        startTracking()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Проверка разрешений перед запросом обновлений местоположения
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                5f,
                locationListener
            )
            isTracking = true
        } else {
            // Остановка сервиса если нет разрешений
            stopSelf()
        }
    }

    private fun stopTracking() {
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
        isTracking = false
        stopSelf()
    }

    private fun createNotification(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, pendingIntentFlags
        )

        return NotificationCompat.Builder(this, "TRACKING_CHANNEL")
            .setContentTitle("Отслеживание пробежки")
            .setContentText("Дистанция: 0.00 км")
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true) // Уведомление только при первом показе
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TRACKING_CHANNEL",
                "Отслеживание пробежки",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Канал для уведомлений во время отслеживания пробежки"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }
}