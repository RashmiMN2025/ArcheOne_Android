package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.controller.ConsumptionReportController
import com.archeGlobal.one.model.ConsumptionReportModel
import com.archeGlobal.one.model.ConsumptionTab
import com.archeGlobal.one.model.ConsumptionStockCategory
import com.archeGlobal.one.model.ConsumptionStockItem
import com.archeGlobal.one.model.UsageCategory
import com.archeGlobal.one.model.UsageItem
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionReportScreen(
    model: ConsumptionReportModel,
    controller: ConsumptionReportController
) {
    BackHandler {
        controller.onBackPressed()
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ConsumptionReportHeader(
                onBackPressed = controller::onBackPressed
            )

            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Increased from 16.dp to 24.dp
            ) {
                item {
                    // Tab selector and location
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        TabAndLocationRow(
                            selectedTab = model.selectedTab,
                            selectedLocation = model.selectedLocation,
                            onTabSelected = controller::onTabSelected,
                            onLocationSelected = controller::onLocationSelected
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                when (model.selectedTab) {
                    ConsumptionTab.STOCK -> {
                        items(model.stockCategories) { category ->
                            StockCategoryCard(
                                category = category,
                                onDownloadReport = controller::onDownloadReport
                            )
                        }
                    }
                    ConsumptionTab.USAGE -> {
                        items(model.usageCategories) { category ->
                            UsageCategoryCard(
                                category = category,
                                onDownloadReport = controller::onDownloadReport
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

            // Loading overlay
            if (model.isLoading) {
                UniversalLoader(isLoading = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionReportHeader(
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.offset(x = (-24).dp),
                    text = "Consumption Report",
                    color = Color.Black,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun TabAndLocationRow(
    selectedTab: ConsumptionTab,
    selectedLocation: String,
    onTabSelected: (ConsumptionTab) -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab buttons
        Row {
            TabButton(
                text = "Stock",
                isSelected = selectedTab == ConsumptionTab.STOCK,
                onClick = { onTabSelected(ConsumptionTab.STOCK) },
                isFirst = true
            )
            TabButton(
                text = "Usage",
                isSelected = selectedTab == ConsumptionTab.USAGE,
                onClick = { onTabSelected(ConsumptionTab.USAGE) },
                isLast = true
            )
        }

        // Location selector
        LocationSelector(
            selectedLocation = selectedLocation,
            onLocationSelected = onLocationSelected
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val shape = when {
        isFirst -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
        isLast -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
        else -> RoundedCornerShape(0.dp)
    }
    
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) Color.White else Color(0xFFE0E0E0)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = GraphikFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
fun LocationSelector(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { /* TODO: Show location picker */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedLocation,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun StockCategoryCard(
    category: ConsumptionStockCategory,
    onDownloadReport: (String) -> Unit
) {
    Column {
        // Header with title and download button - outside the card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.title,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            DownloadReportButton {
                onDownloadReport(category.id)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card with chart only
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Bar chart
                if (category.items.isNotEmpty()) {
                    StockBarChart(items = category.items)
                }
            }
        }
    }
}

@Composable
fun UsageCategoryCard(
    category: UsageCategory,
    onDownloadReport: (String) -> Unit
) {
    Column {
        // Header with title and download button - outside the card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.title,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            DownloadReportButton {
                onDownloadReport(category.id)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card with chart only
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Bar chart
                if (category.items.isNotEmpty()) {
                    UsageBarChart(items = category.items)
                }
            }
        }
    }
}

@Composable
fun DownloadReportButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryRed
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Download Report",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

@Composable
fun StockBarChart(items: List<ConsumptionStockItem>) {
    val maxValue = items.maxOfOrNull { it.quantity } ?: 1
    val scrollState = rememberScrollState()
    
    // Make the chart wider than the screen to enable scrolling when there are many items
    val chartWidth = maxOf(400.dp, (items.size * 80).dp)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .fillMaxHeight()
        ) {
            drawBarChart(
                items = items.map { Triple(it.name, it.quantity, Color(android.graphics.Color.parseColor(it.color))) },
                maxValue = maxValue,
                size = size
            )
        }
    }
}

@Composable
fun UsageBarChart(items: List<UsageItem>) {
    val maxValue = items.maxOfOrNull { it.quantity } ?: 1
    val scrollState = rememberScrollState()
    
    // Make the chart wider than the screen to enable scrolling when there are many items
    val chartWidth = maxOf(400.dp, (items.size * 80).dp)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .fillMaxHeight()
        ) {
            drawBarChart(
                items = items.map { Triple(it.name, it.quantity, Color(android.graphics.Color.parseColor(it.color))) },
                maxValue = maxValue,
                size = size
            )
        }
    }
}

fun DrawScope.drawBarChart(
    items: List<Triple<String, Int, Color>>,
    maxValue: Int,
    size: Size
) {
    if (items.isEmpty() || maxValue == 0) return

    val leftPadding = 80f
    val chartWidth = size.width - leftPadding - 10f
    val barWidth = chartWidth / items.size * 0.7f
    val barSpacing = chartWidth / items.size * 0.3f
    val chartHeight = size.height * 0.7f
    val bottomPadding = size.height * 0.3f

    items.forEachIndexed { index, (name, value, color) ->
        val barHeight = (value.toFloat() / maxValue) * chartHeight
        val x = leftPadding + index * (barWidth + barSpacing) + barSpacing / 2
        val y = size.height - bottomPadding - barHeight

        // Draw bar
        if (value > 0) {
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }

        // Draw value on top of bar
        if (value > 0) {
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    value.toString(),
                    x + barWidth / 2,
                    y - 10,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 32f
                        setColor(android.graphics.Color.BLACK)
                        isFakeBoldText = true
                    }
                )
            }
        }

        // Draw item name at bottom
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 28f
                setColor(android.graphics.Color.BLACK)
            }

            // Function to split text into lines based on available width
            fun splitTextIntoLines(text: String, maxCharsPerLine: Int): List<String> {
                if (text.length <= maxCharsPerLine) {
                    return listOf(text)
                }
                
                val words = text.split(" ", "_", "\\", "/", "-")
                val lines = mutableListOf<String>()
                var currentLine = ""
                
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (testLine.length <= maxCharsPerLine) {
                        currentLine = testLine
                    } else {
                        if (currentLine.isNotEmpty()) {
                            lines.add(currentLine)
                            currentLine = word
                        } else {
                            // Single word is too long, split it
                            if (word.length > maxCharsPerLine) {
                                var remainingWord = word
                                while (remainingWord.length > maxCharsPerLine) {
                                    lines.add(remainingWord.take(maxCharsPerLine))
                                    remainingWord = remainingWord.drop(maxCharsPerLine)
                                }
                                if (remainingWord.isNotEmpty()) {
                                    currentLine = remainingWord
                                }
                            } else {
                                currentLine = word
                            }
                        }
                    }
                }
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                
                return lines.take(3) // Limit to 3 lines maximum
            }
            
            // Split the name into lines (approximately 12 characters per line for good readability)
            val lines = splitTextIntoLines(name, 12)
            
            // Draw each line
            lines.forEachIndexed { lineIndex, line ->
                val yOffset = size.height - bottomPadding + 40 + (lineIndex * 35)
                drawText(line, x + barWidth / 2, yOffset, textPaint)
            }
        }
    }

    // Draw Y-axis labels and grid lines
    val yAxisLabels = if (maxValue <= 10) {
        (0..maxValue).toList()
    } else {
        listOf(0, maxValue / 4, maxValue / 2, (maxValue * 3) / 4, maxValue)
    }
    
    yAxisLabels.forEach { label ->
        val y = size.height - bottomPadding - (label.toFloat() / maxValue) * chartHeight

        // Draw horizontal grid line segments (avoiding bars)
        if (label > 0) { // Don't draw line for 0
            var currentX = leftPadding
            val lineEndX = size.width - 10f
            
            items.forEachIndexed { index, _ ->
                val barX = leftPadding + index * (barWidth + barSpacing) + barSpacing / 2
                val barEndX = barX + barWidth
                
                // Draw line segment before bar
                if (currentX < barX) {
                    drawLine(
                        color = Color(0xFFE0E0E0),
                        start = Offset(currentX, y),
                        end = Offset(barX, y),
                        strokeWidth = 1.5f
                    )
                }
                currentX = barEndX
            }
            
            // Draw final segment after last bar
            if (currentX < lineEndX) {
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(currentX, y),
                    end = Offset(lineEndX, y),
                    strokeWidth = 1.5f
                )
            }
        }

        // Draw Y-axis label
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                label.toString(),
                leftPadding - 10f,
                y + 8f,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.RIGHT
                    textSize = 32f
                    setColor(android.graphics.Color.BLACK)
                    isAntiAlias = true
                }
            )
        }
    }
}
