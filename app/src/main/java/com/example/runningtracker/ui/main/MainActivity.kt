// File: app/src/main/java/com/example/runningtracker/ui/main/MainActivity.kt
package com.example.runningtracker.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.runningtracker.R
import com.example.runningtracker.data.database.RunDatabase
import com.example.runningtracker.data.model.Run
import com.example.runningtracker.service.TrackingService
import com.example.runningtracker.ui.history.RunHistoryActivity
import com.example.runningtracker.ui.statistics.StatisticsActivity
import com.example.runningtracker.utils.Constants
import com.example.runningtracker.utils.HybridDistanceCalculator
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvMaxSpeed: TextView
    private lateinit var tvAvgSpeed: TextView
    private lateinit var btnStart: MaterialCardView
    private lateinit var btnStop: MaterialCardView
    private lateinit var btnSave: MaterialCardView
    private lateinit var btnHistory: MaterialCardView
    private lateinit var btnStats: MaterialCardView
    private lateinit var btnResetTracker: MaterialCardView

    private lateinit var locationManager: LocationManager
    private var isTracking = false
    private var startTime = 0L
    private var calories = 0
    private var totalDistance = 0.0
    private var maxSpeed = 0.0
    private var avgSpeed = 0.0
    private val weight = 70f // вес в кг

    private lateinit var hybridCalculator: HybridDistanceCalculator

    // --- ИЗМЕНЕНО: SharedPreferences и ключи ---
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "TrackingPrefs"
    private val DISTANCE_KEY = "current_distance"
    private val STEPS_KEY = "current_steps" // <-- ДОБАВЛЕНО
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    private lateinit var db: RunDatabase

    // --- ИЗМЕНЕНО: Handler и Runnable для периодического обновления ---
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                updateUIFromSharedPrefs() // --- ВЫЗОВ НОВОГО МЕТОДА ---
                handler.postDelayed(this, 1000) // Обновляем каждую секунду
            }
        }
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- ИЗМЕНЕНО: инициализация SharedPreferences ---
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---

        initViews()
        db = RunDatabase(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // hybridCalculator больше не используется для расчёта дистанции в UI при использовании SharedPreferences
        // Но может использоваться для других целей или как резервный вариант
        // hybridCalculator = HybridDistanceCalculator(stepLength = 0.75) // Можно закомментировать, если не используется

        // Анимации кнопок
        btnStart.setOnClickListener {
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            btnStart.startAnimation(pressAnim)
            startTracking()
        }

        btnStop.setOnClickListener {
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            btnStop.startAnimation(pressAnim)
            stopTracking()
        }

        btnSave.setOnClickListener {
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            btnSave.startAnimation(pressAnim)
            saveRun()
        }

        btnHistory.setOnClickListener {
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            btnHistory.startAnimation(pressAnim)
            startActivity(Intent(this, RunHistoryActivity::class.java))
        }

        btnStats.setOnClickListener {
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            btnStats.startAnimation(pressAnim)
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnResetTracker.setOnClickListener {
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            btnResetTracker.startAnimation(pressAnim)
            resetTracker()
        }
    }

    private fun initViews() {
        tvDistance = findViewById(R.id.tvDistance)
        tvTime = findViewById(R.id.tvTime)
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed)
        tvAvgSpeed = findViewById(R.id.tvAvgSpeed)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnSave = findViewById(R.id.btnSave)
        btnHistory = findViewById(R.id.btnHistory)
        btnStats = findViewById(R.id.btnStats)
        btnResetTracker = findViewById(R.id.btnResetTracker)

        // Отключаем кнопку "Сохранить" по умолчанию
        setButtonEnabled(btnSave, false)
    }

    private fun setButtonEnabled(cardView: MaterialCardView, enabled: Boolean) {
        cardView.isEnabled = enabled
        val alpha = if (enabled) 1.0f else 0.5f
        cardView.alpha = alpha
    }

    private fun startTracking() {
        Log.d("MainActivity", "startTracking вызван")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val intent = Intent(this, TrackingService::class.java)
        startService(intent)
        Log.d("MainActivity", "TrackingService запущен")

        isTracking = true
        startTime = System.currentTimeMillis()
        setButtonEnabled(btnStart, false)
        setButtonEnabled(btnStop, true)
        setButtonEnabled(btnSave, false)

        Log.d("MainActivity", "requestLocationUpdates НЕ вызван в MainActivity")

        startTimer()
        // --- ИЗМЕНЕНО: запуск обновления из SharedPreferences ---
        handler.post(updateRunnable)
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    }

    private fun stopTracking() {
        Log.d("MainActivity", "stopTracking вызван")
        val intent = Intent(this, TrackingService::class.java)
        stopService(intent)
        Log.d("MainActivity", "TrackingService остановлен")

        isTracking = false
        // --- ИЗМЕНЕНО: остановка обновления из SharedPreferences ---
        handler.removeCallbacks(updateRunnable)
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        setButtonEnabled(btnStart, true)
        setButtonEnabled(btnStop, false)
        setButtonEnabled(btnSave, true)

        // Обновим UI один раз после остановки
        updateUI()
    }

    // --- ИЗМЕНЕНО: метод для обновления UI из SharedPreferences ---
    private fun updateUIFromSharedPrefs() {
        val distance = sharedPreferences.getFloat(DISTANCE_KEY, 0f).toDouble()
        // val steps = sharedPreferences.getInt(STEPS_KEY, 0) // <-- ЧИТАЕМ ШАГИ, если нужно отображать в UI
        Log.d("MainActivity", "updateUIFromSharedPrefs вызван, расстояние из SharedPreferences: $distance")
        // Используем расстояние из SharedPreferences
        val distanceKm = (distance * 1000).toInt() // в метрах
        val km = distanceKm / 1000
        val meters = distanceKm % 1000
        tvDistance.text = "Диста: %02d км %03d м".format(km, meters)

        // Обновляем остальные UI элементы (время, калории, скорости)
        val elapsed = System.currentTimeMillis() - startTime
        val seconds = (elapsed / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        val sec = seconds % 60
        val min = minutes % 60

        val timeStr = "Время: %02d:%02d:%02d".format(hours, min, sec)
        tvTime.text = timeStr

        calories = (distance * Constants.CALORIES_PER_KM).roundToInt()

        if (elapsed > 0) {
            avgSpeed = distance / (elapsed / 1000.0 / 3600.0) // км/ч
        }
        tvMaxSpeed.text = "Макс: %02.0f км/ч".format(maxSpeed)
        tvAvgSpeed.text = "Сред: %02.0f км/ч".format(avgSpeed)

        Log.d("MainActivity", "UI обновлен из SharedPreferences, расстояние: $distance")
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    private fun updateUI() {
        Log.d("MainActivity", "updateUI вызван (локальное обновление)")
        // Используем локальное расстояние, если broadcast не приходит или не содержит данных
        // или если не используется SharedPreferences для обновления во время трекинга
        val distance = sharedPreferences.getFloat(DISTANCE_KEY, 0f).toDouble() // Используем SharedPreferences для обновления в конце
        val distanceKm = (distance * 1000).toInt() // в метрах
        val km = distanceKm / 1000
        val meters = distanceKm % 1000
        tvDistance.text = "Диста: %02d км %03d м".format(km, meters)

        val elapsed = System.currentTimeMillis() - startTime
        val seconds = (elapsed / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        val sec = seconds % 60
        val min = minutes % 60

        val timeStr = "Время: %02d:%02d:%02d".format(hours, min, sec)
        tvTime.text = timeStr

        calories = (distance * Constants.CALORIES_PER_KM).roundToInt()

        // Рассчитаем скорости (примерно)
        if (elapsed > 0) {
            avgSpeed = distance / (elapsed / 1000.0 / 3600.0) // км/ч
        }
        tvMaxSpeed.text = "Макс: %02.0f км/ч".format(maxSpeed)
        tvAvgSpeed.text = "Сред: %02.0f км/ч".format(avgSpeed)

        Log.d("MainActivity", "UI обновлен локально, расстояние: $distance")
    }

    private fun startTimer() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (isTracking) {
                    Log.d("MainActivity", "Таймер обновления UI (только время)")
                    // Таймер обновляет только время, если расстояние приходит от сервиса
                    // Обновляем UI, чтобы время обновлялось
                    val elapsed = System.currentTimeMillis() - startTime
                    val seconds = (elapsed / 1000).toInt()
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val sec = seconds % 60
                    val min = minutes % 60

                    val timeStr = "Время: %02d:%02d:%02d".format(hours, min, sec)
                    tvTime.text = timeStr
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun saveRun() {
        val distanceToSave = sharedPreferences.getFloat(DISTANCE_KEY, 0f).toDouble() // Используем расстояние из SharedPreferences
        val stepsToSave = sharedPreferences.getInt(STEPS_KEY, 0) // <-- ЧИТАЕМ ШАГИ
        val run = Run(
            distance = distanceToSave,
            time = tvTime.text.toString().substring(6), // Извлекаем только время
            calories = calories,
            steps = stepsToSave // <-- ПЕРЕДАЁМ ШАГИ В МОДЕЛЬ
        )
        db.addRun(run)

        Toast.makeText(this, "Пробежка сохранена", Toast.LENGTH_SHORT).show()
    }

    private fun resetTracker() {
        if (isTracking) {
            Toast.makeText(this, "Нельзя сбросить во время трекинга", Toast.LENGTH_SHORT).show()
            return
        }

        // Анимация исчезновения
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast)

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Обнуляем значения
                // hybridCalculator.reset() // Больше не используется для UI при использовании SharedPreferences
                startTime = 0L
                calories = 0
                maxSpeed = 0.0
                avgSpeed = 0.0
                updateUI()

                // Анимация появления
                val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in_fast)
                tvDistance.startAnimation(fadeIn)
                tvTime.startAnimation(fadeIn)
                tvMaxSpeed.startAnimation(fadeIn)
                tvAvgSpeed.startAnimation(fadeIn)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        tvDistance.startAnimation(fadeOut)
        tvTime.startAnimation(fadeOut)
        tvMaxSpeed.startAnimation(fadeOut)
        tvAvgSpeed.startAnimation(fadeOut)
    }

    override fun onResume() {
        super.onResume()
        // Более не используем BroadcastReceiver
        Log.d("MainActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        // Более не используем BroadcastReceiver
        Log.d("MainActivity", "onPause")
    }
}