package com.timemaster.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R
import com.timemaster.model.Task
import com.timemaster.model.TaskDbHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Performance : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.performance)

        // Find the Floating Action Buttons
        val fab3: FloatingActionButton = findViewById(R.id.floatingActionButton3)
        val fab4: FloatingActionButton = findViewById(R.id.floatingActionButton4)
        val fab5: FloatingActionButton = findViewById(R.id.floatingActionButton5)
        val fab6: FloatingActionButton = findViewById(R.id.floatingActionButton6)
        val menu by lazy { Menu(this) }

        // Set click listeners for each button
        fab3.setOnClickListener { openActivity(Tasks::class.java) }
        fab4.setOnClickListener { openActivity(Metrics::class.java) }
        fab5.setOnClickListener { openActivity(Performance::class.java) }
        fab6.setOnClickListener { menu.showPopupMenu(it) }

        // Initialize BarChart
        val barChart: BarChart = findViewById(R.id.barChart)

        // Fetch tasks from the database
        val dbHelper = TaskDbHelper(this)
        val tasks = dbHelper.getAllTasks()

        // Create entries for the BarChart
        val entries = mutableListOf<BarEntry>()

        tasks.forEachIndexed { index, task ->
            // Convert duration to minutes for simplicity (customize as needed)
            val durationInMillis = task.duration.toFloat()

            // Log the values for debugging
            Log.d("PerformanceActivity", "Task: ${task.name}, TaskDuration: ${task.duration}  Duration in minutes: $durationInMillis minutes")

            // Create a BarEntry with duration and task index
            entries.add(BarEntry(index.toFloat(), durationInMillis))
        }

        // Create a BarDataSet with the entries
        val dataSet = BarDataSet(entries, "Task Durations")

        val barData = BarData(dataSet)

        // Customize axes
        setupXAxis(barChart.xAxis, tasks)
        setupYAxis(barChart.axisLeft, tasks)
        setupRightAxis(barChart.axisRight)
        setupLeftAxis(barChart.axisLeft)

        // Set BarData to BarChart
        barChart.data = barData

        // Customize other attributes as needed
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(true)
        barChart.setPinchZoom(true)
        barChart.barData.barWidth = 0.9f
        barChart.setFitBars(true)

        barChart.invalidate()

    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    private class YAxisDurationFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            // Convert the value (assumed to be in milliseconds) to a formatted time string
            val minutes = (value / 1000 / 60).toInt()
            val seconds = (value / 1000 % 60).toInt()

            return String.format("%02d:00", minutes, seconds)
        }
    }

    private fun setupXAxis(xAxis: XAxis, tasks: List<Task>) {
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(false)// Show X-axis line at the bottom
        xAxis.setDrawGridLines(false) // Hide vertical grid lines
    }

    private fun setupYAxis(yAxis: YAxis, tasks: List<Task>) {
        yAxis.setDrawGridLines(false)
        // Display the Y-axis on the left side only
        yAxis.axisMinimum = 0f

        val twntyFourHours = 10 * 60 * 1000

        yAxis.axisMaximum = twntyFourHours.toFloat()
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxis.setDrawAxisLine(true) // Disable drawing the Y-axis line on the right side
        yAxis.valueFormatter = YAxisDurationFormatter()
        yAxis.textColor = Color.rgb(200,200,200)
    }

    private fun setupRightAxis(rightAxis: YAxis) {
        rightAxis.setDrawLabels(false)
        rightAxis.setDrawAxisLine(false) // Hide Y-axis line on the right side
        rightAxis.setDrawGridLines(false)
    }

    private fun setupLeftAxis(leftAxis: YAxis) {
        leftAxis.setDrawLabels(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.setDrawGridLines(false)
        leftAxis.textSize = 15f
    }
}
