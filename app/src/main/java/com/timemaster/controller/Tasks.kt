package com.timemaster.controller

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R
import com.timemaster.adapter.TasksAdapter
import com.timemaster.model.Task
import com.timemaster.model.TaskDbHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Tasks : AppCompatActivity() {

    private lateinit var taskAdapter: TasksAdapter
    private lateinit var taskList: MutableList<Task>
    private lateinit var timerHandler: Handler

    private lateinit var taskNameEditText: EditText
    private lateinit var taskRecyclerView: RecyclerView

    private lateinit var taskDbHelper: TaskDbHelper

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks)

        taskRecyclerView = findViewById(R.id.taskRecyclerView) // Updated to RecyclerView

        taskList = mutableListOf()

        taskDbHelper = TaskDbHelper(this)

        // Create TasksAdapter with onDeleteClickListener
        taskAdapter = TasksAdapter(this, taskList, taskDbHelper) { position ->
            // Handle onDeleteClickListener
            deleteTask(position)
        }

        // Updated: Set up RecyclerView
        taskRecyclerView.layoutManager = LinearLayoutManager(this)
        taskRecyclerView.adapter = taskAdapter

        timerHandler = Handler(Looper.getMainLooper())

        // Find the Floating Action Buttons
        val fab4: FloatingActionButton = findViewById(R.id.floatingActionButton4)
        val fab5: FloatingActionButton = findViewById(R.id.floatingActionButton5)
        val fab6: FloatingActionButton = findViewById(R.id.floatingActionButton6)
        val menu by lazy { Menu(this) }

        // Set click listeners for each button
        fab4.setOnClickListener { openActivity(Metrics::class.java) }
        fab5.setOnClickListener { openActivity(Performance::class.java) }
        fab6.setOnClickListener { menu.showPopupMenu(it) }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Handle swipe action
                deleteTask(position)
            }
        })

        itemTouchHelper.attachToRecyclerView(taskRecyclerView)
    }

    private fun loadTasks() {
        taskList.clear()
        taskList.addAll(taskDbHelper.getAllTasks())
        taskList.reverse()
        taskAdapter.notifyDataSetChanged()
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    fun addTask(view: View) {
        val taskName = taskNameEditText.text.toString()
        if (taskName.isNotEmpty()) {
            val newTask = Task(taskName)
            // Set initial values for startTime and endTime
            newTask.startTime = System.currentTimeMillis()
            newTask.endTime = System.currentTimeMillis()
            taskDbHelper.addTask(newTask)
            loadTasks() // Reload tasks from the database
            taskNameEditText.text.clear()
            taskRecyclerView.visibility = View.VISIBLE
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

        task.startTime = System.currentTimeMillis()
        taskDbHelper.updateTask(task)

        timerHandler.postDelayed(object : Runnable {
            override fun run() {
                taskAdapter.updateTimers()
                timerHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun pauseTimer(task: Task) {
        task.isRunning = false
        task.endTime = System.currentTimeMillis()

        // Calculate the duration in seconds
        val durationSeconds = (task.endTime - task.startTime) / 1000

        task.duration += (durationSeconds * 1000)

        // Update the task in the database
        taskDbHelper.updateTask(task)
        loadTasks() // Reload tasks from the database
    }

    fun deleteTask(position: Int) {
        if (position < taskList.size) {
            val deletedTask = taskList.removeAt(position)
            if (deletedTask.isRunning) {
                pauseTimer(deletedTask)
            }

            // Remove the task from the database
            taskDbHelper.deleteTask(deletedTask.id)

            // Stop and remove the timer associated with the task
            stopAndRemoveTimer(position)

            // Notify the adapter about the removal
            taskAdapter.notifyItemRemoved(position)
            taskAdapter.notifyItemRangeChanged(position, taskList.size) // Refresh the items after the removed position
        }
    }

    private fun stopAndRemoveTimer(position: Int) {
        val handler = taskAdapter.getTimerHandler(position)
        handler?.removeCallbacksAndMessages(null)
        taskAdapter.removeTimer(position)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the task list in case of configuration changes
        outState.putParcelableArrayList("taskList", ArrayList(taskList))
    }

    override fun onResume() {
        super.onResume()
        // Reload tasks from the database whenever the activity is resumed
        loadTasks()
        // Resume timers for running tasks
        resumeTimers()
    }

    private fun resumeTimers() {
        taskList.filter { it.isRunning }.forEach { startTimer(it) }
    }

    fun openDialog(view: View) {
        val dialogView = LayoutInflater.from(view.context).inflate(R.layout.task_dialog, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.dialogTaskNameEditText)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.dialogDailyTargetTimePicker)
        val saveButton = dialogView.findViewById<Button>(R.id.dialogSaveButton)

        // Set 24-hour format for the TimePicker
        timePicker.setIs24HourView(true)
        timePicker.hour = 1
        timePicker.minute = 0

        val dialog = AlertDialog.Builder(view.context)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val taskName = taskNameEditText.text.toString()

            // Get the selected hour and minute from TimePicker
            val selectedHour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour
            } else {
                timePicker.currentHour
            }
            val selectedMinute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.minute
            } else {
                timePicker.currentMinute
            }

            // update task time to db
            if (taskName.isNotEmpty()) {
                val newTask = Task(taskName)

                // Set initial values for startTime and endTime
                newTask.startTime = System.currentTimeMillis()
                newTask.endTime = System.currentTimeMillis()

                // Calculate the time in milliseconds
                val selectedTimeInMillis = (selectedHour * 60 + selectedMinute) * 60 * 1000L

                // Set the daily target time
                newTask.dailyTargetTime = selectedTimeInMillis

                taskDbHelper.addTask(newTask)
                loadTasks() // Reload tasks from the database
                dialog.dismiss() // Dismiss the dialog after saving
            }
        }

        dialog.show()
    }
}