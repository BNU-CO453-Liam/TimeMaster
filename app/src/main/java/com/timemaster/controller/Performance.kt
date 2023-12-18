package com.timemaster.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R
import com.timemaster.model.Task
import com.timemaster.model.TaskDbHelper

class Performance : AppCompatActivity() {

    private val taskNames = mutableListOf<String>()

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
        val entries2 = mutableListOf<BarEntry>()

        taskNames.clear()

        tasks.forEachIndexed { index, task ->
            val taskName = task.name
            taskNames.add(taskName)
            // Convert duration to minutes for simplicity
            val durationInMillis = task.duration.toFloat()
            val targetDurationInMillis = task.dailyTargetTime.toFloat()

            // Create a BarEntry with duration and task index
            entries.add(BarEntry(index.toFloat(), durationInMillis))
            entries2.add(BarEntry(index.toFloat(), targetDurationInMillis))
        }

        // Create a BarDataSet with the entries
        val dataSet = BarDataSet(entries, "Current Durations")
        dataSet.setDrawValues(false)

        val dataSet2 = BarDataSet(entries2, "")
        dataSet2.setDrawValues(false)

        dataSet2.barBorderColor = Color.rgb(127, 231, 254)
        dataSet2.barBorderWidth = 2f
        dataSet2.color = Color.TRANSPARENT

        val barData = BarData(dataSet2, dataSet)

        // Customize axes
        setupXAxis(barChart.xAxis, tasks)
        setupYAxis(barChart.axisLeft, tasks)
        setupRightAxis(barChart.axisRight)
        setupLeftAxis(barChart.axisLeft)

        // Set BarData to BarChart
        barChart.data = barData

        // Customize other attributes
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(true)
        barChart.setPinchZoom(true)
        barChart.barData.barWidth = 0.9f
        barChart.setFitBars(true)
        barChart.legend.isEnabled = false

        // Set up the click listener
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                // Handle the click event
                if (e != null) {
                    val index = e.x.toInt()
                    val taskName = taskNames.getOrNull(index)
                    val currentDuration = entries.getOrNull(index)?.y?.toInt() ?:0
                    val targetDuration = entries2.getOrNull(index)?.y?.toInt() ?: 0

                    val displayTarget = formatTime(targetDuration.toLong())
                    val displayCurrent = formatTime(currentDuration.toLong())

                    val message = "Task: $taskName\nDaily Target: $displayTarget\nCurrent Duration: $displayCurrent"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onNothingSelected() {
                // do nothing
            }
        })

        barChart.invalidate()
    }

    fun formatTime(milliseconds: Long): String {
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((milliseconds % (1000 * 60)) / 1000).toInt()

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    class YAxisDurationFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val hours = (value / 1000 / 60 / 60).toInt()
            val minutes = (value / 1000 / 60).toInt()

            val formattedHours = hours.toString().padStart(2, '0')
            val formattedMinutes = (minutes % 60).toString().padStart(2, '0')
            return "$formattedHours:$formattedMinutes"
        }
    }

    private fun setupXAxis(xAxis: XAxis, tasks: List<Task>) {
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(true)// Show X-axis line at the bottom
        xAxis.setDrawGridLines(false) // Hide vertical grid lines

        val taskNames = tasks.map { it.name }

        xAxis.valueFormatter = IndexAxisValueFormatter(taskNames)
        xAxis.textColor = Color.WHITE
    }

    private fun setupYAxis(yAxis: YAxis, tasks: List<Task>) {
        yAxis.setDrawGridLines(false)
        // Display the Y-axis on the left side only
        yAxis.axisMinimum = 0f

        val fiveHours = 5 * 60 * 1000 // sixteen hours in milliseconds

        yAxis.axisMaximum = fiveHours.toFloat()
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxis.setDrawAxisLine(true) // Disable drawing the Y-axis line on the right side
        yAxis.valueFormatter = YAxisDurationFormatter()
        yAxis.textColor = Color.rgb(200, 200, 200)
        yAxis.setLabelCount(6, true)
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
