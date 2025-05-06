package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.TaskPriority
import com.archeGlobal.one.model.TodoModel
import com.archeGlobal.one.model.TodoTask
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.PreferencesManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TodoController(
    private val navigator: Navigator,
    private val context: Context
) {
    private val preferencesManager = PreferencesManager(context)
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .create()
    
    var model by mutableStateOf(TodoModel())
        private set
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        try {
            val tasksJson = preferencesManager.getTasks()
            if (tasksJson.isEmpty()) {
                model = model.copy(tasks = emptyList())
                return
            }
            
            val type = object : TypeToken<List<TodoTask>>() {}.type
            val tasks = gson.fromJson<List<TodoTask>>(tasksJson, type)
            
            model = model.copy(tasks = tasks)
            Log.d("TodoController", "Loaded ${tasks.size} tasks")
        } catch (e: Exception) {
            Log.e("TodoController", "Error loading tasks: ${e.message}", e)
            model = model.copy(tasks = emptyList())
        }
    }
    
    private fun saveTasks() {
        try {
            val tasksJson = gson.toJson(model.tasks)
            preferencesManager.saveTasks(tasksJson)
            Log.d("TodoController", "Saved ${model.tasks.size} tasks")
        } catch (e: Exception) {
            Log.e("TodoController", "Error saving tasks: ${e.message}", e)
        }
    }
    
    fun selectDay(dayOfWeek: Int) {
        model = model.copy(selectedDay = dayOfWeek)
    }
    
    fun getTasksForSelectedDay(): List<TodoTask> {
        return model.tasks.filter { it.dayOfWeek == model.selectedDay }
            .sortedBy { it.startTime }
    }
    
    fun formatTimeRange(task: TodoTask): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return "${task.startTime.format(formatter)} - ${task.endTime.format(formatter)}"
    }
    
    fun formatCreationDate(task: TodoTask): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        return "Added on: ${formatter.format(task.creationDate)} at ${task.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
    }
    
    fun selectTask(task: TodoTask) {
        model = model.copy(
            selectedTask = task,
            showTaskDetail = true
        )
    }
    
    fun closeTaskDetail() {
        model = model.copy(
            showTaskDetail = false
        )
    }
    
    fun startAddTask() {
        model = model.copy(
            isAddingTask = true
        )
    }
    
    fun cancelAddTask() {
        model = model.copy(
            isAddingTask = false
        )
    }
    
    fun addTask(title: String, priority: TaskPriority, startTime: LocalTime, endTime: LocalTime) {
        if (title.isBlank()) {
            return
        }
        
        val newTask = TodoTask(
            title = title,
            priority = priority,
            startTime = startTime,
            endTime = endTime,
            dayOfWeek = model.selectedDay
        )
        
        val updatedTasks = model.tasks.toMutableList()
        updatedTasks.add(newTask)
        
        model = model.copy(
            tasks = updatedTasks,
            isAddingTask = false
        )
        
        saveTasks()
    }
    
    fun startEditTask(task: TodoTask) {
        model = model.copy(
            selectedTask = task,
            isEditingTask = true,
            showTaskDetail = false
        )
    }
    
    fun cancelEditTask() {
        model = model.copy(
            isEditingTask = false
        )
    }
    
    fun updateTask(title: String, priority: TaskPriority, startTime: LocalTime, endTime: LocalTime) {
        if (title.isBlank() || model.selectedTask == null) {
            return
        }
        
        val updatedTask = model.selectedTask!!.copy(
            title = title,
            priority = priority,
            startTime = startTime,
            endTime = endTime
        )
        
        val taskIndex = model.tasks.indexOfFirst { it.id == updatedTask.id }
        if (taskIndex == -1) {
            return
        }
        
        val updatedTasks = model.tasks.toMutableList()
        updatedTasks[taskIndex] = updatedTask
        
        model = model.copy(
            tasks = updatedTasks,
            selectedTask = null,
            isEditingTask = false
        )
        
        saveTasks()
    }
    
    fun deleteTask(task: TodoTask) {
        val updatedTasks = model.tasks.filter { it.id != task.id }
        
        model = model.copy(
            tasks = updatedTasks,
            selectedTask = null,
            showTaskDetail = false
        )
        
        saveTasks()
    }
    
    fun onBackPressed() {
        navigator.navigateToHome()
    }
    
    // Adapters for Gson to handle LocalDate and LocalTime
    private class LocalDateAdapter : com.google.gson.TypeAdapter<LocalDate>() {
        override fun write(out: com.google.gson.stream.JsonWriter, value: LocalDate?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.toString())
            }
        }
        
        override fun read(input: com.google.gson.stream.JsonReader): LocalDate? {
            val dateString = input.nextString()
            return if (dateString.isNullOrEmpty()) null else LocalDate.parse(dateString)
        }
    }
    
    private class LocalTimeAdapter : com.google.gson.TypeAdapter<LocalTime>() {
        override fun write(out: com.google.gson.stream.JsonWriter, value: LocalTime?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.toString())
            }
        }
        
        override fun read(input: com.google.gson.stream.JsonReader): LocalTime? {
            val timeString = input.nextString()
            return if (timeString.isNullOrEmpty()) null else LocalTime.parse(timeString)
        }
    }
}