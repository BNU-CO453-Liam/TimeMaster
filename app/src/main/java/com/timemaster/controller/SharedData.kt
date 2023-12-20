package com.timemaster.controller

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.timemaster.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
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

        displayButton.setOnClickListener {
            val enteredUserId = userIdEditText.text.toString().trim()

            if (enteredUserId.isNotEmpty()) {
                displayTasksForUserId(enteredUserId)
                // n1PUKhXU55MSfvBGCl5KFShezz43 hard code test
            } else {
                Log.e("Shared_data", "User ID is empty")
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
                Log.e("Shared_data", "Error retrieving tasks from TaskDbHelper", e)
            }
        )
    }
}

