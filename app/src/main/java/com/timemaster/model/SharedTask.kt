package com.timemaster.model

data class SharedTask(
    val name: String,
    val dailyTargetTime: Long,
    val duration: Long,
    val formattedDuration: String,
    val formattedTarget: String
    )