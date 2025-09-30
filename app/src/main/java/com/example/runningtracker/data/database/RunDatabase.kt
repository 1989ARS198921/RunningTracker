package com.example.runningtracker.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.runningtracker.data.model.Run

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
}