package com.timemaster.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R
import com.timemaster.model.TaskDbHelper

class Metrics : AppCompatActivity() {

    private lateinit var doughnutPieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.metrics)

        doughnutPieChart = findViewById(R.id.doughnutPieChart)
        configureDoughnutChart()
        animateChart()

        // Fetch task data and update the pie chart
        updatePieChart()

        // Find the Floating Action Buttons
        val fab3: FloatingActionButton = findViewById(R.id.floatingActionButton3)
        val fab5: FloatingActionButton = findViewById(R.id.floatingActionButton5)
        val fab6: FloatingActionButton = findViewById(R.id.floatingActionButton6)
        val menu by lazy { Menu(this) }

        // Set click listeners for each button
        fab3.setOnClickListener { openActivity(Tasks::class.java) }
        fab5.setOnClickListener { openActivity(Performance::class.java) }
        fab6.setOnClickListener { menu.showPopupMenu(it) }
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    private fun configureDoughnutChart() {
        // Fetch tasks from the database
        val dbHelper = TaskDbHelper(this)
        val taskList = dbHelper.getAllTasks()

        // Calculate the total duration from your list of tasks
        val totalDuration = taskList.sumOf { it.duration.toInt() }


        // Create entries for the doughnut pie chart
        val entries = taskList.map { task ->
            PieEntry((task.duration / totalDuration.toFloat()) * 100, task.name)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val pieData = PieData(dataSet)
        doughnutPieChart.data = pieData

        // Customize the pie chart as a doughnut
        doughnutPieChart.isDrawHoleEnabled = true
        doughnutPieChart.setHoleColor(Color.rgb(23,23, 23))
        doughnutPieChart.setDrawEntryLabels(true)
        doughnutPieChart.setEntryLabelTextSize(30f)
        doughnutPieChart.setEntryLabelColor(Color.WHITE)

        doughnutPieChart.setTransparentCircleColor(0)
        doughnutPieChart.setTransparentCircleAlpha(0)

        doughnutPieChart.holeRadius = 60f
        doughnutPieChart.description.isEnabled = false

        // set text for no data
        doughnutPieChart.setNoDataText("No data to display")

        // disable legend
        doughnutPieChart.legend.isEnabled = false
    }

    private fun animateChart(duration: Long = 1400, easing: Easing.EasingFunction = Easing.EaseInOutQuad) {
        doughnutPieChart.animateY(duration.toInt(), easing)
    }

    private fun updatePieChart() {
        // Fetch tasks from the database
        val dbHelper = TaskDbHelper(this)
        val taskList = dbHelper.getAllTasks()

        // Calculate the total duration from your list of tasks
        val totalDuration = taskList.sumBy { it.duration.toInt() }

        // Create entries for the doughnut pie chart
        val entries = taskList.map { task ->
            PieEntry((task.duration / totalDuration.toFloat()) * 100, task.name)
        }

        // customise data text
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 23f

        // set piechart data
        val pieData = PieData(dataSet)
        doughnutPieChart.data = pieData

        // set percentage values to be shown
        pieData.setValueFormatter(PercentFormatter(doughnutPieChart))
        doughnutPieChart.setUsePercentValues(true)

        // Refresh the chart
        doughnutPieChart.invalidate()
    }
}
