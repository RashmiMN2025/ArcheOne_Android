package com.archeGlobal.one.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
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
                            text = "To Do",
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
                        onClick = onBackPressed,
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
            
            // Tasks List
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Empty State or Task List
                if (controller.getTasksForSelectedDay().isEmpty()) {
                    EmptyTasksMessage()
                } else {
                    TaskList(
                        tasks = controller.getTasksForSelectedDay(),
                        onTaskClick = { /* Do nothing when task is clicked */ },
                        onEditClick = controller::startEditTask, // Directly go to edit mode
                        onDeleteClick = controller::deleteTask, // Directly delete the task
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
                shape = RoundedCornerShape(8.dp)
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
                onSave = controller::addTask,
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD9D9D9) // Light gray background for tabs
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEachIndexed { index, dayName ->
                val dayValue = dayValues[index]
                val isSelected = selectedDay == dayValue
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color.White else Color.Transparent
                        )
                        .clickable { onDaySelected(dayValue) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        color = Color.Black,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily
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
    formatTimeRange: (TodoTask) -> String,
    formatCreationDate: (TodoTask) -> String
) {
    LazyColumn {
        items(tasks) { task ->
            SwipeableTaskItem(
                task = task,
                onTaskClick = { /* No action when clicking on the task */ },
                onEditClick = { onEditClick(task) },
                onDeleteClick = { onDeleteClick(task) },
                formatTimeRange = { formatTimeRange(task) },
                formatCreationDate = { formatCreationDate(task) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SwipeableTaskItem(
    task: TodoTask,
    onTaskClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
            .padding(horizontal = 16.dp)
    ) {
        // Actions container - positioned behind the card
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(cardHeight), // Match card height
            horizontalArrangement = Arrangement.End
        ) {
            // Delete button (first on the left)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFF3B30), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
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
            
            // Edit button (on the right side)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF007AFF), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
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
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        if (isSwipeRevealed) {
                            // Reset swipe if actions are revealed
                            offsetX = 0f
                            isSwipeRevealed = false
                        } else {
                            // Only call onTaskClick if we're not resetting the swipe
                            onTaskClick()
                        }
                    }
            ) {
                // Title
                Text(
                    text = task.title,
                    fontSize = 22.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Priority
                Text(
                    text = "Priority: ${task.priority.name}",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time
                Text(
                    text = "Time: ${formatTimeRange()}",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Added on date
                Text(
                    text = formatCreationDate(),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyTasksMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
    var startTimeHour by remember { mutableStateOf(initialTask?.startTime?.hour ?: LocalTime.now().hour) }
    var startTimeMinute by remember { mutableStateOf(initialTask?.startTime?.minute ?: LocalTime.now().minute) }
    var endTimeHour by remember { mutableStateOf(initialTask?.endTime?.hour ?: LocalTime.now().plusHours(1).hour) }
    var endTimeMinute by remember { mutableStateOf(initialTask?.endTime?.minute ?: LocalTime.now().minute) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTimeFormatted = LocalTime.of(startTimeHour, startTimeMinute).format(formatter)
    val endTimeFormatted = LocalTime.of(endTimeHour, endTimeMinute).format(formatter)
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = if (isEditing) "Edit Task" else "Add Task",
                fontSize = 24.sp,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Task Note section
                Text(
                    text = "Task Note",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Task input field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Please enter your task details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp), // Make text field taller
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.Black,
                        unfocusedTextColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority selection - segmented control
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEBEBEB) // Light gray background
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    ) {
                        val options = listOf(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH)
                        
                        options.forEach { option ->
                            val isSelected = priority == option
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color.White else Color.Transparent
                                    )
                                    .clickable { priority = option }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.name.capitalize(),
                                    color = Color.Black,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                    shape = RoundedCornerShape(16.dp),
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
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = startTimeFormatted,
                                fontSize = 16.sp,
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
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = endTimeFormatted,
                                fontSize = 16.sp,
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
                        fontSize = 18.sp,
                        color = Color.White,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Close text button
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Close",
                        color = Color(0xFFDD3825), // Red text
                        fontSize = 16.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = null,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
    
    // Time picker dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
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
        TimePickerDialog(
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
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                fontSize = 20.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Digital clock display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEBEBEB) // Light gray background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Digital time display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hours
                            Card(
                                modifier = Modifier,
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Up arrow
                                    IconButton(
                                        onClick = {
                                            selectedHour = (selectedHour + 1) % 24
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Increase hour",
                                            modifier = Modifier.rotate(90f)
                                        )
                                    }
                                    
                                    // Hour text
                                    Text(
                                        text = selectedHour.toString().padStart(2, '0'),
                                        fontSize = 24.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Down arrow
                                    IconButton(
                                        onClick = {
                                            selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Decrease hour",
                                            modifier = Modifier.rotate(270f)
                                        )
                                    }
                                }
                            }
                            
                            // Separator
                            Text(
                                text = ":",
                                fontSize = 24.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            // Minutes
                            Card(
                                modifier = Modifier,
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Up arrow
                                    IconButton(
                                        onClick = {
                                            selectedMinute = (selectedMinute + 5) % 60
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Increase minute",
                                            modifier = Modifier.rotate(90f)
                                        )
                                    }
                                    
                                    // Minute text
                                    Text(
                                        text = selectedMinute.toString().padStart(2, '0'),
                                        fontSize = 24.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Down arrow
                                    IconButton(
                                        onClick = {
                                            selectedMinute = if (selectedMinute >= 5) selectedMinute - 5 else 55
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Decrease minute",
                                            modifier = Modifier.rotate(270f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(selectedHour, selectedMinute) },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to capitalize first letter of a string
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDropdown(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    options: List<Int>
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString().padStart(2, '0')) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}