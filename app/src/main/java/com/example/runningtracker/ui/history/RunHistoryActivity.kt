package com.example.runningtracker.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningtracker.R
import com.example.runningtracker.data.database.RunDatabase
import com.example.runningtracker.data.model.Run

class RunHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RunAdapter
    private lateinit var db: RunDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_history) // ✅ Убедись, что layout существует

        recyclerView = findViewById(R.id.recyclerView) // ✅ Убедись, что ID совпадает
        db = RunDatabase(this)

        val runs = db.getAllRuns()
        adapter = RunAdapter(runs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}