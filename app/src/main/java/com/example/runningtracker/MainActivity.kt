package com.example.runningtracker.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.runningtracker.R
import com.example.runningtracker.data.database.RunDatabase
import com.example.runningtracker.data.model.Run
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

    private var stepCount = 0
    private var stepSensor: Sensor? = null
    private var stepCounter: SensorEventListener? = null
    private lateinit var hybridCalculator: HybridDistanceCalculator

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            hybridCalculator.addGpsLocation(location)
            updateUI()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private lateinit var db: RunDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        db = RunDatabase(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hybridCalculator = HybridDistanceCalculator(stepLength = 0.75)

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            5f,
            locationListener
        )

        initStepCounter()

        isTracking = true
        startTime = System.currentTimeMillis()
        setButtonEnabled(btnStart, false)
        setButtonEnabled(btnStop, true)
        setButtonEnabled(btnSave, false)

        startTimer()
    }

    private fun stopTracking() {
        locationManager.removeUpdates(locationListener)

        stepSensor?.let {
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(stepCounter, it)
        }

        isTracking = false
        setButtonEnabled(btnStart, true)
        setButtonEnabled(btnStop, false)
        setButtonEnabled(btnSave, true)

        // Обновим UI один раз после остановки
        updateUI()
    }

    private fun initStepCounter() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepCounter = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val newStepCount = event.values[0].toInt()
                    hybridCalculator.updateSteps(newStepCount)
                    updateUI()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        stepSensor?.let {
            sensorManager.registerListener(stepCounter, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun updateUI() {
        val distance = hybridCalculator.getTotalDistance()
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
    }

    private fun startTimer() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (isTracking) {
                    updateUI()
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun saveRun() {
        val run = Run(
            distance = hybridCalculator.getTotalDistance(),
            time = tvTime.text.toString().substring(6),
            calories = calories
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
                hybridCalculator.reset()
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
}