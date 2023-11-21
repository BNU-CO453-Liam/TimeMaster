package com.timemaster.controller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.timemaster.R
import android.os.CountDownTimer
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit
import java.util.Calendar

class Main : AppCompatActivity() {

    private lateinit var countdownTimer: CountDownTimer
    private lateinit var countdownTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        countdownTextView = findViewById(R.id.countdownTextView)

        val currentTimeMillis = System.currentTimeMillis()
        val endOfDayMillis = getEndOfDayMillis()
        val remainingMillis = endOfDayMillis - currentTimeMillis
        val menu by lazy { Menu(this) }

        countdownTimer = object : CountDownTimer(remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }

            override fun onFinish() {
                // Handle when the countdown is finished (optional)
            }
        }

        countdownTimer.start()

        // Find the Floating Action Buttons
        val fab3: FloatingActionButton = findViewById(R.id.floatingActionButton3)
        val fab4: FloatingActionButton = findViewById(R.id.floatingActionButton4)
        val fab5: FloatingActionButton = findViewById(R.id.floatingActionButton5)
        val fab6: FloatingActionButton = findViewById(R.id.floatingActionButton6)

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

    private fun updateCountdownText(millisUntilFinished: Long) {
        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        countdownTextView.text = formattedTime
    }

    private fun getEndOfDayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}