package com.archeGlobal.one.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ChatViewModel
import com.archeGlobal.one.model.ChatBottomNavigationBar
import com.archeGlobal.one.model.Message
import com.archeGlobal.one.ui.components.TypingIndicator
import com.archeGlobal.one.utils.ChatData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    navController: NavController,
    onBackPressed: () -> Unit,
    showBottomBar: Boolean = false
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var messageToReport by remember { mutableStateOf<Message?>(null) }

    // Disable back swipe gesture and back button
    BackHandler(enabled = true) {
        // Do nothing to prevent navigation
    }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val messages = viewModel.messages
    val inputText = viewModel.inputText.value
    val isTyping = viewModel.isTyping.value

    // Auto-scroll to the last message
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light grey at top
                            Color(0xFFC8C8CA), // Medium grey in middle
                            Color(0xFF474749) // Dark grey at bottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Chat Support",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Chat messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    state = listState
                ) {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            viewModel = viewModel,
                            chatData = ChatData.shared,
                            onReportMessage = { msg ->
                                messageToReport = msg
                                showReportDialog = true
                            }
                        )
                    }

                    if (isTyping) {
                        item {
                            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                TypingIndicator()
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Input area
                Column {
                    // Instructional note
                    Text(
                        text = "Long press any message to report",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 0.dp),
                        textAlign = TextAlign.Center
                    )
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEEEEE)) // Light grey background
                            .padding(horizontal = 16.dp, vertical = 12.dp), // Reduced vertical padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { viewModel.inputText.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("Type your question...") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        viewModel.sendMessage(inputText)
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                cursorColor = Color(0xFFDD3825),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 1,
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    if (inputText.isNotBlank()) {
                                        viewModel.sendMessage(inputText)
                                        focusManager.clearFocus()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.send),
                                contentDescription = "Send",
                                tint = Color(0xFFDD3825),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Bottom navigation
                if (showBottomBar) {
                    val context = LocalContext.current
                    val sharedPref = context.getSharedPreferences("event_preferences", Context.MODE_PRIVATE)
                    val isUsingPrideIcon = sharedPref.getBoolean("using_pride_icon", false)

                    ChatBottomNavigationBar(
                        onHomeClick = { navController.navigate("home") },
                        onChatClick = { /* Already on Chat screen */ },
                        onSOSClick = { navController.navigate("sos") },
                        onProfileClick = { navController.navigate("profile") },
                        isUsingPrideIcon = isUsingPrideIcon
                    )
                }
            }
        }
    }

    // Report Dialog
    if (showReportDialog && messageToReport != null) {
        val context = LocalContext.current
        ReportMessageDialog(
            message = messageToReport!!,
            onDismiss = {
                showReportDialog = false
                messageToReport = null
            },
            onReport = { message, reason ->
                reportMessage(context, message, reason)
                showReportDialog = false
                messageToReport = null
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    viewModel: ChatViewModel,
    chatData: ChatData,
    onReportMessage: (Message) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            if (!message.isUser) {
                // Display the main message content
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFAF5)) // Creamy color for bot messages
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onReportMessage(message)
                                }
                            )
                        }
                ) {
                    Column {
                        // Check if the message contains "I found multiple relevant questions"
                        if (message.content.startsWith("I found multiple relevant questions")) {
                            // Display FAQ list items
                            message.content.split("\n").forEach { line ->
                                if (line.startsWith("•")) {
                                    val question = line.substring(2).trim()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectFAQ(question) }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Display bullet point and question text
                                        Text(
                                            text = "• $question",
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Red arrow on the right
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                                            contentDescription = "Arrow",
                                            tint = Color(0xFFDD3825),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else if (line.isNotEmpty() && !line.startsWith("Here are some answers")) {
                                    ClickableEmailText(
                                        text = line,
                                        color = Color.Black,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        onLongPress = { onReportMessage(message) }
                                    )
                                }
                            }
                        } else if (message.content.contains("\n\n\n")) {
                            // Handle the new format with triple newline separator
                            val parts = message.content.split("\n\n\n", limit = 2)
                            if (parts.size == 2) { // Display user question
                                ClickableEmailText(
                                    text = parts[0], // This is the question
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal, // Changed from Medium to Normal to match answer
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(bottom = 24.dp), // Increased padding for more space
                                    onLongPress = { onReportMessage(message) }
                                )

                                // Display the answer with double spacing
                                ClickableEmailText(
                                    text = parts[1], // This is the answer
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    onLongPress = { onReportMessage(message) }
                                )
                            } else {
                                // Fallback if format is unexpected
                                ClickableEmailText(
                                    text = message.content,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    onLongPress = { onReportMessage(message) }
                                )
                            }
                        } else if (message.content.startsWith("You asked:")) {
                            // This block is kept for backward compatibility with older messages
                            val parts = message.content.split("\n\n", limit = 2)

                            if (parts.size > 1) {
                                // Get the question part and remove the "You asked:" prefix and quotes
                                val questionText = parts[0].removePrefix("You asked: ")
                                    .trim()
                                    .removeSurrounding("\"")
                                // Display question with the same style as the answer text
                                ClickableEmailText(
                                    text = questionText,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal, // Changed from Medium to Normal to match answer
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    onLongPress = { onReportMessage(message) }
                                )

                                // Display the answer with proper spacing
                                ClickableEmailText(
                                    text = parts[1],
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    onLongPress = { onReportMessage(message) }
                                )
                            } else {
                                // Fallback if format is unexpected
                                ClickableEmailText(
                                    text = message.content,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    onLongPress = { onReportMessage(message) }
                                )
                            }

                            // Append FAQ list if `showFAQs` is true
                            if (message.showFAQs) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Display FAQ categories
                                val faqsToShow = if (message.showMoreCategories) chatData.faqs else chatData.faqs.take(5)

                                faqsToShow.forEach { faq ->
                                    FAQQuestionRow(question = faq.title) {
                                        viewModel.selectFAQ(faq.question)
                                    }
                                }

                                // Show "Show More" button if not all FAQs are displayed
                                if (!message.showMoreCategories && chatData.faqs.size > 5) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "Show More",
                                            color = Color(0xFFDD3825),
                                            fontSize = 14.sp,
                                            modifier = Modifier
                                                .clickable { viewModel.loadMoreFAQs(message.id) }
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Display the regular message content
                            ClickableEmailText(
                                text = message.content,
                                color = Color.Black,
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                                onLongPress = { onReportMessage(message) }
                            )

                            // Append FAQ list if `showFAQs` is true
                            if (message.showFAQs) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Display FAQ categories
                                val faqsToShow = if (message.showMoreCategories) chatData.faqs else chatData.faqs.take(5)

                                faqsToShow.forEach { faq ->
                                    FAQQuestionRow(question = faq.title) {
                                        viewModel.selectFAQ(faq.question)
                                    }
                                }

                                // Show "Show More" button if not all FAQs are displayed
                                if (!message.showMoreCategories && chatData.faqs.size > 5) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "Show More",
                                            color = Color(0xFFDD3825),
                                            fontSize = 14.sp,
                                            modifier = Modifier
                                                .clickable { viewModel.loadMoreFAQs(message.id) }
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Regular user message bubble
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFDD3825)) // Red color for user messages
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onReportMessage(message)
                                }
                            )
                        }
                ) {
                    Text(
                        text = message.content,
                        color = Color.White
                    )
                }
            }
        }

        // Timestamp
        Text(
            text = formatTime(message.timestamp),
            fontSize = 10.sp,
            color = Color(0xFFFFFAF5),
            modifier = Modifier.padding(top = 4.dp, start = if (message.isUser) 0.dp else 8.dp, end = if (!message.isUser) 0.dp else 8.dp)
        )
    }
}

