package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun CoreValuesScreen(
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 100.dp) // Add padding to avoid overlapping with the fixed header
        ) {
            // New Heading
            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    text = "Our values to keep you at the centre.",
                    color = Color.Black,
                    fontSize = 30.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 36.sp, // Added line spacing
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Blocks
            val blocks = listOf(
                Triple(
                    "Customer First",
                    R.drawable.customer,
                    "Our customers are the essence of our mission. Putting their needs at the forefront, we strive to deliver not just solutions, but meaningful experiences that resonate. By consistently exceeding expectations, we reaffirm our dedication to those we serve."
                ),
                Triple(
                    "Excellence",
                    R.drawable.excellence,
                    "Excellence is more than a goal; it is a continuous pursuit. We are committed to upholding the highest standards in all that we do. By striving for excellence in every task, project, and interaction, we ensure that our work not only meets expectations but also sets new benchmarks."
                ),
                Triple(
                    "Integrity",
                    R.drawable.intigrity,
                    "Integrity is the bedrock of our reputation. Every interaction, every decision, and every commitment we make is underpinned by a foundation of honesty and transparency. It is through this unwavering integrity that we build lasting trust and respect with our clients, partners, and each other."
                ),
                Triple(
                    "Accountability",
                    R.drawable.accountability,
                    "Accountability fosters a culture of reliability and respect. We take ownership of our actions, choices, and outcomes, knowing that each of us contributes to the collective success of Arche. By holding ourselves accountable, we demonstrate our commitment to one another and to the values we uphold."
                ),
                Triple(
                    "Innovation",
                    R.drawable.innovation,
                    "Innovation propels us forward. As a company, we are united by a passion to explore new ideas, embrace change, and forge creative solutions that redefine what’s possible. Our drive to innovate keeps us at the cutting edge, empowering us to make a lasting impact in the digital and AI landscape."
                )
            )

            blocks.forEach { (title, icon, description) ->
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = icon), // Display icon in original color
                            contentDescription = "Icon",
                            modifier = Modifier.size(30.dp),
                            tint = Color.Unspecified // Ensure the icon retains its original color
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp // Reduced line spacing for description
                    )
                }
            }
        }

        // Fixed Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(vertical = 16.dp)
                .statusBarsPadding() // Add this
                .align(Alignment.TopCenter) // Align the top bar at the top center
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp) // Move the arrow slightly to the right
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back"
                )
            }

            Text(
                text = "CoreValues",
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
