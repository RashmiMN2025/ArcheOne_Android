package com.archeGlobal.one.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.ConsumptionReportController
import com.archeGlobal.one.model.ConsumptionReportModel
import com.archeGlobal.one.model.ConsumptionStockCategory
import com.archeGlobal.one.model.ConsumptionStockItem
import com.archeGlobal.one.model.ConsumptionTab
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
                    verticalArrangement = Arrangement.spacedBy(32.dp) // Increased spacing between cards
                ) {
                    item {
                        // Tab selector and location
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            TabAndLocationRow(
                                selectedTab = model.selectedTab,
                                selectedLocation = model.selectedLocation,
                                locations = model.locations,
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
                    fontWeight = FontWeight.SemiBold,
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
    locations: List<String>,
    onTabSelected: (ConsumptionTab) -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
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
            locations = locations,
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
        isFirst -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
        isLast -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) Color.White else Color(0xFFE0E0E0)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = GraphikFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            color = Color.Black
        )
    }
}

@Composable
fun LocationSelector(
    selectedLocation: String,
    locations: List<String>,
    onLocationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.location_selector),
                    contentDescription = "Location",
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = location,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    }
                )
            }
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
                text = category.title.replace("_", " "),
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false)
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
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
            ) {
                // Add more space before the chart to push it down further
                Spacer(modifier = Modifier.height(24.dp))
                // Bar chart
                if (category.items.isNotEmpty()) {
                    StockBarChart(
                        items = category.items.sortedBy { it.name },
                        categoryName = category.title
                    )
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
                text = category.title.replace("_", " "),
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false)
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
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
            ) {
                // Add more space before the chart to push it down further
                Spacer(modifier = Modifier.height(24.dp))
                // Bar chart
                if (category.items.isNotEmpty()) {
                    UsageBarChart(
                        items = category.items.sortedBy { it.name },
                        categoryName = category.title
                    )
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
        Image(
            painter = painterResource(id = R.drawable.report_download),
            contentDescription = "Download",
            modifier = Modifier.size(16.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Download Report",
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

// Function to get color based on category name
fun getCategoryColor(categoryName: String): Color {
    android.util.Log.d("ConsumptionReport", "getCategoryColor called with: '$categoryName'")

    val color = when {
        // Check for stationary/stationery first before stock (since "Stationary Stock" contains both)
        categoryName.contains("stationary", ignoreCase = true) || categoryName.contains("stationery", ignoreCase = true) -> {
            android.util.Log.d("ConsumptionReport", "Matched stationary/stationery - returning teal")
            Color(0xFF4ECDC4) // Teal
        }
        categoryName.contains("party", ignoreCase = true) -> {
            android.util.Log.d("ConsumptionReport", "Matched party - returning blue")
            Color(0xFF45B7D1) // Blue
        }
        categoryName.contains("hk", ignoreCase = true) || categoryName.contains("housekeeping", ignoreCase = true) -> {
            android.util.Log.d("ConsumptionReport", "Matched hk/housekeeping - returning green")
            Color(0xFF96CEB4) // Green
        }
        categoryName.contains("all", ignoreCase = true) -> {
            android.util.Log.d("ConsumptionReport", "Matched stock - returning red")
            Color(0xFFFF6B6B) // Red
        }
        else -> {
            android.util.Log.d("ConsumptionReport", "No match - returning default red")
            Color(0xFFFF6B6B) // Default to red
        }
    }

    android.util.Log.d("ConsumptionReport", "Final color for '$categoryName': $color")
    return color
}

@Composable
fun StockBarChart(items: List<ConsumptionStockItem>, categoryName: String) {
    val maxValue = items.maxOfOrNull { it.quantity } ?: 1.0
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
                items = items.map { Triple(it.name, it.quantity, getCategoryColor(categoryName)) },
                maxValue = maxValue,
                size = size
            )
        }
    }
}

@Composable
fun UsageBarChart(items: List<UsageItem>, categoryName: String) {
    val maxValue = items.maxOfOrNull { it.quantity } ?: 1.0
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
                items = items.map { Triple(it.name, it.quantity, getCategoryColor(categoryName)) },
                maxValue = maxValue,
                size = size
            )
        }
    }
}

