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
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R

class Metrics : AppCompatActivity() {

    private lateinit var doughnutPieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.metrics)

        doughnutPieChart = findViewById(R.id.doughnutPieChart)
        configureDoughnutChart()
        animateChart()

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
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    private fun configureDoughnutChart() {
        // Sample data for the doughnut pie chart
        val entries = listOf(
            PieEntry(25f, "Slice 1"),
            PieEntry(30f, "Slice 2"),
            PieEntry(15f, "Slice 3"),
            PieEntry(30f, "Slice 4")
        )

        val dataSet = PieDataSet(entries, "Doughnut Chart")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val pieData = PieData(dataSet)
        doughnutPieChart.data = pieData

        // Customize the pie chart as a doughnut
        doughnutPieChart.isDrawHoleEnabled = true
        doughnutPieChart.setHoleColor(Color.WHITE)
        doughnutPieChart.setDrawEntryLabels(false)
        doughnutPieChart.setEntryLabelTextSize(12f)
        doughnutPieChart.setEntryLabelColor(Color.BLACK)
    }

    private fun animateChart(duration: Long = 1400, easing: Easing.EasingFunction = Easing.EaseInOutQuad) {
        doughnutPieChart.animateY(duration.toInt(), easing)
    }
}