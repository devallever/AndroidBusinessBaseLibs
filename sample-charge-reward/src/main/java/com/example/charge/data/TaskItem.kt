package com.example.charge.data

import com.example.charge.task.TaskHelper

data class TaskItem(
    val id: String,
    val type: String,
    val category: String,
    val greenValue: Float,
    val desc: String,
    val max: Int,
    var current: Int = TaskHelper.getCurrentCount(id),
    var isFinished: Boolean = TaskHelper.checkFinish(id)
)