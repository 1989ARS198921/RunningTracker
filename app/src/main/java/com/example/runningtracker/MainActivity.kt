package com.example.runningtracker.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.runningtracker.R
import com.example.runningtracker.data.database.RunDatabase
import com.example.runningtracker.data.model.Run
import com.example.runningtracker.ui.history.RunHistoryActivity
import com.example.runningtracker.utils.Constants
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvCalories: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnSave: Button
    private lateinit var btnHistory: Button

    private lateinit var locationManager: LocationManager
    private var isTracking = false
    private var totalDistance = 0.0
    private var startTime = 0L
    private var calories = 0
    private val weight = 70f // вес в кг

    private val locationListener = object : LocationListener {
        private var lastLocation: Location? = null

        override fun onLocationChanged(location: Location) {
            lastLocation?.let { oldLocation ->
                val distanceInMeters = oldLocation.distanceTo(location)
                totalDistance += distanceInMeters / 1000.0 // в км
                updateUI()
            }
            lastLocation = location
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

        btnStart.setOnClickListener { startTracking() }
        btnStop.setOnClickListener { stopTracking() }
        btnSave.setOnClickListener { saveRun() }
        btnHistory.setOnClickListener {
            startActivity(Intent(this, RunHistoryActivity::class.java))
        }
    }

    private fun initViews() {
        tvDistance = findViewById(R.id.tvDistance)
        tvTime = findViewById(R.id.tvTime)
        tvCalories = findViewById(R.id.tvCalories)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnSave = findViewById(R.id.btnSave)
        btnHistory = findViewById(R.id.btnHistory)

        btnSave.isEnabled = false
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

        isTracking = true
        startTime = System.currentTimeMillis()
        btnStart.isEnabled = false
        btnStop.isEnabled = true
        startTimer()
    }

    private fun stopTracking() {
        locationManager.removeUpdates(locationListener)
        isTracking = false
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        btnSave.isEnabled = true
    }

    private fun updateUI() {
        tvDistance.text = "Дистанция: %.2f км".format(totalDistance)

        val elapsed = System.currentTimeMillis() - startTime
        val seconds = (elapsed / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        val sec = seconds % 60
        val min = minutes % 60

        tvTime.text = "Время: %02d:%02d:%02d".format(hours, min, sec)

        calories = (totalDistance * Constants.CALORIES_PER_KM).roundToInt()
        tvCalories.text = "Калории: $calories"
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
            distance = totalDistance,
            time = tvTime.text.toString().substring(6),
            calories = calories
        )
        db.addRun(run)

        Toast.makeText(this, "Пробежка сохранена", Toast.LENGTH_SHORT).show()
    }
}