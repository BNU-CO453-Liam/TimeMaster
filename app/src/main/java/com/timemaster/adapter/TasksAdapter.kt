package com.timemaster.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.timemaster.R
import com.timemaster.model.Task
import java.util.*

class TasksAdapter(private val context: Context, private val taskList: MutableList<Task>,
                   private val onDeleteClickListener: (position: Int) -> Unit) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val timers: MutableMap<Int, Handler> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.task_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskNameTextView.text = task.name
        holder.timerTextView.text = getFormattedTime(task.duration)

        // Set play button state and click listener
        if (task.isRunning) {
            holder.playButton.setBackgroundResource(R.drawable.pause)
            holder.playButton.isChecked = true
        } else {
            holder.playButton.setBackgroundResource(R.drawable.play)
            holder.playButton.isChecked = false
        }

        holder.playButton.setOnClickListener {
            toggleTimer(position)
        }

        holder.deleteButton.setOnClickListener {
            //deleteTask(position)
            onDeleteClickListener.invoke(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun getTimerHandler(position: Int): Handler? {
        return timers[position]
    }

    fun removeTimer(position: Int) {
        timers.remove(position)
    }

    private fun toggleTimer(position: Int) {
        val task = taskList[position]
        if (task.isRunning) {
            pauseTimer(position)
        } else {
            startTimer(position)
        }
        notifyDataSetChanged()
    }

    private fun startTimer(position: Int) {
        val task = taskList[position]
        task.isRunning = true
        timers[position] = Handler(Looper.getMainLooper())
        timers[position]?.postDelayed(object : Runnable {
            override fun run() {
                task.duration += 1000
                notifyDataSetChanged()
                timers[position]?.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun pauseTimer(position: Int) {
        val task = taskList[position]
        task.isRunning = false
        timers[position]?.removeCallbacksAndMessages(null)
        timers.remove(position)
        notifyDataSetChanged()
    }

    fun updateTimers() {
        for (index in timers.keys) {
            taskList[index].duration += 1000
        }
        notifyDataSetChanged()
    }

    fun deleteTask(position: Int) {
        val task = taskList[position]
        if (task.isRunning) {
            pauseTimer(position)
        }
        taskList.removeAt(position)
        notifyDataSetChanged()
    }

    private fun getFormattedTime(duration: Long): String {
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        val seconds = ((duration % 3600000) % 60000) / 1000
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskNameTextView: TextView = view.findViewById(R.id.taskNameTextView)
        val timerTextView: TextView = view.findViewById(R.id.timerTextView)
        val playButton: ToggleButton = view.findViewById(R.id.playButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }
}
