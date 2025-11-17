package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.DownloadReportsController
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import com.archeGlobal.one.controller.OtpVerificationController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadReportsScreen(
    controller: DownloadReportsController,
    onBackPressed: () -> Unit
) {
    val model = controller.model
    val context = LocalContext.current
    val userData = OtpVerificationController.getUserData()

    val userEmail = userData?.email ?: ""

    var showTaggedDialog by remember { mutableStateOf(false) }
    var dialogReportType by remember { mutableStateOf("tagged") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

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
                                text = "Download Reports",
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

                if (model.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        UniversalLoader(isLoading = true)
                    }
                } else if (model.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = model.error,
                            color = Color.Red,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                ReportCard(
                                    title = "Tagged Assets",
                                    icon = R.drawable.download1,
                                    description = "Export tagged assets by location",
                                    onClick = {
                                        dialogReportType = "tagged"
                                        showTaggedDialog = true
                                    }
                                )
                            }
                            item {
                                ReportCard(
                                    title = "Inventory",
                                    icon = R.drawable.download1,
                                    description = "Export full inventory data",
                                    onClick = {
                                        dialogReportType = "inventory"
                                        showTaggedDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // SHOW DIALOG
                if (showTaggedDialog) {
                    TaggedAssetsDialog(
                        locations = model.locations,
                        reportType = dialogReportType,
                        onDismiss = { showTaggedDialog = false },
                        onSubmit = { location: String? ->
                            controller.downloadReport(
                                reportType = dialogReportType,
                                location = location,
                                userEmail = userEmail,
                                onSuccess = { msg ->
                                    successMessage = msg
                                    showTaggedDialog = false
                                    showSuccessDialog = true
                                },
                                onError = { err ->
                                    showTaggedDialog = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }

                if (showSuccessDialog) {
                    SuccessDialog(
                        message = successMessage,
                        onDismiss = { showSuccessDialog = false }
                    )
                }
            }
        }
    }
}


@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
            elevation = CardDefaults.cardElevation(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Green Checkmark
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = message,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        "OK",
                        color = Color.White,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    icon: Int,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.55f)
            .height(230.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                        painter = painterResource(icon),
                        contentDescription = title,
                        modifier = Modifier.size(55.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color(0xFFDD3825))
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 19.sp,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = description,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)
                )
            }
        }
    }
}

@Composable
fun TaggedAssetsDialog(
    locations: List<String>,
    onDismiss: () -> Unit,
    reportType: String,
    onSubmit: (String?) -> Unit
) {
    var selectedLocation by remember { mutableStateOf( "") }
    var expanded by remember { mutableStateOf(false) }

    val headerText = when (reportType) {
        "tagged" -> "Download Tagged Assets"
        "inventory" -> "Download Inventory Assets"
        else -> "Download Report"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.download1),
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = headerText,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Select Location",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(12.dp))

                // TextField + Centered Popup
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedLocation.isEmpty()) "Select Location" else selectedLocation,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.dropdown),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { expanded = !expanded }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = PrimaryRed,
                            focusedTextColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )

                    // Centered Popup with Dividers
                    if (expanded) {
                        Popup(
                            onDismissRequest = { expanded = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            // Get screen size
                            val configuration = LocalConfiguration.current
                            val screenWidth = configuration.screenWidthDp.dp
                            val popupWidth = (screenWidth * 0.8f).coerceAtMost(400.dp)

                            Card(
                                modifier = Modifier
                                    .width(popupWidth)
                                    .heightIn(max = 300.dp) // Max height
                                    .align(Alignment.Center) // Center on screen
                                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                LazyColumn {
                                    items(locations.size) { index ->
                                        val location = locations[index]

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedLocation = location
                                                    expanded = false
                                                }
                                                .padding(horizontal = 20.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = location,
                                                fontFamily = GraphikFontFamily,
                                                fontSize = 16.sp,
                                                color = Color.Black,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        // Divider (except last item)
                                        if (index < locations.lastIndex) {
                                            Divider(
                                                color = Color(0xFFE0E0E0),
                                                thickness = 0.5.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF949494)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "Cancel",
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            // send **null** when nothing is selected
                            onSubmit(if (selectedLocation.isEmpty()) null else selectedLocation)
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
//                        enabled = selectedLocation.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            "Submit",
                            color = Color.White,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}