package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier as ComposeModifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetITAdminScreen(
    items : List<AssetAdminItem>,
    onBackPressed: () -> Unit,
    onItemClick: (String) -> Unit,
) {
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
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0DCD1), // Light Beige
                            Color(0xFFC8C8CA), // Light Gray
                            Color(0xFF474749) // Dark Gray
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
                                modifier = Modifier.offset(x = 5.dp),
                                text = "Admin Dashboard",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {onBackPressed() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp)) // Balance the navigation icon
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(items) { item ->
                            AssetAdminCard(
                                item = item,
                                onClick = { onItemClick(item.name) }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun AssetAdminCard (
    item: AssetAdminItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.55f)
            .height(230.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .offset(y = (-8).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = getIcon(item.image)),
                        contentDescription = item.name,
                        modifier = Modifier.size(55.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color(0xFFDD3825))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.name,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 19.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.description,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(16.dp,0.dp, 16.dp, 16.dp)
                )
            }
        }
    }
}

data class AssetAdminItem(
    val name: String,
    val description: String,
    val image: String
)

fun assetAdminItem(): List<AssetAdminItem> =
    listOf(
        AssetAdminItem(
            name = "Asset Inventory",
            description = "View and assign asset inventory",
            image = "ic_inventory"
        ),
        AssetAdminItem(
            name = "Tickets",
            description = "Manage and track asset tickets",
            image = "ic_order_received"
        ),
        AssetAdminItem(
            name = "Asset Consumption",
            description = "Check usage and consumption",
            image = "ic_consumption_report"
        ),
        AssetAdminItem(
            name = "Self-Tag Requests",
            description = "Approve or reject user tagged assets",
            image = "ic_self_tag_request"
        )
    )

@Composable
private fun getIcon (image: String): Int =
    when (image) {
        "ic_inventory" -> R.drawable.inventory
        "ic_order_received" -> R.drawable.order_received
        "ic_consumption_report" -> R.drawable.consumption_report
        "ic_self_tag_request" -> R.drawable.self_tag
        else -> R.drawable.ic_file
    }