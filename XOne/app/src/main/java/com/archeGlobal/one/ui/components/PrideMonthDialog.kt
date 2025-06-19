package com.archeGlobal.one.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun PrideMonthDialog(
    isUsingPrideIcon: Boolean,
    onDismiss: () -> Unit,
    onToggleIcon: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Celebrating love, equality, and pride this month and always.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp),
                    fontSize = 15.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // App Icons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Regular App Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .size(64.dp),
                                
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = Color(0xFFDD3825)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.arche2),
                                    contentDescription = "App Icon",
                                    modifier = Modifier
                                        .size(48.dp),
                                        
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "App Icon",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                    
                    // Rotating loader between icons
                    RotatingLoader(spin = isSpinning)
                    
                    // Pride App Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .size(64.dp),
                                
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.archepride),
                                    contentDescription = "Pride App Icon",
                                    modifier = Modifier
                                        .fillMaxSize(),
                                        
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Event Icon",
                            fontSize = 14.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Change Icon Button
                Button(
                    onClick = {
                        isSpinning = true
                        onToggleIcon()
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(46.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUsingPrideIcon) Color(0xFFFEE9E9) else Color(0xFFDD3825),
                        contentColor = if (isUsingPrideIcon) Color(0xFFDD3825) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isUsingPrideIcon) "Change back to App Icon" else "Change to Pride Icon",
                        fontSize = 15.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = if (isUsingPrideIcon) Color(0xFFDD3825) else Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
