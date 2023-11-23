package com.timemaster.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "TaskDatabase"
        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_IS_RUNNING = "is_running"
        private const val COLUMN_DURATION = "duration"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, "
                + "$COLUMN_NAME TEXT, $COLUMN_IS_RUNNING INTEGER, $COLUMN_DURATION INTEGER)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTask(task: Task) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, task.name)
        values.put(COLUMN_IS_RUNNING, if (task.isRunning) 1 else 0)
        values.put(COLUMN_DURATION, task.duration)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun deleteTask(taskId: Int): Boolean {
        val db = this.writableDatabase
        return try {
            db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(taskId.toString())) > 0
        } finally {
            db.close()
        }
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        while (cursor.moveToNext()) {
            val taskId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val taskDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION))

            val task = Task(taskName)
            task.id = taskId.toInt()
            task.duration = taskDuration.toLong()

            tasks.add(task)
        }

        cursor.close()
        db.close()

        return tasks
    }
}
