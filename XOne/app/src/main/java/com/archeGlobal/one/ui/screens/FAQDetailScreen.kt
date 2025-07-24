package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
fun FAQDetailScreen(
    faqId: String,
    controller: HelpDeskController
) {
    val faq = controller.getFAQById(faqId)

    if (faq == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WelcomeBackgroundTop,
                                WelcomeBackgroundMiddle,
                                WelcomeBackgroundBottom
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FAQ not found",
                    fontSize = 18.sp,
                    fontFamily = GraphikFontFamily,
                    color = Color.Black
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WelcomeBackgroundTop,
                            WelcomeBackgroundMiddle,
                            WelcomeBackgroundBottom
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.offset(x = (-24).dp),
                                text = "FAQ Details",
                                fontWeight = FontWeight.Bold,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { controller.navigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        QuestionCard(faq)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        AnswerCard(faq)
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Still unable to fix the issue",
                                fontSize = 15.sp,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black
                            )

                            Button(
                                onClick = {
                                    controller.raiseTicket(faq.question, faq.answer)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Raise a Ticket",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(faq: HelpDeskFAQ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.query),
                        contentDescription = "Query",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFD32F2F))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Query",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = faq.question,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GraphikFontFamily,
                color = Color.Black,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun AnswerCard(faq: HelpDeskFAQ) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.solution),
                        contentDescription = "Solution",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFD32F2F))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Solution",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create annotated string with styling for steps and emails
            val emailPattern = Regex("[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            val stepPattern = Regex("• ([^:]+):")
            val answerText = faq.answer
            val emailMatches = emailPattern.findAll(answerText).toList()
            val stepMatches = stepPattern.findAll(answerText).toList()

            val annotatedString = buildAnnotatedString {
                var lastIndex = 0
                val allMatches = (emailMatches.map { "email" to it } + stepMatches.map { "step" to it })
                    .sortedBy { it.second.range.first }

                allMatches.forEach { (type, match) ->
                    // Add text before match
                    append(answerText.substring(lastIndex, match.range.first))

                    when (type) {
                        "email" -> {
                            // Add clickable email
                            pushStringAnnotation(
                                tag = "EMAIL",
                                annotation = match.value
                            )
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFD32F2F),
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(match.value)
                            }
                            pop()
                        }
                        "step" -> {
                            // Add bullet point
                            append("• ")
                            // Add semibold step title
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(match.groupValues[1])
                            }
                            // Add colon
                            append(":")
                        }
                    }

                    lastIndex = match.range.last + 1
                }

                // Add remaining text
                if (lastIndex < answerText.length) {
                    append(answerText.substring(lastIndex))
                }
            }

            ClickableText(
                text = annotatedString,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontFamily = GraphikFontFamily,
                    lineHeight = 20.sp
                ),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "EMAIL",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { annotation ->
                        uriHandler.openUri("mailto:${annotation.item}")
                    }
                }
            )
        }
    }
}