@Composable
fun WelcomeMessage() {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "👋",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "\uD83D\uDC4B Welcome to ArcheOne Assistant!\n\nI'm your personal support guide, ready to help you navigate through ArcheOne's features and services.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "I'm your personal support guide, ready to help you navigate through ArcheOne's features and services.",
                color = Color.Black
            )
        }
    }
}

@Composable
fun FAQQuestionRow(question: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Arrow",
                tint = Color(0xFFDD3825),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatTime(date: Date): String {
    return DateFormat.format("hh:mm a", date).toString()
}

@Composable
fun ClickableEmailText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    lineHeight: androidx.compose.ui.unit.TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Black,
    onLongPress: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val annotatedString = buildAnnotatedStringWithEmails(text)
    val textLayoutResult = remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    onLongPress?.invoke()
                },
                onTap = { offset ->
                    textLayoutResult.value?.let { layoutResult ->
                        val position = layoutResult.getOffsetForPosition(offset)
                        annotatedString.getStringAnnotations(
                            tag = "EMAIL",
                            start = position,
                            end = position
                        ).firstOrNull()?.let { emailAnnotation ->
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${emailAnnotation.item}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        },
        onTextLayout = { textLayoutResult.value = it },
        style = androidx.compose.ui.text.TextStyle(
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            color = color
        )
    )
}

