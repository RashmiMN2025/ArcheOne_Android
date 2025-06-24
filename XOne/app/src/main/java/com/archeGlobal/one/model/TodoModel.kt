package com.archeGlobal.one.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class TodoModel(
    val tasks: List<TodoTask> = emptyList(),
    val selectedDay: Int = LocalDate.now().dayOfWeek.value, // 1 = Monday, 7 = Sunday
    val selectedTask: TodoTask? = null,
    val showTaskDetail: Boolean = false,
    val isAddingTask: Boolean = false,
    val isEditingTask: Boolean = false
)

data class TodoTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val startTime: LocalTime = LocalTime.now(),
    val endTime: LocalTime = LocalTime.now().plusHours(1),
    val dayOfWeek: Int = LocalDate.now().dayOfWeek.value, // 1 = Monday, 7 = Sunday
    val dateAdded: LocalDate = LocalDate.now(),
    val creationDate: LocalDate = LocalDate.now(),
    val completed: Boolean = false // <-- Add this line
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}
