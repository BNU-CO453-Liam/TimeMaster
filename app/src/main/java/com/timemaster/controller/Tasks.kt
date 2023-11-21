package com.timemaster.controller

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R
import com.timemaster.adapter.TasksAdapter
import com.timemaster.model.Task

class Tasks : AppCompatActivity() {

    private lateinit var taskAdapter: TasksAdapter
    private lateinit var taskList: MutableList<Task>
    private lateinit var timerHandler: Handler
    private lateinit var timerRunnable: Runnable

    private lateinit var taskNameEditText: EditText
    private lateinit var taskListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks)

        taskNameEditText = findViewById(R.id.taskNameEditText)
        taskListView = findViewById(R.id.taskListView)

        taskList = mutableListOf()
        taskAdapter = TasksAdapter(this, taskList)
        taskListView.adapter = taskAdapter

        timerHandler = Handler(Looper.getMainLooper())

        timerRunnable = object : Runnable {
            override fun run() {
                taskAdapter.updateTimers()
                timerHandler.postDelayed(this, 1000)
            }
        }

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

    fun addTask(view: View) {
        val taskName = taskNameEditText.text.toString()
        if (taskName.isNotEmpty()) {
            val newTask = Task(taskName)
            taskList.add(newTask)
            taskAdapter.notifyDataSetChanged()
            taskNameEditText.text.clear()

            // Show the taskListView when a task is added
            taskListView.visibility = View.VISIBLE
        }
    }

    fun toggleTimer(position: Int) {
        if (position < taskList.size) {
            val task = taskList[position]
            if (task.isRunning) {
                pauseTimer(task)
            } else {
                startTimer(task)
            }
            taskAdapter.notifyDataSetChanged() // Notify adapter about the data change
        }
    }

    private fun startTimer(task: Task) {
        task.isRunning = true
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    private fun pauseTimer(task: Task) {
        task.isRunning = false
    }

    fun deleteTask(position: Int) {
        if (position < taskList.size) {
            val deletedTask = taskList.removeAt(position)
            if (deletedTask.isRunning) {
                pauseTimer(deletedTask)
            }
            taskAdapter.notifyDataSetChanged()
        }
    }
}
