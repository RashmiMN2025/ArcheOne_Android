package com.archeGlobal.one.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TodoController
import com.archeGlobal.one.model.TaskPriority
import com.archeGlobal.one.model.TodoTask
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    controller: TodoController,
    onBackPressed: () -> Unit
) {
    var newTask by remember { mutableStateOf<TodoTask?>(null) } // <-- Add this line

    val tasksForSelectedDay = controller.getTasksForSelectedDay()
    val currentNewTask = newTask

    // --- Clear newTask if there are other tasks for the day (not just the new one) ---
    LaunchedEffect(tasksForSelectedDay.size, controller.model.selectedDay) {
        // If there are no tasks, clear newTask
        if (tasksForSelectedDay.isEmpty() && newTask != null) {
            newTask = null
        }
        // If the only task is not newTask, clear newTask
        if (tasksForSelectedDay.isNotEmpty() && newTask != null && tasksForSelectedDay.none { it.id == newTask!!.id }) {
            newTask = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFE0DCD1) // Light Beige
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top AppBar
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Checkmate",
                            color = Color.Black,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPressed
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(50.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Day selector row
            DaySelector(
                selectedDay = controller.model.selectedDay,
                onDaySelected = controller::selectDay
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tasks List
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Stretch to full width
                    .weight(0.85f) // Increase height as needed
                    .background(Color(0xFFF8F8F0)), // Beige color
                contentAlignment = Alignment.TopCenter
            ) {
                // Empty State or Task List
                if (tasksForSelectedDay.isEmpty() && currentNewTask != null && currentNewTask.dayOfWeek == controller.model.selectedDay) {
                    EmptyTasksMessage(
                        newTask = currentNewTask,
                        onTaskClick = { /* ... */ },
                        onEditClick = controller::startEditTask,
                        onDeleteClick = {
                            controller.deleteTask(it)
                            newTask = null
                        },
                        onToggleCompleted = controller::toggleTaskCompleted,
                        formatTimeRange = controller::formatTimeRange,
                        formatCreationDate = controller::formatCreationDate
                    )
                } else if (tasksForSelectedDay.isEmpty()) {
                    // Show the empty state if there are no tasks at all for this day
                    EmptyTasksMessage()
                } else {
                    TaskList(
                        tasks = tasksForSelectedDay,
                        onTaskClick = { /* Do nothing when task is clicked */ },
                        onEditClick = controller::startEditTask, // Directly go to edit mode
                        onDeleteClick = controller::deleteTask, // Directly delete the task
                        onToggleCompleted = controller::toggleTaskCompleted, // <-- Pass controller function here
                        formatTimeRange = controller::formatTimeRange,
                        formatCreationDate = controller::formatCreationDate
                    )
                }
            }

            // Add Task Button at bottom
            Button(
                onClick = controller::startAddTask,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE83A25) // Red color from image
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Add Task",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Task Detail Dialog
        if (controller.model.showTaskDetail && controller.model.selectedTask != null) {
            TaskDetailDialog(
                task = controller.model.selectedTask!!,
                onDismiss = controller::closeTaskDetail,
                onEdit = controller::startEditTask,
                onDelete = controller::deleteTask,
                formatTimeRange = controller::formatTimeRange
            )
        }

        // Add Task Dialog
        if (controller.model.isAddingTask) {
            TaskFormDialog(
                isEditing = false,
                initialTask = null,
                onSave = { title, priority, startTime, endTime ->
                    val task = controller.addTask(title, priority, startTime, endTime)
                    newTask = task
                },
                onCancel = controller::cancelAddTask
            )
        }

        // Edit Task Dialog
        if (controller.model.isEditingTask && controller.model.selectedTask != null) {
            TaskFormDialog(
                isEditing = true,
                initialTask = controller.model.selectedTask,
                onSave = controller::updateTask,
                onCancel = controller::cancelEditTask
            )
        }
    }
}

@Composable
fun DaySelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    // Day names - Mon through Fri
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val dayValues = (1..5).toList() // 1 = Monday, 5 = Friday

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFC8C8CA) // Light gray background for tabs
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEachIndexed { index, dayName ->
                val dayValue = dayValues[index]
                val isSelected = selectedDay == dayValue

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .clickable { onDaySelected(dayValue) }, // <-- clickable here for all
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(66.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                fontFamily = GraphikFontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = dayName,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            fontFamily = GraphikFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Add divider between days except after the last one
                if (index < days.lastIndex) {
                    Divider(
                        color = Color(0xFFB0B0B0),
                        modifier = Modifier
                            .fillMaxHeight(0.6f)
                            .width(1.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskList(
    tasks: List<TodoTask>,
    onTaskClick: (TodoTask) -> Unit,
    onEditClick: (TodoTask) -> Unit,
    onDeleteClick: (TodoTask) -> Unit,
    onToggleCompleted: (TodoTask) -> Unit,
    formatTimeRange: (TodoTask) -> String,
    formatCreationDate: (TodoTask) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White), // Keep border
        elevation = CardDefaults.cardElevation(2.dp), // Remove shadow
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp) // <-- Add this line for right shift
        ) {
            items(tasks.size) { index ->
                val task = tasks[index]
                SwipeableTaskItem(
                    task = task,
                    onTaskClick = { /* No action when clicking on the task */ },
                    onEditClick = { onEditClick(task) },
                    onDeleteClick = { onDeleteClick(task) },
                    onToggleCompleted = { onToggleCompleted(task) },
                    formatTimeRange = { formatTimeRange(task) },
                    formatCreationDate = { formatCreationDate(task) }
                )

                if (index < tasks.lastIndex) {
                    Divider(
                        color = Color(0xFFE0DCD1),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableTaskItem(
    task: TodoTask,
    onTaskClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleCompleted: (TodoTask) -> Unit, // <-- Add this line
    formatTimeRange: () -> String,
    formatCreationDate: () -> String
) {
    // State for swipe offset
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 200f // Threshold to consider revealing buttons
    val density = LocalDensity.current

    // Reset swipe when tapping outside
    var isSwipeRevealed by remember { mutableStateOf(false) }

    // Track the card height for matching action button heights
    var cardHeight by remember { mutableStateOf(0.dp) }

    // Buttons width in dp
    val actionsWidth = 160.dp
    // Animated offset to smooth transitions
    val animatedOffsetDp by animateDpAsState(
        targetValue = with(density) { minOf(offsetX, 0f).coerceAtLeast(-actionsWidth.toPx()).toDp() },
        animationSpec = tween(durationMillis = 300),
        label = "Offset Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(cardHeight), // Match card height
            horizontalArrangement = Arrangement.End
        ) {
            // Only show Delete button if task is completed
            if (task.completed) {
                Box(
                    modifier = Modifier
                        .width(80.dp) // Full width of actions
                        .fillMaxHeight()
                        .background(
                            Color(0xFFFF3B30)
                        )
                        .clickable {
                            onDeleteClick()
                            offsetX = 0f // Reset swipe state
                            isSwipeRevealed = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delete",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            } else {
                // Delete button (left)
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .background(
                            Color(0xFFFF3B30)
                        )
                        .clickable {
                            onDeleteClick()
                            offsetX = 0f // Reset swipe state
                            isSwipeRevealed = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delete",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                // Edit button (right)
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .background(
                            Color(0xFF007AFF)
                        )
                        .clickable {
                            onEditClick()
                            offsetX = 0f // Reset swipe state
                            isSwipeRevealed = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Edit",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Main task card - can be swiped
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetDp.roundToPx(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Only allow swiping left
                        if (delta <= 0f) {
                            offsetX += delta
                        } else if (isSwipeRevealed) {
                            // Swiping right to close
                            offsetX += delta
                        }
                        if (offsetX < -swipeThreshold) {
                            isSwipeRevealed = true
                        }
                        if (offsetX > -20f) {
                            isSwipeRevealed = false
                        }
                    },
                    onDragStopped = { _ ->
                        // Snap to position based on current offset
                        offsetX = if (isSwipeRevealed || offsetX < -swipeThreshold / 2) {
                            -actionsWidth.value * density.density
                        } else {
                            0f
                        }
                    }
                )
                .onSizeChanged { size ->
                    // Convert the size in pixels to dp
                    cardHeight = with(density) { size.height.toDp() }
                },
            shape = RoundedCornerShape(0.dp), // <-- Remove curved corners
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSwipeRevealed) {
                            offsetX = 0f
                            isSwipeRevealed = false
                        } else {
                            onToggleCompleted(task) // <-- Call controller's toggle function
                            onTaskClick()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                // Circle with check mark if checked
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (task.completed) Color(0xFFDD3825) else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (task.completed) Color(0xFFDD3825) else Color.Gray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Task details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title
                    Text(
                        text = task.title,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None // <-- Add this line
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Priority
                    Text(
                        text = "Priority: ${task.priority.name}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    )

                    // Time
                    Text(
                        text = "Time: ${formatTimeRange()}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    )

                    // Added on date
                    Text(
                        text = formatCreationDate(),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTasksMessage(
    newTask: TodoTask? = null, // Pass the newly added task here, or null if none
    onTaskClick: (() -> Unit)? = null,
    onEditClick: ((TodoTask) -> Unit)? = null,
    onDeleteClick: ((TodoTask) -> Unit)? = null,
    onToggleCompleted: ((TodoTask) -> Unit)? = null,
    formatTimeRange: ((TodoTask) -> String)? = null,
    formatCreationDate: ((TodoTask) -> String)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth() // Stretch to full width
            .height(650.dp) // Increase height as needed
            .background(Color(0xFFF8F8F0)), // Beige color
        contentAlignment = if (newTask == null) Alignment.Center else Alignment.TopCenter
    ) {
        if (newTask == null) {
            // Show empty state text and arrow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No tasks for this day",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap Add Task to create a new task",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Down arrow with circle icon
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape), // Gray border
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // Use ArrowBack and rotate
                        contentDescription = "Down Arrow",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(270f) // Rotate to point down
                    )
                }
            }
        } else { // Show the new task card on the beige background, hide the text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp), // Add top padding here (adjust as needed)
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SwipeableTaskItem(
                    task = newTask,
                    onTaskClick = { onTaskClick?.invoke() ?: Unit },
                    onEditClick = { onEditClick?.invoke(newTask) ?: Unit },
                    onDeleteClick = {
                        onDeleteClick?.invoke(newTask)
                        // No extra space above the card
                    },
                    onToggleCompleted = { onToggleCompleted?.invoke(newTask) ?: Unit },
                    formatTimeRange = { formatTimeRange?.invoke(newTask) ?: "" },
                    formatCreationDate = { formatCreationDate?.invoke(newTask) ?: "" }
                )
            }
        }
    }
}

@Composable
fun TaskDetailDialog(
    task: TodoTask,
    onDismiss: () -> Unit,
    onEdit: (TodoTask) -> Unit,
    onDelete: (TodoTask) -> Unit,
    formatTimeRange: (TodoTask) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Task Details", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Priority: ")
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = when (task.priority) {
                                    TaskPriority.HIGH -> Color(0xFFDD3825) // Red
                                    TaskPriority.MEDIUM -> Color(0xFFFFA500) // Orange
                                    TaskPriority.LOW -> Color(0xFF4CAF50) // Green
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.priority.name,
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Time: ${formatTimeRange(task)}",
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onEdit(task) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = { onDelete(task) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825)
                    )
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TaskFormDialog(
    isEditing: Boolean,
    initialTask: TodoTask?,
    onSave: (String, TaskPriority, LocalTime, LocalTime) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: TaskPriority.MEDIUM) }
    var startTimeHour by remember {
        mutableStateOf(
            initialTask?.startTime?.hour ?: LocalTime.now().hour
        )
    }
    var startTimeMinute by remember {
        mutableStateOf(
            initialTask?.startTime?.minute ?: LocalTime.now().minute
        )
    }
    var endTimeHour by remember {
        mutableStateOf(
            initialTask?.endTime?.hour ?: LocalTime.now().plusHours(1).hour
        )
    }
    var endTimeMinute by remember {
        mutableStateOf(
            initialTask?.endTime?.minute ?: LocalTime.now().minute
        )
    }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTimeFormatted = LocalTime.of(startTimeHour, startTimeMinute).format(formatter)
    val endTimeFormatted = LocalTime.of(endTimeHour, endTimeMinute).format(formatter)

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val dialogWidth = screenWidthDp - 40.dp // Match the card container's horizontal padding

    // Full-screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)) // semi-transparent background
            .clickable(onClick = onCancel) // dismiss on outside click
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp) // match your card container
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Task" else "Add Task",
                    fontSize = 22.sp,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Task Note section
                Text(
                    text = "Task Note",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Task input field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Please enter your task details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp), // Make text field taller
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.LightGray,
                        unfocusedTextColor = Color.LightGray,
                        focusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Priority selection - segmented control
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEBEBEB) // Light gray background
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val options = listOf(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH)
                        options.forEachIndexed { index, option ->
                            val isSelected = priority == option

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color.White else Color.Transparent
                                    )
                                    .clickable { priority = option }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.name.capitalize(),
                                    color = Color.Black,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Medium
                                )
                            }

                            // Add divider between priorities except after the last one
                            if (index < options.lastIndex) {
                                Divider(
                                    color = Color(0xFFD0D0D0),
                                    modifier = Modifier
                                        .fillMaxHeight(0.7f)
                                        .width(1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time range section
                Text(
                    text = "Time Range",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Combined time range selection box
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEBEBEB) // Light gray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From time
                        Row(
                            modifier = Modifier
                                .clickable { showStartTimePicker = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "From:",
                                fontSize = 18.sp,
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = startTimeFormatted,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        // Separator
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray)
                        )

                        // To time
                        Row(
                            modifier = Modifier
                                .clickable { showEndTimePicker = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "To:",
                                fontSize = 18.sp,
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = endTimeFormatted,
                                fontSize = 18.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Task button
                Button(
                    onClick = {
                        val startTime = LocalTime.of(startTimeHour, startTimeMinute)
                        val endTime = LocalTime.of(endTimeHour, endTimeMinute)
                        onSave(title, priority, startTime, endTime)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825) // Red button
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isEditing) "Save Task" else "Add Task",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Close text button
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Close",
                        color = Color(0xFFDD3825), // Red text
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }

    // Time picker dialogs
    if (showStartTimePicker) {
        NativeTimePickerDialog(
            initialHour = startTimeHour,
            initialMinute = startTimeMinute,
            onTimeSelected = { hour, minute ->
                startTimeHour = hour
                startTimeMinute = minute
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        NativeTimePickerDialog(
            initialHour = endTimeHour,
            initialMinute = endTimeMinute,
            onTimeSelected = { hour, minute ->
                endTimeHour = hour
                endTimeMinute = minute
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
fun NativeTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour12 = when {
        initialHour == 0 -> 12
        initialHour > 12 -> initialHour - 12
        else -> initialHour
    }
    val initialIsAM = initialHour < 12

    var selectedHour by remember { mutableStateOf<Int?>(initialHour12) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var isAM by remember { mutableStateOf(initialIsAM) }
    var isSelectingMinute by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(340.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!isSelectingMinute) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(Color(0xFFEBEBEB), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        with(LocalDensity.current) {
                            val radius = 105.dp.toPx()
                            for (i in 0 until 12) {
                                val angle = Math.toRadians((i * 30 - 60).toDouble())
                                val x = kotlin.math.cos(angle) * radius
                                val y = kotlin.math.sin(angle) * radius
                                val hour = if (i == 0) 12 else i
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                x = x.toInt(),
                                                y = y.toInt()
                                            )
                                        }
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedHour == hour) Color(0xFFDD3825) else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (selectedHour == hour) Color(0xFFDD3825) else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedHour = hour
                                            isSelectingMinute = true // Switch to minute selection immediately
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hour.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                        color = if (selectedHour == hour) Color.White else Color.Black
                                    )
                                }
                            }
                            // AM/PM toggle in center
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { isAM = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAM) Color(0xFFDD3825) else Color(0xFFF8F8F0)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "AM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GraphikFontFamily,
                                        color = if (isAM) Color.White else Color(0xFFDD3825)
                                    )
                                }
                                Button(
                                    onClick = { isAM = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isAM) Color(0xFFDD3825) else Color(0xFFF8F8F0)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "PM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GraphikFontFamily,
                                        color = if (!isAM) Color.White else Color(0xFFDD3825)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(Color(0xFFEBEBEB), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        with(LocalDensity.current) {
                            val radius = 100.dp.toPx() // Increased radius for numbers
                            for (i in 0 until 12) {
                                val angle = Math.toRadians((i * 30 - 60).toDouble())
                                val x = kotlin.math.cos(angle) * radius
                                val y = kotlin.math.sin(angle) * radius
                                val minute = (i * 5) % 60
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                x = x.toInt(),
                                                y = y.toInt()
                                            )
                                        }
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedMinute == minute) Color(0xFFDD3825) else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (selectedMinute == minute) Color(0xFFDD3825) else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedMinute = minute
                                            // Confirm immediately after minute selection
                                            val hour24 = if (selectedHour == 12) {
                                                if (isAM) 0 else 12
                                            } else {
                                                if (isAM) selectedHour!! else selectedHour!! + 12
                                            }
                                            onTimeSelected(hour24, selectedMinute)
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "%02d".format(minute),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = GraphikFontFamily,
                                        color = if (selectedMinute == minute) Color.White else Color.Black
                                    )
                                }
                            }
                            // Back to hour selection in center (unchanged)
                            TextButton(
                                onClick = { isSelectingMinute = false },
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = "Back",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = GraphikFontFamily,
                                    color = Color(0xFFDD3825)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = GraphikFontFamily,
                        color = Color(0xFFDD3825)
                    )
                }
            }
        }
    }
}

// Helper function to capitalize first letter of a string
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
