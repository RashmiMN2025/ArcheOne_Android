package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.TextStyle

@Composable
fun ScopeSection(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Background image
        Image(
            painter = when(title) {
                "Scope1: Operational Efficiency" -> painterResource(id = R.drawable.scope1)
                "Scope2: Sustainable Workspaces" -> painterResource(id = R.drawable.scope2)
                else -> painterResource(id = R.drawable.scope3)
            },
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        // Content container with glass effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glass effect card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.99f),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 21.sp
                    )
                    
                    items.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "•",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = item,
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InitiativeCard(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),  // Reduced padding from 24.dp to 16.dp
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)  // Reduced spacing from 16.dp to 12.dp
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)  // Reduced from 64.dp to 48.dp
                    .background(Color(0xFF00A651).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = text,
                    tint = Color(0xFF00A651),
                    modifier = Modifier.size(24.dp)  // Reduced from 32.dp to 24.dp
                )
            }

            Text(
                text = text,
                fontSize = 14.sp,  // Reduced from 18.sp to 14.sp
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,  // Reduced from 24.sp to 20.sp
                overflow = TextOverflow.Ellipsis,
                maxLines = 4  // Reduced from 6 to 4 for more compact appearance
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionScreen(onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {  // Removed parent padding here
        Column(
            modifier = Modifier
                .padding(16.dp)  // Added padding to inner content
        ) {
            // Top Bar with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "Vision",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Hero Image Section with Vision text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.visionb),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Content container with glass effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glass effect card for vision text
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.99f),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Vision",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Arche 2035: A sustainability vision as transformative as our solutions.",
                                color = Color.White,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vision content with plain text (no card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Our Vision: Clear Goals, Real Impact",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "At Arche, our vision is to lead with sustainability. We aim to reduce our carbon footprint, embrace renewable energy, and build a greener digital future. This vision extends beyond solutions—it's about creating lasting impact. Join us on our journey to a sustainable 2035.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vision content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Scope sections with bottom padding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val scope1Items = listOf(
                        "Substitution for diesel generators (DG)",
                        "Improved HVAC systems",
                        "Transition to electric vehicles (EVs) for company fleets"
                    )
                    
                    val scope2Items = listOf(
                        "Sustainable office spaces",
                        "Rating existing offices for efficiency",
                        "Expanding renewable energy sources"
                    )
                    
                    val scope3Items = listOf(
                        "Advocating hybrid working",
                        "Encouraging EV adoption",
                        "Optimizing business travel and leased spaces"
                    )

                    ScopeSection(
                        title = "Scope1: Operational Efficiency",
                        items = scope1Items
                    )

                    ScopeSection(
                        title = "Scope2: Sustainable Workspaces",
                        items = scope2Items
                    )

                    ScopeSection(
                        title = "Scope3: Sustainable Value Chain",
                        items = scope3Items
                    )
                }

                // Protecting the Planet section heading
                Text(
                    text = "Protecting the planet, Powering\nthe progress.",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Our focus is on transforming our operations through energy efficiency, renewable energy adoption, and innovative strategies, ensuring we drive sustainability while delivering impactful solutions.",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Initiative Cards
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)  // Reduced from 280.dp to 200.dp
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)  // Reduced spacing from 16.dp to 12.dp
                    ) {
                        item {
                            InitiativeCard(
                                icon = R.drawable.ic_eco_truck,
                                text = "Advancing low-carbon logistics to reshape resource movement, cutting emissions every mile.",
                                modifier = Modifier.width(200.dp)
                            )
                        }
                        
                        item {
                            InitiativeCard(
                                icon = R.drawable.ic_eco_leaf,
                                text = "Scaling sustainable practices that prioritize planet-first decisions for a lasting impact.",
                                modifier = Modifier.width(200.dp)
                            )
                        }
                        
                        item {
                            InitiativeCard(
                                icon = R.drawable.ic_eco_recycle,
                                text = "Adopting lifecycle management to ensure every product contributes to a circular economy.",
                                modifier = Modifier.width(200.dp)
                            )
                        }

                        item {
                            InitiativeCard(
                                icon = R.drawable.ic_eco_footprint,
                                text = "Offsetting emissions with projects that neutralize our footprint across global operations.",
                                modifier = Modifier.width(200.dp)
                            )
                        }

                        item {
                            InitiativeCard(
                                icon = R.drawable.ic_eco_travel,
                                text = "Streamlining business travel with sustainable practices to minimize our carbon footprint.",
                                modifier = Modifier.width(200.dp)
                            )
                        }

                        item {
                            InitiativeCard(
                                icon = R.drawable.ic_eco_building,
                                text = "Optimizing office spaces with energy-efficient designs to create greener work environments.",
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // CEO Vision Section (outside the padded Column)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Leading with purpose",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Our CEO's Vision",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C853) // Green shade
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "At Arche, sustainability is our vision and opportunity to lead with purpose. As innovators, we believe our technology should transform businesses while safeguarding our planet's future. Our vision runs deep—rethinking operations with renewable energy and low-impact solutions. We ask, What more can we do?—and then act.\n\nOur goal is Net-Zero emissions by 2035, a bold vision that defines us. We integrate sustainability into our culture, products, and partnerships, challenging ourselves to think differently and act responsibly. This vision isn't just Arche's—it's what we achieve together. Collaborating with partners, customers, and communities, we build a future where progress and sustainability align. At Arche, we innovate with conscience, leaving a legacy of change for generations.",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                // CEO signature and image
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ceo),
                        contentDescription = "CEO Image",
                        modifier = Modifier
                            .size(52.dp)  // Increased from 48.dp to 52.dp
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))  // Add spacing between image and text
                    
                    Column {
                        Text(
                            text = "Bobby MD",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CEO, Arche",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
