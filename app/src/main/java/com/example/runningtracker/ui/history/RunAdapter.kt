package com.example.runningtracker.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runningtracker.R
import com.example.runningtracker.data.model.Run
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter(private val runs: List<Run>) : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    class RunViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvCalories: TextView = view.findViewById(R.id.tvCalories)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_run, parent, false)
        return RunViewHolder(view)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = runs[position]

        holder.tvDistance.text = "Дистанция: %.2f км".format(run.distance)
        holder.tvTime.text = "Время: ${run.time}"
        holder.tvCalories.text = "Калории: ${run.calories}"

        val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(run.timestamp))
        holder.tvDate.text = date
    }

    override fun getItemCount() = runs.size
}