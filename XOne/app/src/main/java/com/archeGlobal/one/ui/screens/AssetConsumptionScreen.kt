package com.archeGlobal.one.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.archeGlobal.one.ui.theme.PrimaryRed
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.model.BulkUploadRequest
import com.archeGlobal.one.model.Message
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetConsumptionScreen(
    items : List<AssetConsumptionItem>,
    onBackPressed: () -> Unit,
    onItemClick: (String, NavController?) -> Unit, // Updated to accept NavController
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userData = OtpVerificationController.getUserData()
    val userEmail = userData?.email ?: ""

    var showBulkUploadDialog by remember { mutableStateOf(false) }
    var bulkSuccessMessage by remember { mutableStateOf("") }

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
                                text = "Asset Consumption",
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
                            AssetConsumption(
                                item = item,
                                onClick = {
                                    when (item.name) {
                                        "Tagged Assets" -> {
                                            navController?.navigate("tagged_assets")
                                        }
                                        "Bulk Upload" -> {
                                            if (userEmail.isBlank()) {
                                                Toast.makeText(context, "Email not found", Toast.LENGTH_LONG).show()
                                                return@AssetConsumption
                                            }
                                            scope.launch {
                                                try {
                                                    val request = BulkUploadRequest(userEmail)
                                                    val response = RetrofitClient.apiService.initiateBulkUpload(request)
                                                    if (response.success) {
                                                        bulkSuccessMessage = response.message
                                                        showBulkUploadDialog = true
                                                    } else {
                                                        Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                        "Download Reports" -> {
                                            navController?.navigate("download_reports")
                                        }
                                        else -> onItemClick(item.name, navController)
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }

    if (showBulkUploadDialog) {
        BulkUploadSuccessDialog(
            message = bulkSuccessMessage,
            onDismiss = { showBulkUploadDialog = false }
        )
    }
}

@Composable
fun BulkUploadSuccessDialog(
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
                .padding(20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
            elevation = CardDefaults.cardElevation(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Green Checkmark
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = message,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        "OK",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

            }
        }
    }
}

@Composable
fun AssetConsumption (
    item: AssetConsumptionItem,
    onClick: () -> Unit,
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

data class AssetConsumptionItem(
    val name: String,
    val description: String,
    val image: String
)

fun assetConsumptionItem(): List<AssetConsumptionItem> =
    listOf(
        AssetConsumptionItem(
            name = "Tagged Assets",
            description = "View tagged assets",
            image = "ic_tagged_assets"
        ),
        AssetConsumptionItem(
            name = "Download Reports",
            description = "Download \n asset reports",
            image = "ic_download_reports"
        ),
        AssetConsumptionItem(
            name = "Bulk Upload",
            description = "Upload assets \n in bulk",
            image = "ic_bulk_upload"
        )
    )

@Composable
private fun getIcon (image: String): Int =
    when (image) {
        "ic_tagged_assets" -> R.drawable.tag_asset
        "ic_download_reports" -> R.drawable.download1
        "ic_bulk_upload" -> R.drawable.share
        else -> R.drawable.ic_file
    }