fun DrawScope.drawBarChart(
    items: List<Triple<String, Double, Color>>,
    maxValue: Double,
    size: Size
) {
    if (items.isEmpty()) return

    // Use minimum value of 1.0 to ensure chart shows even when all values are 0
    val effectiveMaxValue = if (maxValue == 0.0) 1.0 else maxValue

    val leftPadding = 80f
    val chartWidth = size.width - leftPadding - 10f
    val barWidth = chartWidth / items.size * 0.7f
    val barSpacing = chartWidth / items.size * 0.3f
    val chartHeight = size.height * 0.7f
    val bottomPadding = size.height * 0.3f

    items.forEachIndexed { index, (name, value, color) ->
        val barHeight = (value.toFloat() / effectiveMaxValue.toFloat()) * chartHeight
        val x = leftPadding + index * (barWidth + barSpacing) + barSpacing / 2
        val y = size.height - bottomPadding - barHeight

        // Draw bar (even for zero values, show a minimal bar)
        if (value > 0) {
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        } else {
            // Draw minimal placeholder bar for zero values
            drawRect(
                color = Color.LightGray,
                topLeft = Offset(x, size.height - bottomPadding - 2f),
                size = Size(barWidth, 2f)
            )
        }

        // Always draw value on top of bar (show 0 for zero values)
        drawContext.canvas.nativeCanvas.apply {
            // Round to nearest whole number and display as integer
            val displayValue = kotlin.math.round(value).toInt().toString()
            val textY = if (value > 0) y - 10 else size.height - bottomPadding - 15f

            drawText(
                displayValue,
                x + barWidth / 2,
                textY,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 32f
                    setColor(android.graphics.Color.BLACK)
                    isFakeBoldText = true
                }
            )
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

            // Replace underscores with spaces and split the name into lines (approximately 12 characters per line for good readability)
            val lines = splitTextIntoLines(name.replace("_", " "), 12)

            // Draw each line
            lines.forEachIndexed { lineIndex, line ->
                val yOffset = size.height - bottomPadding + 40 + (lineIndex * 35)
                drawText(line, x + barWidth / 2, yOffset, textPaint)
            }
        }
    }

    // Draw Y-axis labels and grid lines
    val yAxisLabels = if (effectiveMaxValue <= 10.0) {
        (0..effectiveMaxValue.toInt()).map { it.toDouble() }
    } else {
        listOf(0.0, effectiveMaxValue / 4, effectiveMaxValue / 2, (effectiveMaxValue * 3) / 4, effectiveMaxValue)
    }

    yAxisLabels.forEach { label ->
        val y = size.height - bottomPadding - (label.toFloat() / effectiveMaxValue.toFloat()) * chartHeight

        // Draw continuous horizontal grid line (avoiding bars that are taller than the line)
        if (label > 0) { // Don't draw line for 0
            var currentX = leftPadding
            val lineEndX = size.width - 10f

            items.forEachIndexed { index, (_, value, _) ->
                val barX = leftPadding + index * (barWidth + barSpacing) + barSpacing / 2
                val barEndX = barX + barWidth
                val barHeight = (value.toFloat() / effectiveMaxValue.toFloat()) * chartHeight
                val barTopY = size.height - bottomPadding - barHeight

                // Only skip drawing through the bar if the bar is taller than this grid line
                val barIsTallerThanLine = value > 0 && barTopY < y

                if (barIsTallerThanLine) {
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
                } // If bar is shorter than line or no bar, continue the line
            }

            // Draw final segment after last bar to end of chart
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
                if (label % 1.0 == 0.0) label.toInt().toString() else label.toString(),
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
