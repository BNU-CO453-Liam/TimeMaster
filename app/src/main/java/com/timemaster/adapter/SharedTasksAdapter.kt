package com.timemaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.timemaster.R
import com.timemaster.model.SharedTask

class SharedTasksAdapter : RecyclerView.Adapter<SharedTasksAdapter.TaskViewHolder>() {

    private var tasks: List<SharedTask> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.shared_data_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    fun setTasks(taskList: List<SharedTask>) {
        tasks = taskList
        notifyDataSetChanged()
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        private val dailyTargetTextView: TextView = itemView.findViewById(R.id.dailyTargetTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)

        fun bind(task: SharedTask) {
            taskNameTextView.text = "Task: ${task.name}"
            dailyTargetTextView.text = "Daily Target: ${task.dailyTargetTime}"
            durationTextView.text = "Duration: ${task.duration}"
        }
    }
}
