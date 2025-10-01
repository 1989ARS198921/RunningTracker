package com.example.runningtracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runningtracker.R
import com.example.runningtracker.data.database.RunDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticsActivity : AppCompatActivity() {

    private lateinit var db: RunDatabase
    private lateinit var chart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        db = RunDatabase(this)
        chart = findViewById(R.id.chart)

        val tvWeekly = findViewById<TextView>(R.id.tvWeekly)
        val tvMonthly = findViewById<TextView>(R.id.tvMonthly)
        val tvYearly = findViewById<TextView>(R.id.tvYearly)

        tvWeekly.text = "Неделя: %.2f км".format(db.getWeeklyDistance())
        tvMonthly.text = "Месяц: %.2f км".format(db.getMonthlyDistance())
        tvYearly.text = "Год: %.2f км".format(db.getYearlyDistance())

        setupChart()
    }

    private fun setupChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, db.getWeeklyDistance().toFloat()))
        entries.add(BarEntry(1f, db.getMonthlyDistance().toFloat()))
        entries.add(BarEntry(2f, db.getYearlyDistance().toFloat()))

        val labels = listOf("Неделя", "Месяц", "Год")

        val dataSet = BarDataSet(entries, "Расстояние (км)")
        dataSet.color = Color.BLUE // или цвет из Material 3: Color.parseColor("#6200EE")
        dataSet.valueTextSize = 14f

        val barData = BarData(dataSet)
        chart.data = barData

        // Подписи по оси X
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }

        // Подписи по оси Y
        chart.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
        }
        chart.axisRight.isEnabled = false

        // Легенда
        chart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }

        // Описание
        chart.description.text = "Статистика за периоды"
        chart.description.textSize = 12f

        // Анимация
        chart.animateY(2000)

        chart.invalidate() // перерисовать
    }
}