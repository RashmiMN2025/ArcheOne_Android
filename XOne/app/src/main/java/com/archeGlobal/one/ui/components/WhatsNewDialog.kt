package com.archeGlobal.one.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.R
import com.archeGlobal.one.network.WhatsNewItem
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun WhatsNewDialog(
    whatsNewItems: List<WhatsNewItem>,
    appVersion: String = "1.3",
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon
                Icon(
                    painter = painterResource(id = R.drawable.arche_black2),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(42.dp),
                    tint = Color.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = "What's New in Version $appVersion",
                    fontSize = 19.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "Explore the latest updates to enhance your experience!",
                    fontSize = 13.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // What's new items
                whatsNewItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Red bullet point
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = Color(0xFFDD3825),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Category title
                            Text(
                                text = item.category,
                                fontSize = 15.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Description
                            Text(
                                text = item.description,
                                fontSize = 13.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (item != whatsNewItems.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Continue button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD3825),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
