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
import java.text.SimpleDateFormat
import java.util.*

class DailyStatsFragment : Fragment() {

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
        val distances = db.getDistanceByDays(7) // Получаем дистанции за последние 7 дней
        val entries = ArrayList<BarEntry>()
        val labels = mutableListOf<String>()

        // Подписи: Пн, Вт, Ср, Чт, Пт, Сб, Вс
        val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

        for (i in distances.indices) {
            entries.add(BarEntry(i.toFloat(), distances[i].toFloat()))
            labels.add(dayNames[i])
        }

        val dataSet = BarDataSet(entries, "Расстояние (км)")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.WHITE // Цвет шрифта значений

        val barData = BarData(dataSet)
        chart.data = barData

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE // Цвет шрифта оси X
            setDrawGridLines(false)
            granularity = 1f
        }

        chart.axisLeft.apply {
            textColor = Color.WHITE // Цвет шрифта оси Y
            axisMinimum = 0f
        }
        chart.axisRight.apply {
            textColor = Color.WHITE
            isEnabled = false
        }

        chart.legend.textColor = Color.WHITE
        chart.description.apply {
            text = "График активности" // Изменяем текст описания
            textColor = Color.WHITE
        }

        chart.animateY(2000)
        chart.invalidate()
    }
}