private fun buildAnnotatedStringWithEmails(text: String): AnnotatedString {
    val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()

    return buildAnnotatedString {
        var lastIndex = 0

        emailPattern.findAll(text).forEach { matchResult ->
            // Add text before email
            if (matchResult.range.first > lastIndex) {
                append(text.substring(lastIndex, matchResult.range.first))
            }

            // Add email with styling and annotation
            val email = matchResult.value
            pushStringAnnotation(tag = "EMAIL", annotation = email)
            pushStyle(
                SpanStyle(
                    color = Color(0xFFDD3825), // Red color for emails
                    textDecoration = TextDecoration.Underline
                )
            )
            append(email)
            pop() // Remove style
            pop() // Remove annotation

            lastIndex = matchResult.range.last + 1
        }

        // Add remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun ReportMessageDialog(
    message: Message,
    onDismiss: () -> Unit,
    onReport: (Message, String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Inappropriate Content") }
    var customReason by remember { mutableStateOf("") }

    val reportReasons = listOf(
        "Inappropriate Content",
        "Spam or Repetitive",
        "Harassment or Abuse",
        "False Information",
        "Offensive Language",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report Message",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column {
                Text(
                    text = "Why are you reporting this message?",
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                reportReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            color = Color.Black
                        )
                    }
                }

                // Show text input when "Other" is selected
                if (selectedReason == "Other") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Describe the issue", fontSize = 12.sp) },
                        placeholder = { Text("Please explain the issue...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        maxLines = 2,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFFDD3825),
                            unfocusedIndicatorColor = Color.Gray,
                            focusedLabelColor = Color(0xFFDD3825),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color(0xFFDD3825),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Other" && customReason.isNotBlank()) {
                        "Other: $customReason"
                    } else if (selectedReason == "Other") {
                        "Other: No specific details provided"
                    } else {
                        selectedReason
                    }
                    onReport(message, finalReason)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDD3825)
                )
            ) {
                Text("Report", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFDD3825))
            }
        },
        containerColor = Color.White
    )
}

private fun reportMessage(context: Context, message: Message, reason: String) {
    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(message.timestamp)
    val report = """
        === MESSAGE REPORT ===
        Report ID: ${System.currentTimeMillis()}
        Timestamp: $timestamp
        Sender: ${if (message.isUser) "User" else "Assistant"}
        Reason: $reason
        Content: ${message.content.take(200)}${if (message.content.length > 200) "..." else ""}
        Report Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
        Device: Android
        App Version: 1.0
        ========================
        
    """.trimIndent()

    try {
        // Save to app's private storage
        val file = java.io.File(context.filesDir, "content_reports.txt")
        file.appendText(report)

        // In a production app, you would also send this to your server
        // sendReportToServer(report)

        Toast.makeText(
            context,
            "Thank you for your report. We will review this content.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Unable to submit report. Please try again.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
