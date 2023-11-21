package com.timemaster.model

import java.util.logging.Handler

class Task(val name: String) {
    var isRunning: Boolean = false
    var duration: Long = 0
    var handler: Handler? = null
    var timerRunnable: Runnable? = null
}
