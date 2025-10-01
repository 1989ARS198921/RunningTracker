package com.example.runningtracker.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.runningtracker.data.model.Run
import java.text.SimpleDateFormat
import java.util.*

class RunDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "runs.db"
        private const val DB_VERSION = 1

        private const val TABLE_NAME = "runs"
        private const val COL_ID = "id"
        private const val COL_DISTANCE = "distance"
        private const val COL_TIME = "time"
        private const val COL_CALORIES = "calories"
        private const val COL_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DISTANCE REAL,
                $COL_TIME TEXT,
                $COL_CALORIES INTEGER,
                $COL_TIMESTAMP INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addRun(run: Run) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DISTANCE, run.distance)
            put(COL_TIME, run.time)
            put(COL_CALORIES, run.calories)
            put(COL_TIMESTAMP, run.timestamp)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllRuns(): List<Run> {
        val runs = mutableListOf<Run>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COL_TIMESTAMP DESC")

        cursor.use {
            while (it.moveToNext()) {
                runs.add(Run(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    distance = it.getDouble(it.getColumnIndexOrThrow(COL_DISTANCE)),
                    time = it.getString(it.getColumnIndexOrThrow(COL_TIME)),
                    calories = it.getInt(it.getColumnIndexOrThrow(COL_CALORIES)),
                    timestamp = it.getLong(it.getColumnIndexOrThrow(COL_TIMESTAMP))
                ))
            }
        }
        db.close()
        return runs
    }

    // Новые методы для статистики
    fun getWeeklyDistance(): Double {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return getDistanceByTimeRange(weekAgo, System.currentTimeMillis())
    }

    fun getMonthlyDistance(): Double {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val monthAgo = cal.timeInMillis
        return getDistanceByTimeRange(monthAgo, System.currentTimeMillis())
    }

    fun getYearlyDistance(): Double {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        val yearAgo = cal.timeInMillis
        return getDistanceByTimeRange(yearAgo, System.currentTimeMillis())
    }

    private fun getDistanceByTimeRange(start: Long, end: Long): Double {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf("SUM($COL_DISTANCE) AS total_distance"),
            "$COL_TIMESTAMP BETWEEN ? AND ?",
            arrayOf(start.toString(), end.toString()),
            null, null, null
        )

        var total = 0.0
        cursor.use {
            if (it.moveToFirst()) {
                total = it.getDouble(0)
            }
        }
        db.close()
        return total
    }

    // Метод для сброса текущего расстояния
    fun resetCurrentDistance() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null) // Удаляет все записи
        db.close()
    }
    // В файле data/database/RunDatabase.kt

    fun getDistanceByDays(days: Int = 7): List<Double> {
        val db = readableDatabase
        val result = mutableListOf<Double>()
        val calendar = Calendar.getInstance()

        for (i in 0 until days) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000L) - 1 // Конец дня

            val cursor = db.query(
                TABLE_NAME,
                arrayOf("SUM($COL_DISTANCE) AS total_distance"),
                "$COL_TIMESTAMP BETWEEN ? AND ?",
                arrayOf(startOfDay.toString(), endOfDay.toString()),
                null, null, null
            )

            var distance = 0.0
            cursor.use {
                if (it.moveToFirst()) {
                    distance = it.getDouble(0)
                }
            }
            result.add(0, distance) // Добавляем в начало, чтобы даты шли по возрастанию
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Вернуть к текущему дню
        }

        return result
    }
}