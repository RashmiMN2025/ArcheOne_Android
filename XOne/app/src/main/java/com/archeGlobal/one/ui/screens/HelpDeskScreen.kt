package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.model.HelpDeskFAQ
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDeskScreen(controller: HelpDeskController) {
    val model by controller.model.collectAsState()

    // Handle back press gesture to navigate to proper source screen
    BackHandler {
        controller.navigateToHome()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        WelcomeBackgroundTop,
                                        WelcomeBackgroundMiddle,
                                        WelcomeBackgroundBottom,
                                    ),
                            ),
                    ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                modifier = Modifier.offset(x = 24.dp),
                                text = "Help Desk",
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    navigationIcon = {
                        val backInteractionSource = remember { MutableInteractionSource() }
                        val isBackPressed by backInteractionSource.collectIsPressedAsState()

                        val backScale by animateFloatAsState(
                            targetValue = if (isBackPressed) 0.8f else 1f,
                            animationSpec = tween(durationMillis = 150),
                            label = "backScale",
                        )

                        IconButton(
                            onClick = { controller.navigateToHome() },
                            interactionSource = backInteractionSource,
                            modifier = Modifier.scale(backScale),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                            )
                        }
                    },
                    actions = {
                        val trackInteractionSource = remember { MutableInteractionSource() }
                        val isTrackPressed by trackInteractionSource.collectIsPressedAsState()

                        val trackScale by animateFloatAsState(
                            targetValue = if (isTrackPressed) 0.9f else 1f,
                            animationSpec = tween(durationMillis = 150),
                            label = "trackScale",
                        )

                        TextButton(
                            onClick = { controller.navigateToTrackTickets() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            interactionSource = trackInteractionSource,
                            modifier = Modifier.scale(trackScale),
                        ) {
                            Text(
                                text = "Track Tickets",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                ) {
                    Text(
                        text = "Support Categories",
                        fontFamily = GraphikFontFamily,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    Text(
                        text = "Select a category for detailed assistance or raise a ticket",
                        fontFamily = GraphikFontFamily,
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )

                    when {
                        model.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        model.error != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Error loading FAQ data",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Red,
                                )
                                Text(
                                    text = model.error!!,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(top = 8.dp)
                                            .padding(horizontal = 16.dp),
                                )
                                Button(
                                    onClick = { controller.refreshFAQData() },
                                    modifier = Modifier.padding(top = 16.dp),
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                        else -> {
                            AnimatedVisibility(
                                visible = !model.isLoading,
                                enter = fadeIn(animationSpec = tween(300)),
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(22.dp),
                                ) {
                                    // Dynamically generate categories from FAQ data
                                    val faqsByCategory = model.faqItems.groupBy { it.title }

                                    // Display each category with its FAQ items
                                    faqsByCategory.forEach { (categoryTitle, faqs) ->
                                        if (categoryTitle != "Other Issues") { // Handle "Other Issues" separately
                                            item {
                                                CategorySection(
                                                    title = categoryTitle,
                                                    items = faqs.map { faq -> faq.question to faq.id },
                                                    onItemClick = { itemId -> controller.navigateToFAQOrRaiseTicket(itemId) },
                                                )
                                            }
                                        }
                                    }

                                    // Add extra space at the bottom for the fixed button
                                    item {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fixed "Raise a Ticket" button at the bottom
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 40.dp),
        ) {
            val raiseTicketInteractionSource = remember { MutableInteractionSource() }
            val isRaiseTicketPressed by raiseTicketInteractionSource.collectIsPressedAsState()

            val raiseTicketScale by animateFloatAsState(
                targetValue = if (isRaiseTicketPressed) 0.95f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "raiseTicketScale",
            )

            Button(
                onClick = { controller.navigateToRaiseConcern("Raise a Ticket") },
                modifier =
                    Modifier
                        .fillMaxWidth(0.63f)
                        .height(56.dp)
                        .scale(raiseTicketScale)
                        .align(Alignment.Center),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                    ),
                shape = RoundedCornerShape(28.dp),
                interactionSource = raiseTicketInteractionSource,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Raise a Ticket",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.25.sp,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(18.dp)
                                .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Ticket",
                            tint = Color.White,
                            modifier = Modifier.size(9.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    title: String,
    items: List<Pair<String, String>>,
    onItemClick: (String) -> Unit,
) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = GraphikFontFamily,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        items.forEach { (itemText, itemId) ->
            CategoryItem(
                text = itemText,
                onClick = { onItemClick(itemId) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CategoryItem(
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.7f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.helpq),
                        contentDescription = "Help Question",
                        modifier = Modifier.size(20.dp),
                        colorFilter =
                            androidx.compose.ui.graphics.ColorFilter
                                .tint(Color.Gray),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    lineHeight = 20.sp,
                )
            }

            Text(
                text = ">",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
fun FAQCard(
    faq: HelpDeskFAQ,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.7f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (faq.question.contains("Other issue", ignoreCase = true)) {
                                    Color(0xFFD32F2F)
                                } else {
                                    Color(0xFFE8E4F3)
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (faq.question.contains("Other issue", ignoreCase = true)) {
                        Text(
                            text = "!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = faq.question,
                    fontSize = 15.sp,
                    color =
                        if (faq.question.contains("Other issue", ignoreCase = true)) {
                            Color(0xFFD32F2F)
                        } else {
                            Color.Black
                        },
                    fontFamily = GraphikFontFamily,
                    fontWeight =
                        if (faq.question.contains("Other issue", ignoreCase = true)) {
                            FontWeight.Medium
                        } else {
                            FontWeight.Normal
                        },
                    modifier = Modifier.weight(1f),
                    lineHeight = 20.sp,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Arrow",
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(19.dp),
            )
        }
    }
}
