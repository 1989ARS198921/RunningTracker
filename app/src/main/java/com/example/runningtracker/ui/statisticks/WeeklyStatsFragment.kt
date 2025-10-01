package com.example.runningtracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.runningtracker.R
import com.example.runningtracker.data.database.RunDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class WeeklyStatsFragment : Fragment() {

    private lateinit var chart: BarChart
    private lateinit var db: RunDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        chart = view.findViewById(R.id.chart)
        db = RunDatabase(requireContext())

        setupChart()
        return view
    }

    private fun setupChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, db.getWeeklyDistance().toFloat()))

        val labels = listOf("Неделя")

        val dataSet = BarDataSet(entries, "Расстояние (км)")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.WHITE

        val barData = BarData(dataSet)
        chart.data = barData

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        chart.axisLeft.apply {
            textColor = Color.WHITE
            axisMinimum = 0f
        }
        chart.axisRight.apply {
            textColor = Color.WHITE
            isEnabled = false
        }

        chart.legend.textColor = Color.WHITE
        chart.description.textColor = Color.WHITE

        chart.animateY(2000)
        chart.invalidate()
    }
}