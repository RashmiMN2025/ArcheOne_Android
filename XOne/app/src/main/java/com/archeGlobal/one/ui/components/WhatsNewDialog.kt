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
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface (
            modifier = Modifier
                .fillMaxWidth(0.94f) // 98% of actual screen width
                .padding(horizontal = 8.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF6F4EE)
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
                    modifier = Modifier.size(46.dp),
                    tint = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = "What's New",
                    fontSize = 19.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle
                Text(
                    text = "Explore the latest updates to enhance your experience!",
                    fontSize = 14.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // What's new items
                whatsNewItems.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // 🔴 Bullet + Category Title in one row
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Color(0xFFDD3825),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = item.category,
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // 📌 Description directly below category
                        Row(
                            modifier = Modifier
                                .padding(20.dp, 0.dp, 0.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.description,
                                fontSize = 10.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                lineHeight = 22.sp
                            )
                        }

                        // Divider after each item except last
                        if (item != whatsNewItems.last()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(
                                color = Color.LightGray,
                                thickness = 1.dp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Continue button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(18.dp),
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