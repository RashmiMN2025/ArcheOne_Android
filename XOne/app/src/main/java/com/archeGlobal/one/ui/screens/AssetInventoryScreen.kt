package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetInventoryScreen(
    items : List<AssetInventoryItem>,
    onItemClick: (String) -> Unit,
    onBackPressed: () -> Boolean
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
                                text = "Asset Inventory",
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackPressed() }) {
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
                        .padding(4.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(items) { item ->
                            AssetInventory(
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
fun AssetInventory (
    item: AssetInventoryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = item.name,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.Black,
            )

            Text(
                text = item.description,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                textAlign = TextAlign.End,
                color = Color(0xFFDD3825),
                modifier = Modifier
                    .weight(0.6f)
                    .padding(end = 8.dp)
            )

        }
    }
}

data class AssetInventoryItem(
    val name: String,
    val description: String,
)

fun assetInventoryItem(): List<AssetInventoryItem> =
    listOf(
        AssetInventoryItem(
            name = "Switch",
            description = "Units: 10",
        ),
        AssetInventoryItem(
            name = "Printer",
            description = "Units: 8",
        ),
        AssetInventoryItem(
            name = "WiFi",
            description = "Units: 6",
        ),
        AssetInventoryItem(
            name = "Router",
            description = "Units: 4",
        ),
        AssetInventoryItem(
            name = "Keyboard",
            description = "Units: 2",
        ),
        AssetInventoryItem(
            name = "Mouse",
            description = "Units: 2",
        ),
        AssetInventoryItem(
            name = "Monitor",
            description = "Units: 2",
        ),
        AssetInventoryItem(
            name = "Speaker",
            description = "Units: 2",
        ),
        AssetInventoryItem(
            name = "Microphone",
            description = "Units: 2",
        ),
        AssetInventoryItem(
            name = "Projector",
            description = "Units: 1",
        ),
        AssetInventoryItem(
            name = "Scanner",
            description = "Units: 1",
        ),
        AssetInventoryItem(
            name = "Webcam",
            description = "Units: 1",
        ),
        AssetInventoryItem(
            name = "Tablet",
            description = "Units: 1",
        ),
        AssetInventoryItem(
            name = "Laptop",
            description = "Units: 1",
        ),
        AssetInventoryItem(
            name = "Desktop",
            description = "Units: 1",
        )
    )
