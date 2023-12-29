package com.timemaster.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.timemaster.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timemaster.adapter.SharedTasksAdapter
import com.timemaster.model.TaskDbHelper

class SharedData : AppCompatActivity() {

    private lateinit var userIdEditText: EditText
    private lateinit var displayButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: SharedTasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shared_data)

        userIdEditText = findViewById(R.id.userIdEditText)
        displayButton = findViewById(R.id.displayButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Initialize the RecyclerView and its adapter
        taskAdapter = SharedTasksAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

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

        displayButton.setOnClickListener {
            val enteredUserId = userIdEditText.text.toString().trim()

            if (enteredUserId.isNotEmpty()) {
                displayTasksForUserId(enteredUserId)
            } else {
                val message = "User ID cannot be blank"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayTasksForUserId(userId: String) {
        val dbHelper = TaskDbHelper(this)

        dbHelper.displayTasksForUserId(userId,
            onSuccess = { tasksList ->
                // Update the UI
                taskAdapter.setTasks(tasksList)
            },
            onFailure = { e ->
                val message = "User ID is incorrect"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}

