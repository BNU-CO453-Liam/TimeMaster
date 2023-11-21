package com.timemaster.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import com.timemaster.R
import com.timemaster.model.Task
import java.util.*

class TasksAdapter(private val context: Context, private val taskList: MutableList<Task>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val timers: MutableMap<Int, Handler> = mutableMapOf()

    override fun getCount(): Int {
        return taskList.size
    }

    override fun getItem(position: Int): Task {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.task_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val task = getItem(position)
        viewHolder.taskNameTextView.text = task.name
        viewHolder.timerTextView.text = getFormattedTime(task.duration)

        // Set play button state and click listener
        if (task.isRunning) {
            viewHolder.playButton.setBackgroundResource(R.drawable.pause)
            viewHolder.playButton.isChecked = true
        } else {
            viewHolder.playButton.setBackgroundResource(R.drawable.play)
            viewHolder.playButton.isChecked = false
        }

        viewHolder.playButton.setOnClickListener {
            toggleTimer(position)
        }

        viewHolder.deleteButton.setOnClickListener {
            deleteTask(position)
        }

        return view
    }

    private fun toggleTimer(position: Int) {
        val task = getItem(position)
        if (task.isRunning) {
            pauseTimer(position)
        } else {
            startTimer(position)
        }
        notifyDataSetChanged()
    }

    private fun startTimer(position: Int) {
        val task = getItem(position)
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
        val task = getItem(position)
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

    private fun deleteTask(position: Int) {
        val task = getItem(position)
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

    private class ViewHolder(view: View) {
        val taskNameTextView: TextView = view.findViewById(R.id.taskNameTextView)
        val timerTextView: TextView = view.findViewById(R.id.timerTextView)
        val playButton: ToggleButton = view.findViewById(R.id.playButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }
}
