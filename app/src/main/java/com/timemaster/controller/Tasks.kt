package com.timemaster.controller

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.R
import com.timemaster.adapter.TasksAdapter
import com.timemaster.model.Task
import com.timemaster.model.TaskDbHelper

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

        taskNameEditText = findViewById(R.id.taskNameEditText)
        taskRecyclerView = findViewById(R.id.taskRecyclerView) // Updated to RecyclerView

        taskList = mutableListOf()

        taskDbHelper = TaskDbHelper(this)

        // Create TasksAdapter with onDeleteClickListener
        taskAdapter = TasksAdapter(this, taskList, taskDbHelper) { position ->
            // Handle onDeleteClickListener logic here, e.g., delete the task
            deleteTask(position)
        }

        // Updated: Set up RecyclerView
        taskRecyclerView.layoutManager = LinearLayoutManager(this)
        taskRecyclerView.adapter = taskAdapter

        timerHandler = Handler(Looper.getMainLooper())



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
                // Handle swipe action, e.g., delete the task
                deleteTask(position)
            }
        })

        itemTouchHelper.attachToRecyclerView(taskRecyclerView)
    }

    private fun loadTasks() {
        taskList.clear()
        taskList.addAll(taskDbHelper.getAllTasks())
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
        Log.d("TaskDuration", "Pause Timer function called")
        task.isRunning = false
        task.endTime = System.currentTimeMillis()

        // Log the start time, end time, and calculated duration
        Log.d("TaskDuration", "Start Time: ${task.startTime}")
        Log.d("TaskDuration", "End Time: ${task.endTime}")

        // Calculate the duration in seconds
        val durationSeconds = (task.endTime - task.startTime) / 1000
        Log.d("TaskDuration", "Duration: $durationSeconds seconds")

        task.duration = durationSeconds

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
}
