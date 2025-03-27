package com.archeGlobal.one.ui.screens

import android.annotation.SuppressLint
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ChatViewModel
import com.archeGlobal.one.model.Message
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.model.ChatBottomNavigationBar
import com.archeGlobal.one.ui.components.BottomNavigationBar
import com.archeGlobal.one.ui.components.TypingIndicator
import com.archeGlobal.one.utils.ChatData
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    navController: NavController,
    onBackPressed: () -> Unit,
    showBottomBar: Boolean = false
) {
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
                            Color(0xFF474749)  // Dark grey at bottom
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
                            viewModel = viewModel
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
                                focusedIndicatorColor = Color(0xFFDD3825),
                                focusedTextColor = Color.Black,
                                cursorColor = Color(0xFFDD3825),
                                unfocusedIndicatorColor = Color.Gray,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 1,
                            singleLine = true
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDD3825), CircleShape)
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
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Bottom navigation
                if (showBottomBar) {
                    ChatBottomNavigationBar(
                        onHomeClick = { navController.navigate("home") },
                        onChatClick = { /* Already on Chat screen */ },
                        onSOSClick = { navController.navigate("sos") },
                        onProfileClick = { navController.navigate("profile") }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    viewModel: ChatViewModel
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
            if (!message.isUser && message.content.contains("Welcome to ArcheOne Assistant!")) {
                // Welcome message
                WelcomeMessage()
            } else if (!message.isUser && message.content.contains("Here's what I can help you with:")) {
                // Support categories message
                SupportCategoriesMessage(viewModel)
            } else if (!message.isUser && message.content.contains("•")) {
                // FAQ list message
                Card(
                    modifier = Modifier.padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFAF5) // Creamy color background
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        message.content.split("\n").forEach { line ->
                            if (line.startsWith("•")) {
                                val question = line.substring(2).trim()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectFAQ(question) }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = line,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "›",
                                        color = Color(0xFFDD3825),
                                        fontSize = 18.sp
                                    )
                                }
                            } else if (line.isNotEmpty()) {
                                Text(
                                    text = line,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Regular message bubble
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (message.isUser) Color(0xFFDD3825) else Color(0xFFFFFAF5) // Creamy color for bot messages
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = if (message.isUser) Color.White else Color.Black
                    )
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
                    text = "Welcome to ArcheOne Assistant!",
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
fun SupportCategoriesMessage(viewModel: ChatViewModel) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFAF5) // Creamy color background
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Here's what I can help you with:",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Feel free to ask any questions!",
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Support categories
            SupportCategory(title = "Profile Update", onClick = { viewModel.selectFAQ("How do I update my profile?") })
            SupportCategory(title = "Technical Issues", onClick = { viewModel.selectFAQ("I'm having technical issues") })
            SupportCategory(title = "Company Policies", onClick = { viewModel.selectFAQ("Where can I find company policies?") })
            SupportCategory(title = "Forgot Password", onClick = { viewModel.selectFAQ("How do I reset my password?") })
            SupportCategory(title = "Benefits Enrollment", onClick = { viewModel.selectFAQ("How does benefits enrollment work?") })
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Show More",
                    color = Color(0xFFDD3825),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { /* Handle show more categories */ }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun SupportCategory(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Arrow",
                tint = Color(0xFFDD3825),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FAQQuestionRow(question: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
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
            
            Text(
                text = "›",
                color = Color(0xFFDD3825),
                fontSize = 18.sp
            )
        }
    }
}

private fun formatTime(date: Date): String {
    return DateFormat.format("hh:mm a", date).toString()
} 